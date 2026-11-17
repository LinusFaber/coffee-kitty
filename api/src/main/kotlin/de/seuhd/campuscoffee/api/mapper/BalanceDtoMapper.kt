package de.seuhd.campuscoffee.api.mapper

import de.seuhd.campuscoffee.api.dtos.BalanceDto
import de.seuhd.campuscoffee.domain.model.Balance
import org.mapstruct.Mapper
import org.mapstruct.Mapping

/**
 * MapStruct mapper from a [Balance] to a [BalanceDto]. The mapper converts in one direction and does not extend
 * [DtoMapper]. The opposite direction is not needed, because the controller passes the amount from the request body
 * (`ExpenseDto`) to the domain as a number.
 */
@Mapper(componentModel = "spring")
interface BalanceDtoMapper {
    /**
     * Converts [balance] to a [BalanceDto]. The `@Mapping` copies the login name from the user of the balance.
     *
     * @param balance the balance to convert
     */
    @Mapping(target = "loginName", source = "user.loginName")
    fun toDto(balance: Balance): BalanceDto
}
