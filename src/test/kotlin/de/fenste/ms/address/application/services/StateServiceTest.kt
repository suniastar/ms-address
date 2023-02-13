/*
 * Copyright (c) 2023 Frederik Enste <frederik@fenste.de>.
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

import de.fenste.ms.address.application.dtos.CityDto
import de.fenste.ms.address.application.dtos.CountryDto
import de.fenste.ms.address.application.dtos.StateDto
import de.fenste.ms.address.application.dtos.StateInputDto
import de.fenste.ms.address.config.SampleDataConfig
import de.fenste.ms.address.domain.model.State
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("sample")
class StateServiceTest(
    @Autowired private val sampleData: SampleDataConfig,
    @Autowired private val service: StateService,
) {

    @BeforeTest
    fun `set up`() {
        sampleData.reset()
    }

    @Test
    fun `test count`(): Unit = transaction {
        val expected = sampleData.states.count()
        val actual = service.count()

        assertEquals(expected, actual)
    }

    @Test
    fun `test find by id on sample data`() {
        val expected = sampleData.states.random().let { s -> StateDto(s) }
        val actual = service.find(id = expected.id)

        transaction { assertEquals(expected, actual) }
    }

    @Test
    fun `test find by id on non existing sample data`() {
        val actual = service.find(id = UUID.randomUUID())

        assertNull(actual)
    }

    @Test
    fun `test list on sample data`() {
        val expected = sampleData.states
            .sortedBy { s -> s.id.value.toString() }
            .map { s -> StateDto(s) }
        val actual = service.list()

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `test list on sample data with options`() {
        val expected = sampleData.states
            .sortedWith(compareBy({ s -> s.name }, { s -> s.id.value.toString() }))
            .drop(1 * 2)
            .take(2)
            .map { s -> StateDto(s) }
        val actual = service.list(
            sort = "name,asc",
            page = 1,
            size = 2,
        )

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `test list on no data`() {
        sampleData.clear()
        val list = service.list()

        assertNotNull(list)
        assertTrue(list.isEmpty())
    }

    @Test
    fun `test get country on sample data`() {
        val state = transaction { sampleData.states.random() }
        val expected = transaction { CountryDto(state.country) }

        transaction {
            val actual = service.getCountry(state.id.value)

            assertEquals(expected, actual)
        }
    }

    @Test
    fun `test list cities on sample data`() {
        val state = transaction { sampleData.states.filterNot { s -> s.cities.empty() }.random() }
        val expected = transaction {
            state
                .cities
                .sortedBy { c -> c.id.value.toString() }
                .map { c -> CityDto(c) }
        }

        transaction {
            val actual = service.listCities(state.id.value)

            assertContentEquals(expected, actual)
        }
    }

    @Test
    fun `test list cities on sample data with options`() {
        val state = transaction { sampleData.states.filterNot { s -> s.cities.empty() }.random() }
        val expected = transaction {
            state
                .cities
                .sortedWith(compareBy({ c -> c.name }, { c -> c.id.value.toString() }))
                .drop(1 * 2)
                .take(2)
                .map { c -> CityDto(c) }
        }

        transaction {
            val actual = service.listCities(
                id = state.id.value,
                sort = "name,asc",
                page = 1,
                size = 2,
            )

            assertContentEquals(expected, actual)
        }
    }

    @Test
    fun `test create`() {
        val name = "Name"
        val country = sampleData.countries.random()

        val create = StateInputDto(
            name = name,
            country = country.id.value,
        )

        val actual = service.create(
            state = create,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        transaction {
            val created = State.findById(actual.id)
            assertNotNull(created)
        }
    }

    @Test
    fun `test update all`() {
        val state = sampleData.states.random()
        val name = "Name"
        val country = transaction { sampleData.countries.filterNot { c -> c.states.contains(state) }.random() }

        val update = StateInputDto(
            name = name,
            country = country.id.value,
        )

        val actual = service.update(
            id = state.id.value,
            state = update,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        transaction {
            val updated = State.findById(actual.id)
            assertNotNull(updated)
        }
    }

    @Test
    fun `test delete`() {
        val id = sampleData.states.random().id.value

        transaction { assertNotNull(State.findById(id)) }

        service.delete(id)

        transaction { assertNull(State.findById(id)) }
    }
}
