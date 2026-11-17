package de.seuhd.campuscoffee.domain.implementation

import de.seuhd.campuscoffee.domain.exceptions.NotFoundException
import de.seuhd.campuscoffee.domain.exceptions.ValidationException
import de.seuhd.campuscoffee.domain.model.Balance
import de.seuhd.campuscoffee.domain.model.User
import de.seuhd.campuscoffee.domain.ports.data.BalanceDataService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID
import kotlin.reflect.jvm.javaMethod

/**
 * Unit tests for [BalanceServiceImpl] with a Mockito mock of [BalanceDataService].
 */
class BalanceServiceTest {
    private val dataService: BalanceDataService = mock()
    private val service = BalanceServiceImpl(dataService)

    private val userId: UUID = UUID(0L, 1L)

    private val user =
        User(
            id = userId,
            loginName = "max",
            emailAddress = "max@se.de",
            firstName = "Max",
            lastName = "M"
        )

    private fun balance(amount: String) = Balance(id = UUID(0L, 600L), user = user, balance = BigDecimal(amount))

    @Test
    fun `getByUserId returns the balance from the data port`() {
        whenever(dataService.getByUserId(userId)).thenReturn(balance("2.50"))

        assertThat(service.getByUserId(userId)).isEqualTo(balance("2.50"))
    }

    @Test
    fun `recordExpense adds the amount to the balance of the locked row`() {
        whenever(dataService.getByUserIdForUpdate(userId)).thenReturn(balance("2.50"))
        whenever(dataService.upsert(any())).thenAnswer { it.arguments[0] as Balance }

        val result = service.recordExpense(userId, BigDecimal("10.00"))

        assertThat(result.balance).isEqualByComparingTo("12.50")
        verify(dataService).upsert(balance("12.50"))
    }

    @Test
    fun `recordExpense is transactional with REQUIRED propagation`() {
        val recordExpense = requireNotNull(BalanceServiceImpl::recordExpense.javaMethod)

        // recordExpense must read the balance and store the raised balance in one transaction, because the row lock
        // lasts only until the transaction ends
        assertThat(recordExpense.getAnnotation(Transactional::class.java)?.propagation)
            .isEqualTo(Propagation.REQUIRED)
    }

    @ParameterizedTest
    @ValueSource(strings = ["0", "0.00", "-1.00", "1.999", "10000000000.00"])
    fun `recordExpense with an invalid amount throws ValidationException and writes nothing`(amount: String) {
        assertThrows<ValidationException> { service.recordExpense(userId, BigDecimal(amount)) }
        verify(dataService, never()).getByUserIdForUpdate(any())
        verify(dataService, never()).upsert(any())
    }

    @Test
    fun `recordExpense that would raise the balance above the maximum throws ValidationException`() {
        whenever(dataService.getByUserIdForUpdate(userId)).thenReturn(balance("9999999999.98"))

        assertThrows<ValidationException> { service.recordExpense(userId, BigDecimal("0.02")) }
        verify(dataService, never()).upsert(any())
    }

    @Test
    fun `recordExpense for an unknown user throws NotFoundException and writes nothing`() {
        whenever(dataService.getByUserIdForUpdate(userId))
            .thenThrow(NotFoundException(User::class.java, userId))

        assertThrows<NotFoundException> { service.recordExpense(userId, BigDecimal("10.00")) }
        verify(dataService, never()).upsert(any())
    }
}
