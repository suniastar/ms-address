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

package de.fenste.ms.address.infrastructure.repositories

import de.fenste.ms.address.config.SampleDataConfig
import de.fenste.ms.address.domain.model.City
import de.fenste.ms.address.domain.model.Country
import de.fenste.ms.address.infrastructure.tables.CityTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("sample")
class CityRepositoryTest(
    @Autowired private val sampleData: SampleDataConfig,
    @Autowired private val repository: CityRepository,
) {

    @BeforeTest
    fun `set up`() {
        sampleData.reset()
    }

    @Test
    fun `test count`(): Unit = transaction {
        val expected = sampleData.cities.count()
        val actual = repository.count()

        assertEquals(expected, actual)
    }

    @Test
    fun `test list on sample data`(): Unit = transaction {
        val expected = sampleData.cities
            .sortedBy { c -> c.id.value.toString() }
        val actual = repository.list()

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test list on sample data with size`(): Unit = transaction {
        val expected = sampleData.cities
            .sortedWith(compareBy({ c -> c.name }, { c -> c.id }))
            .take(2)
        val actual = repository.list(
            order = arrayOf(CityTable.name to SortOrder.ASC),
            size = 2,
        )

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test list on sample data with options`(): Unit = transaction {
        val expected = sampleData.cities
            .sortedWith(compareBy({ c -> c.name }, { c -> c.id }))
            .drop(1 * 2)
            .take(2)
        val actual = repository.list(
            order = arrayOf(CityTable.name to SortOrder.ASC),
            page = 1,
            size = 2,
        )

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test list on no data`(): Unit = transaction {
        sampleData.clear()
        val list = repository.list()

        assertTrue(list.empty())
    }

    @Test
    fun `test find by id on sample data`(): Unit = transaction {
        val expected = sampleData.cities.random()
        val actual = repository.find(id = expected.id.value)

        assertEquals(expected, actual)
    }

    @Test
    fun `test find by id on no data`(): Unit = transaction {
        sampleData.clear()
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
        val country = sampleData.countries.filter { c -> c.states.empty() }.random()

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
        val country = sampleData.countries.filterNot { c -> c.states.empty() }.random()
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
        val country = sampleData.countries.filterNot { c -> c.states.empty() }.random()
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
        val country = sampleData.countries.random()
        val state = sampleData.states.filterNot { s -> country.states.contains(s) }.random()

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
        val countryId = sampleData.countries.random().id.value
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
        val city = sampleData.cities.random()
        val name = "Name"
        val country = city.country
        val state = city.state

        val actual = repository.update(
            id = city.id.value,
            name = name,
            countryId = country.id.value,
            stateId = state?.id?.value,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
    }

    @Test
    fun `test update country`(): Unit = transaction {
        val city = sampleData.cities.filter { c -> c.country.states.empty() }.random()
        val name = city.name
        val country = Country.new {
            this.alpha2 = "XX"
            this.alpha3 = "XXX"
            this.name = "Stateless Nation"
            this.localizedName = "Stateless Nation"
        }

        val actual = repository.update(
            id = city.id.value,
            name = name,
            countryId = country.id.value,
            stateId = null,
        )

        assertNotNull(actual)
        assertEquals(country, actual.country)
    }

    @Test
    fun `test update state`(): Unit = transaction {
        val city = sampleData.cities.filter { c -> c.country.states.count() >= 2 }.random()
        val name = city.name
        val country = city.country
        val state = city.country.states.filterNot { s -> s == city.state }.random()

        val actual = repository.update(
            id = city.id.value,
            name = name,
            countryId = country.id.value,
            stateId = state.id.value,
        )

        assertNotNull(actual)
        assertNotNull(actual.state)
        assertEquals(state, actual.state)
    }

    @Test
    fun `test update remove state`(): Unit = transaction {
        val city = sampleData.cities.filterNot { c -> c.state == null }.random()
        val name = city.name
        val country = city.country

        val actual = repository.update(
            id = city.id.value,
            name = name,
            countryId = country.id.value,
            stateId = null,
        )

        assertNotNull(actual)
        assertNull(actual.state)
    }

    @Test
    fun `test update country and state`(): Unit = transaction {
        val city = sampleData.cities.filterNot { c -> c.state == null }.random()
        val name = city.name
        val country = sampleData.countries.filterNot { c -> c == city.country || c.states.empty() }.random()
        val state = country.states.toList().random()

        val actual = repository.update(
            id = city.id.value,
            name = name,
            countryId = country.id.value,
            stateId = state.id.value,
        )

        assertNotNull(actual)
        assertEquals(country, actual.country)
        assertNotNull(actual.state)
        assertEquals(state, actual.state)
    }

    @Test
    fun `test update all`(): Unit = transaction {
        val city = sampleData.cities.filterNot { c -> c.state == null }.random()
        val name = "Name"
        val country = sampleData.countries.filterNot { c -> c == city.country || c.states.empty() }.random()
        val state = country.states.toList().random()

        val actual = repository.update(
            id = city.id.value,
            name = name,
            countryId = country.id.value,
            stateId = state.id.value,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        assertEquals(country, actual.country)
        assertNotNull(actual.state)
        assertEquals(state, actual.state)
    }

    @Test
    fun `test update all remove state`(): Unit = transaction {
        val city = sampleData.cities.filterNot { c -> c.state == null }.random()
        val name = "Name"
        val country = sampleData.countries.filterNot { c -> c != city.country && c.states.empty() }.random()

        val actual = repository.update(
            id = city.id.value,
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
    fun `test update all to same`(): Unit = transaction {
        val city = sampleData.cities.filterNot { c -> c.state == null }.random()
        val name = city.name
        val country = city.country
        val state = city.state

        val actual = repository.update(
            id = city.id.value,
            name = name,
            countryId = country.id.value,
            stateId = state?.id?.value,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        assertEquals(country, actual.country)
        assertNotNull(actual.state)
        assertEquals(state, actual.state)
    }

    @Test
    fun `test update on not existing`(): Unit = transaction {
        val id = UUID.randomUUID()
        val name = "doesn't matter"
        val countryId = UUID.randomUUID()
        val stateId = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = id,
                name = name,
                countryId = countryId,
                stateId = stateId,
            )
        }
    }

    @Test
    fun `test update name to existing`(): Unit = transaction {
        val city = sampleData.cities.filter { c -> (c.state?.cities?.count() ?: 0) >= 2 }.random()
        val name = city.state!!.cities.filterNot { c -> c == city }.random().name
        val country = city.country
        val state = city.state

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = city.id.value,
                name = name,
                countryId = country.id.value,
                stateId = state?.id?.value,
            )
        }
    }

    @Test
    fun `test update country to not existing`(): Unit = transaction {
        val city = sampleData.cities.first()
        val name = city.name
        val countryId = UUID.randomUUID()
        val state = city.state

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = city.id.value,
                name = name,
                countryId = countryId,
                stateId = state?.id?.value,
            )
        }
    }

    @Test
    fun `test update state to not existing`(): Unit = transaction {
        val city = sampleData.cities.first()
        val name = city.name
        val country = city.country
        val stateId = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = city.id.value,
                name = name,
                countryId = country.id.value,
                stateId = stateId,
            )
        }
    }

    @Test
    fun `test update country where state does not belong to country`(): Unit = transaction {
        val city = sampleData.cities.filterNot { c -> c.state == null }.random()
        val name = city.name
        val country = sampleData.countries.filterNot { c -> c == city.country }.random()
        val state = city.state

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = city.id.value,
                name = name,
                countryId = country.id.value,
                stateId = state?.id?.value,
            )
        }
    }

    @Test
    fun `test update state where state does not belong to country`(): Unit = transaction {
        val city = sampleData.cities.random()
        val name = city.name
        val country = city.country
        val state = sampleData.states.filterNot { s -> s.country == city.country || s == city.state }.random()

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = city.id.value,
                name = name,
                countryId = country.id.value,
                stateId = state.id.value,
            )
        }
    }

    @Test
    fun `test delete`(): Unit = transaction {
        val id = sampleData.cities.random().id.value

        assertNotNull(City.findById(id))

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
