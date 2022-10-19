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

package de.fenste.ms.address.application.services

import de.fenste.ms.address.application.dtos.requests.CreateStateDto
import de.fenste.ms.address.application.dtos.requests.UpdateStateDto
import de.fenste.ms.address.application.dtos.responses.CountryDto
import de.fenste.ms.address.application.dtos.responses.StateDto
import de.fenste.ms.address.domain.model.State
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
class StateServiceTest(
    @Autowired private val service: StateService,
) {

    @BeforeTest
    fun `set up`() {
        SampleData.reset()
    }

    @Test
    fun `test list on sample data`() {
        val expected = SampleData.states.sortedBy { s -> s.id.value.toString() }.map { s -> StateDto(s) }
        val actual = service.list()

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `test list on sample data with options`() {
        val expected = SampleData.states
            .sortedBy { s -> s.id.value.toString() }
            .drop(2)
            .take(1)
            .map { s -> StateDto(s) }
        val actual = service.list(
            offset = 2,
            limit = 1,
        )

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `test list on no data`() {
        SampleData.clear()
        val list = service.list()

        assertNull(list)
    }

    @Test
    fun `test find by id on sample data`() {
        val expected = SampleData.states.random().let { s -> StateDto(s) }
        val actual = service.find(id = UUID.fromString(expected.id))

        transaction { assertEquals(expected, actual) }
    }

    @Test
    fun `test find by id on non existing sample data`() {
        val actual = service.find(id = UUID.randomUUID())

        assertNull(actual)
    }

    @Test
    fun `test create`() {
        val name = "Name"
        val country = SampleData.countries.random()

        val create = CreateStateDto(
            name = name,
            country = country.id.value.toString(),
        )

        val actual = service.create(
            create = create,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        transaction { assertEquals(CountryDto(country), actual.country) }
    }

    @Test
    fun `test update all`() {
        val state = SampleData.states.random()
        val name = "Name"
        val country = transaction { SampleData.countries.filterNot { c -> c.states.contains(state) }.random() }

        val update = UpdateStateDto(
            id = state.id.value.toString(),
            name = name,
            country = country.id.value.toString(),
        )

        val actual = service.update(
            update = update,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        transaction { assertEquals(CountryDto(country), actual.country) }
    }

    @Test
    fun `test update nothing`() {
        val expected = SampleData.states.random().let { s -> StateDto(s) }

        val update = UpdateStateDto(
            id = expected.id,
        )

        val actual = service.update(
            update = update,
        )

        transaction { assertEquals(expected, actual) }
    }

    @Test
    @Ignore // TODO allow cascade deletion?
    fun `test delete`() {
        val id = SampleData.states.random().id.value

        service.delete(id)

        transaction { assertNull(State.findById(id)) }
    }
}
