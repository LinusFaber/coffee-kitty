package de.seuhd.campuscoffee.api.dtos

import java.math.BigDecimal

/**
 * Response body of the endpoints under `/api/users/{userId}/balance` and `/api/users/{userId}/expenses`, with the
 * [loginName] of the user and the [balance] in euro. The class does not implement [Dto], because it has no id.
 */
data class BalanceDto(
    val loginName: String,
    val balance: BigDecimal
)
