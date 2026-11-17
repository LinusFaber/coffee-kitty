package de.seuhd.campuscoffee.domain.implementation

import de.seuhd.campuscoffee.domain.exceptions.ValidationException
import de.seuhd.campuscoffee.domain.model.Balance
import de.seuhd.campuscoffee.domain.ports.api.BalanceService
import de.seuhd.campuscoffee.domain.ports.data.BalanceDataService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.util.UUID

/**
 * Domain implementation of [BalanceService]. [recordExpense] adds an expense in one transaction. It loads and locks
 * the balance of the user and then stores a copy with the amount added. A concurrent request for the same user waits
 * until the transaction ends and then reads the raised balance. Without the lock, two concurrent requests could both
 * read a balance of 10.00 and both store a balance of 20.00, so one expense would be lost.
 */
@Service
class BalanceServiceImpl(
    private val balanceDataService: BalanceDataService
) : BalanceService {
    override fun getByUserId(userId: UUID): Balance = balanceDataService.getByUserId(userId)

    @Transactional
    override fun recordExpense(
        userId: UUID,
        amount: BigDecimal
    ): Balance {
        requireValidAmount(amount)
        val current = balanceDataService.getByUserIdForUpdate(userId)
        val raised = current.balance + amount
        if (raised > Balance.MAX_BALANCE) {
            throw ValidationException("The balance must not exceed ${Balance.MAX_BALANCE}.")
        }
        return balanceDataService.upsert(current.copy(balance = raised))
    }

    /**
     * Checks that [amount] is greater than zero, has at most [Balance.SCALE] decimal places, and does not exceed
     * [Balance.MAX_BALANCE].
     *
     * @param amount the amount of the expense
     * @throws ValidationException if the amount fails the check
     */
    private fun requireValidAmount(amount: BigDecimal) {
        if (amount.signum() <= 0 || amount.scale() > Balance.SCALE || amount > Balance.MAX_BALANCE) {
            throw ValidationException(
                "The amount must be greater than 0.00, have at most two decimal places, and not exceed " +
                    "${Balance.MAX_BALANCE}."
            )
        }
    }
}
