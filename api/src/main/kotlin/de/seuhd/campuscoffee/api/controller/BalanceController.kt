package de.seuhd.campuscoffee.api.controller

import de.seuhd.campuscoffee.api.dtos.BalanceDto
import de.seuhd.campuscoffee.api.dtos.ExpenseDto
import de.seuhd.campuscoffee.api.mapper.BalanceDtoMapper
import de.seuhd.campuscoffee.domain.ports.api.BalanceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID
import io.swagger.v3.oas.annotations.parameters.RequestBody as ApiRequestBody

/**
 * Controller under `/api/users/{userId}` that reads the balance of a user and records an expense for the user. Both
 * endpoints return the login name and the balance of the user as a [BalanceDto].
 */
@Tag(name = "Balance", description = "Reading a user's balance and recording an expense.")
@RestController
@RequestMapping("/users/{userId}")
class BalanceController(
    private val balanceService: BalanceService,
    private val balanceDtoMapper: BalanceDtoMapper
) {
    /**
     * Returns the balance of the user with [userId].
     *
     * @param userId the id of the user
     */
    @Operation(summary = "Get a user's balance.")
    @GetMapping("/balance")
    fun get(
        @Parameter(description = "Unique identifier of the user.", required = true)
        @PathVariable userId: UUID
    ): ResponseEntity<BalanceDto> = ResponseEntity.ok(balanceDtoMapper.toDto(balanceService.getByUserId(userId)))

    /**
     * Adds the amount in [dto] to the balance of the user with [userId] and returns the new balance. The domain accepts
     * only an amount greater than 0.00 with at most two decimal places and rejects any other amount with a
     * `ValidationException` (400).
     *
     * @param userId the id of the user
     * @param dto    the request body with the amount of the expense
     */
    @Operation(summary = "Record an expense for a user.")
    @PostMapping("/expenses")
    fun recordExpense(
        @Parameter(description = "Unique identifier of the user.", required = true)
        @PathVariable userId: UUID,
        @ApiRequestBody(description = "The amount of the expense in euro.", required = true)
        @RequestBody
        @Valid dto: ExpenseDto
    ): ResponseEntity<BalanceDto> {
        val updated = balanceService.recordExpense(userId, requireNotNull(dto.amount))
        return ResponseEntity.ok(balanceDtoMapper.toDto(updated))
    }
}
