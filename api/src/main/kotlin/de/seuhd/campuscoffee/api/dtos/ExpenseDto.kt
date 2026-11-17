package de.seuhd.campuscoffee.api.dtos

import jakarta.validation.constraints.NotNull
import java.math.BigDecimal

/**
 * Request body of `POST /api/users/{userId}/expenses`, which records an expense with an [amount] in euro. Bean
 * Validation rejects a body without an [amount] with a 400 and the error code `BadRequest`. The domain rejects an
 * amount that is not greater than 0.00, has more than two decimal places, or exceeds the maximum balance with a
 * `ValidationException` (400).
 */
data class ExpenseDto(
    @field:NotNull
    val amount: BigDecimal?
)
