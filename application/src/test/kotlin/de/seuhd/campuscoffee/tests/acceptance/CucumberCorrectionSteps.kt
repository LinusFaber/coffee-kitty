package de.seuhd.campuscoffee.tests.acceptance

import de.seuhd.campuscoffee.tests.ConsumptionRequests
import io.cucumber.datatable.DataTable
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.assertj.core.api.Assertions.assertThat

/**
 * Step definitions for the acceptance scenarios in `correction.feature`.
 *
 * The When step sends the correction of FR-8 through [ConsumptionRequests.correctCoffeeCount] and keeps the status
 * code. The Then steps check that status code, read the stored coffee count with [ConsumptionRequests.coffeeCount],
 * and compare the stored corrections with [ConsumptionRequests.corrections] against a data table. A note of `""` in a
 * scenario sends no note, so that one scenario outline covers a correction without a note and a correction with a
 * blank note.
 */
class CucumberCorrectionSteps {
    private var lastStatus: Int = 0

    @When("the admin {string} corrects the coffee count of the user {string} to {int} with the note {string}")
    fun theAdminCorrectsTheCoffeeCount(
        admin: String,
        loginName: String,
        count: Int,
        note: String
    ) {
        lastStatus = ConsumptionRequests.correctCoffeeCount(admin, loginName, count, note.ifEmpty { null })
    }

    @Then("the correction succeeds")
    fun theCorrectionSucceeds() {
        assertThat(lastStatus).`as`("status of the correction").isEqualTo(200)
    }

    @Then("the correction is rejected with {int}")
    fun theCorrectionIsRejectedWith(status: Int) {
        assertThat(lastStatus).`as`("status of the correction").isEqualTo(status)
    }

    @Then("the coffee count of the user {string} is {int}")
    fun theCoffeeCountOfTheUserIs(
        loginName: String,
        count: Int
    ) {
        assertThat(ConsumptionRequests.coffeeCount(loginName)).isEqualTo(count)
    }

    @Then("the corrections of the user {string} are:")
    fun theCorrectionsOfTheUserAre(
        loginName: String,
        table: DataTable
    ) {
        val expected =
            table.asMaps().map { row ->
                ConsumptionRequests.Correction(
                    countBefore = row.getValue("countBefore")!!.toInt(),
                    countAfter = row.getValue("countAfter")!!.toInt(),
                    note = row.getValue("note")!!,
                    adminLoginName = row.getValue("adminLoginName")!!
                )
            }
        assertThat(ConsumptionRequests.corrections(loginName)).containsExactlyElementsOf(expected)
    }
}
