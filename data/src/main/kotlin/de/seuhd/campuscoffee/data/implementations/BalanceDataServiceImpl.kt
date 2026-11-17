package de.seuhd.campuscoffee.data.implementations

import de.seuhd.campuscoffee.data.mapper.BalanceEntityMapper
import de.seuhd.campuscoffee.data.persistence.entities.BalanceEntity
import de.seuhd.campuscoffee.data.persistence.repositories.BalanceRepository
import de.seuhd.campuscoffee.domain.exceptions.NotFoundException
import de.seuhd.campuscoffee.domain.model.Balance
import de.seuhd.campuscoffee.domain.model.User
import de.seuhd.campuscoffee.domain.ports.data.BalanceDataService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * Data adapter that implements the [BalanceDataService] port on the `balances` table. The business rules are in the
 * domain services. Every user has exactly one balance. A lookup that finds no balance therefore throws a
 * [NotFoundException] for the user. [upsert] throws a `DuplicationException` for a second balance of a user (see
 * [CrudDataServiceImpl]).
 */
@Service
class BalanceDataServiceImpl(
    repository: BalanceRepository,
    entityMapper: BalanceEntityMapper
) : CrudDataServiceImpl<Balance, BalanceEntity, BalanceRepository, UUID>(
        repository,
        entityMapper,
        Balance::class.java
    ),
    BalanceDataService {
    override fun getByUserId(userId: UUID): Balance = toDomainOrThrow(repository.findByUserId(userId), userId)

    /**
     * Returns the balance of the user with [userId] and locks the balance row until the transaction ends. With
     * propagation `MANDATORY`, a call outside a transaction throws an `IllegalTransactionStateException`. Without a
     * transaction, the lock would already end with the query, and the caller would store the new balance without the
     * lock.
     *
     * @throws NotFoundException if no user has [userId]
     */
    @Transactional(propagation = Propagation.MANDATORY)
    override fun getByUserIdForUpdate(userId: UUID): Balance =
        toDomainOrThrow(repository.findForUpdateByUserId(userId), userId)

    /**
     * Maps [entity] to its domain object.
     *
     * @throws NotFoundException for the user with [userId] if [entity] is null
     */
    private fun toDomainOrThrow(
        entity: BalanceEntity?,
        userId: UUID
    ): Balance = entity?.let { mapper.fromEntity(it) } ?: throw NotFoundException(User::class.java, userId)
}
