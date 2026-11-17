package de.seuhd.campuscoffee.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

/**
 * Domain model of a balance, which holds the amount of money that the group owes one [user]. Each user has exactly one
 * balance, which `UserService.create` creates together with the user at [ZERO_BALANCE]. Instances are immutable.
 * Recording an expense stores a copy with the raised [balance].
 *
 * The constants in the companion object are the limits that an expense amount must meet. The column
 * `balances.balance` has the same precision and scale.
 */
data class Balance(
    override val id: UUID? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null,
    val user: User,
    val balance: BigDecimal
) : DomainModel<UUID> {
    /** The limits of a balance and of an expense amount. */
    companion object {
        /** The balance of a user who has neither paid nor recorded an expense. */
        val ZERO_BALANCE: BigDecimal = BigDecimal("0.00")

        /** The number of decimal places that the column `balances.balance` stores. */
        const val SCALE = 2

        /** The largest balance that the column `balances.balance` stores. */
        val MAX_BALANCE: BigDecimal = BigDecimal("9999999999.99")
    }
}
