package de.seuhd.campuscoffee.data.persistence.entities

import jakarta.persistence.Column
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.math.BigDecimal

/**
 * JPA entity of a balance, which holds a [user] and the amount of money that the group owes that user. The column
 * `balance` stores at most ten digits before and two digits after the decimal point. The unique constraint
 * [USER_UNIQUE_CONSTRAINT] allows one row per user.
 */
@jakarta.persistence.Entity
@Table(name = "balances")
class BalanceEntity : Entity() {
    @field:ManyToOne
    @field:JoinColumn(name = "user_id", nullable = false)
    var user: UserEntity? = null

    @field:Column(name = "balance", precision = 12, scale = 2)
    var balance: BigDecimal? = null

    companion object {
        /** Name of the unique constraint on `user_id`. */
        const val USER_UNIQUE_CONSTRAINT = "uq_balances_user"
    }
}
