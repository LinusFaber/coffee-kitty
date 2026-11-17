package de.seuhd.campuscoffee.domain.ports.api

import de.seuhd.campuscoffee.domain.exceptions.NotFoundException
import de.seuhd.campuscoffee.domain.exceptions.ValidationException
import de.seuhd.campuscoffee.domain.model.Balance
import java.math.BigDecimal
import java.util.UUID

/**
 * API port for the balances. The controllers call it to read a user's balance and to record an expense.
 * `BalanceServiceImpl` in the domain module implements it. [UserService.create] creates the balance of a new user at
 * zero.
 */
interface BalanceService {
    /**
     * Returns the balance of the user with [userId].
     *
     * @param userId the id of the user
     * @throws NotFoundException if no user has [userId]
     */
    fun getByUserId(userId: UUID): Balance

    /**
     * Adds [amount] to the balance of the user with [userId] and returns the updated balance. The domain accepts only
     * an amount greater than zero with at most two decimal places, because the group owes the money of an expense back
     * to the user.
     *
     * @param userId the id of the user
     * @param amount the amount of the expense, which must be greater than zero
     * @throws ValidationException if [amount] is not positive, has more than two decimal places, or would raise the
     *   balance above the maximum
     * @throws NotFoundException if no user has [userId]
     */
    fun recordExpense(
        userId: UUID,
        amount: BigDecimal
    ): Balance
}
