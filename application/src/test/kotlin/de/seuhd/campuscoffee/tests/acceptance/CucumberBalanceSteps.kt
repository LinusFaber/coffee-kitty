package de.seuhd.campuscoffee.tests.acceptance

import de.seuhd.campuscoffee.api.dtos.BalanceDto
import de.seuhd.campuscoffee.api.dtos.ExpenseDto
import de.seuhd.campuscoffee.api.dtos.UserDto
import de.seuhd.campuscoffee.api.exceptions.ErrorResponse
import de.seuhd.campuscoffee.domain.model.persistedId
import de.seuhd.campuscoffee.tests.SystemTestUtils.client
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.client.EntityExchangeResult
import org.springframework.test.web.servlet.client.returnResult
import tools.jackson.databind.ObjectMapper
import java.math.BigDecimal
import java.util.UUID

/**
 * Step definitions for the acceptance scenarios in `balance.feature`. The Given step finds the id of the user in the
 * list from `GET /api/users`. The When step records an expense through `POST /api/users/{userId}/expenses` and keeps
 * the latest response. The Then steps check the status and the balance of that response.
 */
class CucumberBalanceSteps(
    private val objectMapper: ObjectMapper
) {
    private lateinit var userId: UUID
    private lateinit var lastResult: EntityExchangeResult<ByteArray>

    @Given("the user with login name {string}")
    fun theUserWithLoginName(loginName: String) {
        val users =
            client()
                .get()
                .uri("/api/users")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .returnResult<Array<UserDto>>()
                .responseBody!!
        userId = users.single { it.loginName == loginName }.persistedId
    }

    @When("the user records an expense of {bigdecimal} euro")
    fun theUserRecordsAnExpenseOf(amount: BigDecimal) {
        lastResult =
            client()
                .post()
                .uri("/api/users/{userId}/expenses", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ExpenseDto(amount))
                .exchange()
                .returnResult<ByteArray>()
    }

    @Then("the request succeeds and the balance is {bigdecimal} euro")
    fun theRequestSucceedsAndTheBalanceIs(balance: BigDecimal) {
        assertThat(lastResult.status.value()).isEqualTo(200)
        val dto = objectMapper.readValue(lastResult.responseBody, BalanceDto::class.java)
        assertThat(dto.balance).isEqualByComparingTo(balance)
    }

    @Then("the request fails with {int} and the balance stays {bigdecimal} euro")
    fun theRequestFailsAndTheBalanceStays(
        status: Int,
        balance: BigDecimal
    ) {
        assertThat(lastResult.status.value()).isEqualTo(status)
        val error = objectMapper.readValue(lastResult.responseBody, ErrorResponse::class.java)
        assertThat(error.errorCode).isEqualTo("ValidationException")
        val current =
            client()
                .get()
                .uri("/api/users/{userId}/balance", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .returnResult<BalanceDto>()
                .responseBody!!
        assertThat(current.balance).isEqualByComparingTo(balance)
    }
}
