/*
 * Copyright (c) 2022 Frederik Enste <frederik@fenste.de>.
 *
 * Licensed under the GNU General Public License, Version 3 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.gnu.org/licenses/gpl-3.0.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.fenste.ms.address.application.controllers

import de.fenste.ms.address.application.dtos.requests.CreateStateDto
import de.fenste.ms.address.application.dtos.requests.UpdateStateDto
import de.fenste.ms.address.application.dtos.responses.CountryDto
import de.fenste.ms.address.application.dtos.responses.StateDto
import de.fenste.ms.address.domain.model.Country
import de.fenste.ms.address.test.SampleData
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
class StateControllerTest(
    @Autowired private val controller: StateController,
) {

    @BeforeTest
    fun `set up`() {
        SampleData.reset()
    }

    @Test
    fun `test list on sample data`() {
        val expected = SampleData.states.sortedBy { s -> s.id.value.toString() }.map { s -> StateDto(s) }
        val actual = controller.states()

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `test list on sample data with options`() {
        val expected = SampleData.states
            .sortedBy { s -> s.id.value.toString() }
            .drop(2)
            .take(1)
            .map { s -> StateDto(s) }
        val actual = controller.states(
            offset = 2,
            limit = 1,
        )

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `test find by id on sample data`() {
        val expected = SampleData.states.random().let { s -> StateDto(s) }
        val actual = controller.state(id = expected.id)

        transaction { assertEquals(expected, actual) }
    }

    @Test
    fun `test find by id on non existing sample data`() {
        val actual = controller.state(id = UUID.randomUUID().toString())

        assertNull(actual)
    }

    @Test
    fun `test create`() {
        val country = SampleData.countries.random()
        val create = CreateStateDto(
            name = "Name",
            country = country.id.value.toString(),
        )

        val actual = controller.createState(
            state = create,
        )

        assertNotNull(actual.id)
        assertEquals(create.name, actual.name)
        transaction { assertEquals(CountryDto(country), actual.country) }
    }

    @Test
    fun `test update all`(): Unit = transaction {
        val sampleId = SampleData.states.first().id.value.toString()
        val country = SampleData.countries.last()
        val update = UpdateStateDto(
            id = sampleId,
            name = "Name",
            country = country.id.value.toString(),
        )

        val actual = controller.updateState(
            state = update,
        )

        assertNotNull(actual.id)
        assertEquals(update.name, actual.name)
        transaction { assertEquals(CountryDto(country), actual.country) }
    }

    @Test
    fun `test update nothing`(): Unit = transaction {
        val expected = SampleData.states.random().let { s -> StateDto(s) }
        val update = UpdateStateDto(
            id = expected.id,
        )
        val actual = controller.updateState(
            state = update,
        )

        transaction { assertEquals(expected, actual) }
    }

    @Test
    @Ignore // TODO allow cascade deletion?
    fun `test delete`(): Unit = transaction {
        val sampleId = SampleData.states.random().id.value.toString()

        controller.deleteState(sampleId)

        assertNull(Country.findById(UUID.fromString(sampleId)))
    }
}
