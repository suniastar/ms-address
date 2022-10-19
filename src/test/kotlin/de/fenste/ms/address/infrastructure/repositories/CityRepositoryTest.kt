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

package de.fenste.ms.address.infrastructure.repositories

import de.fenste.ms.address.domain.model.City
import de.fenste.ms.address.domain.model.Country
import de.fenste.ms.address.infrastructure.tables.CityTable
import de.fenste.ms.address.test.SampleData
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
class CityRepositoryTest(
    @Autowired private val repository: CityRepository,
) {

    @BeforeTest
    fun `set up`() {
        SampleData.reset()
    }

    @Test
    fun `test list on sample data`(): Unit = transaction {
        val expected = SampleData.cities.sortedBy { c -> c.id.value.toString() }
        val actual = repository.list()

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test list on sample data with options`(): Unit = transaction {
        val expected = SampleData.cities
            .sortedBy { c -> c.name }
            .drop(2)
            .take(1)
        val actual = repository.list(
            order = arrayOf(CityTable.name to SortOrder.ASC),
            offset = 2,
            limit = 1,
        )

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test list on no data`(): Unit = transaction {
        SampleData.clear()
        val list = repository.list()

        assertTrue(list.empty())
    }

    @Test
    fun `test find by id on sample data`(): Unit = transaction {
        val expected = SampleData.cities.random()
        val actual = repository.find(id = expected.id.value)

        assertEquals(expected, actual)
    }

    @Test
    fun `test find by id on no data`(): Unit = transaction {
        SampleData.clear()
        val actual = repository.find(id = UUID.randomUUID())

        assertNull(actual)
    }

    @Test
    fun `test find by id on non existing sample data`(): Unit = transaction {
        val actual = repository.find(id = UUID.randomUUID())

        assertNull(actual)
    }

    @Test
    fun `test create`(): Unit = transaction {
        val name = "Name"
        val country = SampleData.countries.filter { c -> c.states.empty() }.random()

        val actual = repository.create(
            name = name,
            countryId = country.id.value,
            stateId = null,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        assertEquals(country, actual.country)
        assertNull(actual.state)
    }

    @Test
    fun `test create all`(): Unit = transaction {
        val name = "Name"
        val country = SampleData.countries.filterNot { c -> c.states.empty() }.random()
        val state = country.states.toList().random()

        val actual = repository.create(
            name = name,
            countryId = country.id.value,
            stateId = state.id.value,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        assertEquals(country, actual.country)
        assertEquals(state, actual.state)
    }

    @Test
    fun `test create existing`(): Unit = transaction {
        val country = SampleData.countries.filterNot { c -> c.states.empty() }.random()
        val state = country.states.toList().random()
        val name = state.cities.first().name

        assertFailsWith<IllegalArgumentException> {
            repository.create(
                name = name,
                countryId = country.id.value,
                stateId = state.id.value,
            )
        }
    }

    @Test
    fun `test create state does not belong to country`(): Unit = transaction {
        val name = "Name"
        val country = SampleData.countries.random()
        val state = SampleData.states.filterNot { s -> country.states.contains(s) }.random()

        assertFailsWith<IllegalArgumentException> {
            repository.create(
                name = name,
                countryId = country.id.value,
                stateId = state.id.value,
            )
        }
    }

    @Test
    fun `test create non existing country`(): Unit = transaction {
        val name = "Name"
        val countryId = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repository.create(
                name = name,
                countryId = countryId,
                stateId = null,
            )
        }
    }

    @Test
    fun `test create non existing state`(): Unit = transaction {
        val name = "Name"
        val countryId = SampleData.countries.random().id.value
        val stateId = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repository.create(
                name = name,
                countryId = countryId,
                stateId = stateId,
            )
        }
    }

    @Test
    fun `test update name`(): Unit = transaction {
        val city = SampleData.cities.random()
        val name = "Name"

        val actual = repository.update(
            id = city.id.value,
            name = name,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
    }

    @Test
    fun `test update country`(): Unit = transaction {
        val city = SampleData.cities.filter { c -> c.country.states.empty() }.random()
        val countryId = Country.new {
            alpha2 = "XX"
            alpha3 = "XXX"
            name = "Stateless Nation"
            localizedName = "Stateless Nation"
        }.id.value

        val actual = repository.update(
            id = city.id.value,
            countryId = countryId,
        )

        assertNotNull(actual)
        assertEquals(countryId, actual.country.id.value)
    }

    @Test
    fun `test update state`(): Unit = transaction {
        val city = SampleData.cities.filter { c -> c.country.states.count() >= 2 }.random()
        val stateId = city.country.states.filterNot { s -> s == city.state }.random().id.value

        val actual = repository.update(
            id = city.id.value,
            stateId = stateId,
        )

        assertNotNull(actual)
        assertNotNull(actual.state)
        assertEquals(stateId, actual.state?.id?.value)
    }

    @Test
    fun `test update remove state`(): Unit = transaction {
        val city = SampleData.cities.filterNot { c -> c.state == null }.random()

        val actual = repository.update(
            id = city.id.value,
            removeState = true,
        )

        assertNotNull(actual)
        assertNull(actual.state)
    }

    @Test
    fun `test update country and state`(): Unit = transaction {
        val city = SampleData.cities.filterNot { c -> c.state == null }.random()
        val country = SampleData.countries.filterNot { c -> c == city.country || c.states.empty() }.random()
        val state = country.states.toList().random()

        val actual = repository.update(
            id = city.id.value,
            countryId = country.id.value,
            stateId = state.id.value,
        )

        assertNotNull(actual)
        assertEquals(country.id.value, actual.country.id.value)
        assertNotNull(actual.state)
        assertEquals(state.id.value, actual.state?.id?.value)
    }

    @Test
    fun `test update all`(): Unit = transaction {
        val city = SampleData.cities.filterNot { c -> c.state == null }.random()
        val name = "Name"
        val country = SampleData.countries.filterNot { c -> c == city.country || c.states.empty() }.random()
        val state = country.states.toList().random()

        val actual = repository.update(
            id = city.id.value,
            name = name,
            countryId = country.id.value,
            stateId = state.id.value,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        assertEquals(country.id.value, actual.country.id.value)
        assertNotNull(actual.state)
        assertEquals(state.id.value, actual.state?.id?.value)
    }

    @Test
    fun `test update all remove state`(): Unit = transaction {
        val city = SampleData.cities.filterNot { c -> c.state == null }.random()
        val name = "Name"
        val country = SampleData.countries.filterNot { c -> c != city.country && c.states.empty() }.random()

        val actual = repository.update(
            id = city.id.value,
            name = name,
            countryId = country.id.value,
            removeState = true,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        assertEquals(country.id.value, actual.country.id.value)
        assertNull(actual.state)
    }

    @Test
    fun `test update all to same`(): Unit = transaction {
        val city = SampleData.cities.filterNot { c -> c.state == null }.random()
        val name = city.name
        val countryId = city.country.id.value
        val stateId = city.state?.id?.value

        val actual = repository.update(
            id = city.id.value,
            name = name,
            countryId = countryId,
            stateId = stateId,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        assertEquals(countryId, actual.country.id.value)
        assertNotNull(actual.state)
        assertEquals(stateId, actual.state?.id?.value)
    }

    @Test
    fun `test update nothing`(): Unit = transaction {
        val expected = SampleData.cities.random()
        val actual = repository.update(id = expected.id.value)

        assertEquals(expected, actual)
    }

    @Test
    fun `test update on not existing`(): Unit = transaction {
        val id = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = id,
                name = "doesn't matter",
                countryId = UUID.randomUUID(),
            )
        }
    }

    @Test
    fun `test update name to existing`(): Unit = transaction {
        val city = SampleData.cities.filter { c -> (c.state?.cities?.count() ?: 0) >= 2 }.random()
        val name = city.state!!.cities.filterNot { c -> c == city }.random().name

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = city.id.value,
                name = name,
            )
        }
    }

    @Test
    fun `test update country to not existing`(): Unit = transaction {
        val city = SampleData.cities.first()
        val countryId = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = city.id.value,
                countryId = countryId,
            )
        }
    }

    @Test
    fun `test update state to not existing`(): Unit = transaction {
        val city = SampleData.cities.first()
        val stateId = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = city.id.value,
                stateId = stateId,
            )
        }
    }

    @Test
    fun `test update country where state does not belong to country`(): Unit = transaction {
        val city = SampleData.cities.filterNot { c -> c.state == null }.random()
        val country = SampleData.countries.filterNot { c -> c == city.country }.random()

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = city.id.value,
                countryId = country.id.value,
            )
        }
    }

    @Test
    fun `test update state where state does not belong to country`(): Unit = transaction {
        val city = SampleData.cities.random()
        val state = SampleData.states.filterNot { s -> s.country == city.country || s == city.state }.random()

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = city.id.value,
                stateId = state.id.value,
            )
        }
    }

    @Test
    @Ignore // TODO allow cascade deletion?
    fun `test delete`(): Unit = transaction {
        val id = SampleData.cities.random().id.value

        repository.delete(id)

        assertNull(City.findById(id))
    }

    @Test
    fun `test delete not existing`(): Unit = transaction {
        val id = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repository.delete(id)
        }
    }
}
