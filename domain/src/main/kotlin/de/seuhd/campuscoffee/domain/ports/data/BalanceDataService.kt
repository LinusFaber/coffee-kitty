package de.seuhd.campuscoffee.domain.ports.data

import de.seuhd.campuscoffee.domain.exceptions.NotFoundException
import de.seuhd.campuscoffee.domain.model.Balance
import java.util.UUID

/**
 * Data port for the balances, with two lookups by user in addition to the operations of [CrudDataService].
 * `BalanceDataServiceImpl` in the data module implements it. [getAll] returns the balances in the order in which they
 * were created.
 */
interface BalanceDataService : CrudDataService<Balance, UUID> {
    /**
     * Returns the balance of the user with [userId].
     *
     * @param userId the id of the user
     * @throws NotFoundException if no user has [userId]
     */
    fun getByUserId(userId: UUID): Balance

    /**
     * Returns the balance of the user with [userId] and locks the balance row until the current transaction ends. A
     * concurrent call for the same user waits for the lock and then reads the current balance. Call this method only
     * inside a transaction. Outside a transaction, the data adapter throws an `IllegalTransactionStateException`.
     *
     * @param userId the id of the user
     * @throws NotFoundException if no user has [userId]
     */
    fun getByUserIdForUpdate(userId: UUID): Balance
}
