package de.seuhd.campuscoffee.data.mapper

import de.seuhd.campuscoffee.data.persistence.entities.BalanceEntity
import de.seuhd.campuscoffee.domain.model.Balance
import org.mapstruct.Mapper
import org.mapstruct.Mapping
import org.mapstruct.MappingTarget

/**
 * MapStruct mapper between [Balance] domain objects and [BalanceEntity] JPA entities. [toEntity] and [updateEntity]
 * set [BalanceEntity.user] to the stored user, which [EntityReferenceResolver] loads by the id of [Balance.user].
 * Saving a balance then leaves the user row unchanged (see [EntityReferenceResolver]). [fromEntity] maps the user with
 * all fields through [UserEntityMapper]. [updateEntity] does not copy the id or the timestamps (see
 * [EntityMapper.updateEntity]).
 */
@Mapper(componentModel = "spring", uses = [UserEntityMapper::class, EntityReferenceResolver::class])
interface BalanceEntityMapper : EntityMapper<Balance, BalanceEntity> {
    @Mapping(target = "user", source = "user.id")
    override fun toEntity(source: Balance): BalanceEntity

    @Mapping(target = "user", source = "user.id")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    override fun updateEntity(
        source: Balance,
        @MappingTarget target: BalanceEntity
    )
}
