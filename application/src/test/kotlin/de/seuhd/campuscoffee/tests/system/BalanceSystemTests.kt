package de.seuhd.campuscoffee.tests.system

import de.seuhd.campuscoffee.api.dtos.BalanceDto
import de.seuhd.campuscoffee.api.dtos.ExpenseDto
import de.seuhd.campuscoffee.api.exceptions.ErrorResponse
import de.seuhd.campuscoffee.domain.model.persistedId
import de.seuhd.campuscoffee.tests.SystemTestUtils.client
import de.seuhd.campuscoffee.tests.SystemTestUtils.requireStatus
import de.seuhd.campuscoffee.tests.SystemTestUtils.statusCode
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.returnResult
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * System tests for reading the balance of a user and recording an expense through `/api/users/{userId}/balance` and
 * `/api/users/{userId}/expenses`. The tests for an unknown user use a random id, and all other tests use the fixture
 * user `maxmustermann`.
 */
class BalanceSystemTests : AbstractSystemTest() {
    private val loginName = "maxmustermann"

    private fun fixtureUserId(): UUID = fixtureUser(loginName).persistedId

    private fun getBalance(userId: UUID = fixtureUserId()) =
        client()
            .get()
            .uri("/api/users/{userId}/balance", userId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()

    private fun postExpense(
        body: Any,
        userId: UUID = fixtureUserId()
    ) = client()
        .post()
        .uri("/api/users/{userId}/expenses", userId)
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .exchange()

    private fun balance(): BalanceDto = getBalance().returnResult<BalanceDto>().responseBody!!

    @Test
    fun `getting the balance of a fixture user returns 200 OK with a balance of zero`() {
        val result = getBalance().returnResult<BalanceDto>()

        assertThat(result.status.value()).isEqualTo(200)
        assertThat(result.responseBody!!.loginName).isEqualTo(loginName)
        assertThat(result.responseBody!!.balance).isEqualByComparingTo("0.00")
    }

    @Test
    fun `recording an expense of 15 euro returns 200 OK with a balance of 15 euro`() {
        val result = postExpense(ExpenseDto(BigDecimal("15.00"))).returnResult<BalanceDto>()

        assertThat(result.status.value()).isEqualTo(200)
        assertThat(result.responseBody!!.loginName).isEqualTo(loginName)
        assertThat(result.responseBody!!.balance).isEqualByComparingTo("15.00")
        assertThat(balance().balance).isEqualByComparingTo("15.00")
    }

    @Test
    fun `recording two expenses adds both amounts to the balance`() {
        postExpense(ExpenseDto(BigDecimal("2.50"))).requireStatus(200)
        postExpense(ExpenseDto(BigDecimal("10.00"))).requireStatus(200)

        assertThat(balance().balance).isEqualByComparingTo("12.50")
    }

    @Test
    fun `recording an expense of zero euro returns 400 Bad Request and keeps the balance unchanged`() {
        val result = postExpense(ExpenseDto(BigDecimal("0.00"))).returnResult<ErrorResponse>()

        assertThat(result.status.value()).isEqualTo(400)
        assertThat(result.responseBody!!.errorCode).isEqualTo("ValidationException")
        assertThat(result.responseBody!!.message).contains("greater than 0.00")
        assertThat(balance().balance).isEqualByComparingTo("0.00")
    }

    @Test
    fun `recording a negative expense returns 400 Bad Request and keeps the balance unchanged`() {
        val result = postExpense(ExpenseDto(BigDecimal("-1.00"))).returnResult<ErrorResponse>()

        assertThat(result.status.value()).isEqualTo(400)
        assertThat(result.responseBody!!.errorCode).isEqualTo("ValidationException")
        assertThat(balance().balance).isEqualByComparingTo("0.00")
    }

    @Test
    fun `recording an expense with three decimal places returns 400 Bad Request`() {
        val result = postExpense(ExpenseDto(BigDecimal("1.999"))).returnResult<ErrorResponse>()

        assertThat(result.status.value()).isEqualTo(400)
        assertThat(result.responseBody!!.errorCode).isEqualTo("ValidationException")
        assertThat(balance().balance).isEqualByComparingTo("0.00")
    }

    @Test
    fun `recording an expense without an amount returns 400 Bad Request`() {
        val result = postExpense(emptyMap<String, Any>()).returnResult<ErrorResponse>()

        assertThat(result.status.value()).isEqualTo(400)
        assertThat(result.responseBody!!.errorCode).isEqualTo("BadRequest")
    }

    @Test
    fun `recording an expense for an unknown user returns 404 Not Found that names the user`() {
        val userId = UUID.randomUUID()

        val result = postExpense(ExpenseDto(BigDecimal("10.00")), userId).returnResult<ErrorResponse>()

        assertThat(result.status.value()).isEqualTo(404)
        assertThat(result.responseBody!!.errorCode).isEqualTo("NotFoundException")
        assertThat(result.responseBody!!.message).isEqualTo("User with ID $userId does not exist.")
    }

    @Test
    fun `getting the balance of an unknown user returns 404 Not Found that names the user`() {
        val userId = UUID.randomUUID()

        val result = getBalance(userId).returnResult<ErrorResponse>()

        assertThat(result.status.value()).isEqualTo(404)
        assertThat(result.responseBody!!.errorCode).isEqualTo("NotFoundException")
        assertThat(result.responseBody!!.message).isEqualTo("User with ID $userId does not exist.")
    }

    @Test
    fun `recording expenses concurrently adds every amount to the balance`() {
        val userId = fixtureUserId()
        val start = CountDownLatch(1)
        val executor = Executors.newFixedThreadPool(CONCURRENT_REQUESTS)
        try {
            val statuses =
                (1..CONCURRENT_REQUESTS).map {
                    executor.submit<Int> {
                        start.await()
                        postExpense(ExpenseDto(CENT), userId).statusCode()
                    }
                }
            start.countDown()

            assertThat(statuses.map { it.get(TIMEOUT_SECONDS, TimeUnit.SECONDS) }).containsOnly(200)
        } finally {
            executor.shutdownNow()
        }
        assertThat(balance().balance).isEqualByComparingTo("0.40")
    }

    private companion object {
        /**
         * The number of expenses that the concurrency test records at the same time from separate threads. Without the
         * row lock that `getByUserIdForUpdate` sets on the balance, requests lose some amounts.
         */
        const val CONCURRENT_REQUESTS = 40

        /** How long the concurrency test waits for each request. */
        const val TIMEOUT_SECONDS = 30L

        /** The amount of each expense in the concurrency test. */
        val CENT: BigDecimal = BigDecimal("0.01")
    }
}
