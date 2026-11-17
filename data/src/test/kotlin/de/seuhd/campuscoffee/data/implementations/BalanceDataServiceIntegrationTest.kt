package de.seuhd.campuscoffee.data.implementations

import de.seuhd.campuscoffee.data.integration.AbstractDataIntegrationTest
import de.seuhd.campuscoffee.domain.exceptions.DuplicationException
import de.seuhd.campuscoffee.domain.exceptions.NotFoundException
import de.seuhd.campuscoffee.domain.model.Balance
import de.seuhd.campuscoffee.domain.model.User
import de.seuhd.campuscoffee.domain.ports.data.BalanceDataService
import de.seuhd.campuscoffee.domain.ports.data.UserDataService
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.IllegalTransactionStateException
import java.math.BigDecimal
import java.util.UUID

/**
 * Integration tests of the data adapter [BalanceDataServiceImpl] and its base class [CrudDataServiceImpl] against
 * PostgreSQL. The tests check that [CrudDataServiceImpl.upsert] assigns the id and the timestamps, that an update
 * keeps the creation time, and that a second balance for a user and an unknown user throw the expected exceptions.
 */
class BalanceDataServiceIntegrationTest : AbstractDataIntegrationTest() {
    @Autowired
    private lateinit var userDataService: UserDataService

    @Autowired
    private lateinit var balanceDataService: BalanceDataService

    private fun user(loginName: String): User =
        User(
            loginName = loginName,
            emailAddress = "$loginName@se.de",
            firstName = "First",
            lastName = "Last"
        )

    private fun storedUser(loginName: String): User = userDataService.upsert(user(loginName))

    @Test
    fun `upsert of a new balance assigns an id and both timestamps that getById reads back unchanged`() {
        val user = storedUser("alice")

        val created = balanceDataService.upsert(Balance(user = user, balance = Balance.ZERO_BALANCE))

        assertThat(created.id).isNotNull()
        assertThat(created.createdAt).isNotNull()
        assertThat(created.updatedAt).isEqualTo(created.createdAt)
        assertThat(balanceDataService.getById(requireNotNull(created.id))).isEqualTo(created)
    }

    @Test
    fun `upsert of an existing balance without timestamps keeps createdAt and stores the new balance`() {
        val user = storedUser("frank")
        val created = balanceDataService.upsert(Balance(user = user, balance = Balance.ZERO_BALANCE))

        val updated =
            balanceDataService.upsert(created.copy(balance = BigDecimal("10.00"), createdAt = null, updatedAt = null))

        assertThat(updated.id).isEqualTo(created.id)
        assertThat(updated.balance).isEqualByComparingTo("10.00")
        assertThat(updated.createdAt).isEqualTo(created.createdAt)
        assertThat(updated.updatedAt).isAfter(created.updatedAt)
        assertThat(balanceDataService.getByUserId(requireNotNull(user.id)).balance).isEqualByComparingTo("10.00")
    }

    @Test
    fun `upsert of a second balance for a user throws DuplicationException that names the user ID`() {
        val user = storedUser("erin")
        balanceDataService.upsert(Balance(user = user, balance = Balance.ZERO_BALANCE))

        assertThatThrownBy { balanceDataService.upsert(Balance(user = user, balance = BigDecimal("1.00"))) }
            .isInstanceOf(DuplicationException::class.java)
            .hasMessage("Balance with user ID '${user.id}' already exists.")
    }

    @Test
    fun `upsert of a balance for an unknown user throws NotFoundException that names the user`() {
        val unknownUser = user("nobody").copy(id = UUID(0L, 7L))

        assertThatThrownBy { balanceDataService.upsert(Balance(user = unknownUser, balance = Balance.ZERO_BALANCE)) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("User with ID 00000000-0000-0000-0000-000000000007 does not exist.")
    }

    @Test
    fun `getByUserId of an unknown user throws NotFoundException that names the user`() {
        assertThatThrownBy { balanceDataService.getByUserId(UUID(0L, 8L)) }
            .isInstanceOf(NotFoundException::class.java)
            .hasMessage("User with ID 00000000-0000-0000-0000-000000000008 does not exist.")
    }

    @Test
    fun `delete of a user cascades to their balance and the lookup throws NotFoundException`() {
        val user = storedUser("dave")
        val userId = requireNotNull(user.id)
        balanceDataService.upsert(Balance(user = user, balance = Balance.ZERO_BALANCE))

        userDataService.delete(userId)

        assertThatThrownBy { balanceDataService.getByUserId(userId) }
            .isInstanceOf(NotFoundException::class.java)
    }

    @Test
    fun `getByUserIdForUpdate outside a transaction throws IllegalTransactionStateException`() {
        val user = storedUser("otto")
        balanceDataService.upsert(Balance(user = user, balance = Balance.ZERO_BALANCE))

        assertThatThrownBy { balanceDataService.getByUserIdForUpdate(requireNotNull(user.id)) }
            .isInstanceOf(IllegalTransactionStateException::class.java)
    }
}
