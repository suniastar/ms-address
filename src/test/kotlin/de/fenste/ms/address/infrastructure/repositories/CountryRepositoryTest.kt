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
import de.fenste.ms.address.domain.exception.DuplicateException
import de.fenste.ms.address.domain.exception.InvalidArgumentException
import de.fenste.ms.address.domain.exception.NotFoundException
import de.fenste.ms.address.domain.model.Country
import de.fenste.ms.address.infrastructure.tables.CityTable
import de.fenste.ms.address.infrastructure.tables.CountryTable
import de.fenste.ms.address.infrastructure.tables.StateTable
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
class CountryRepositoryTest(
    @Autowired private val sampleData: SampleDataConfig,
    @Autowired private val repository: CountryRepository,
) {

    @BeforeTest
    fun `set up`() {
        sampleData.reset()
    }

    @Test
    fun `test count`(): Unit = transaction {
        val expected = sampleData.countries.count()
        val actual = repository.count()

        assertEquals(expected, actual)
    }

    @Test
    fun `test find by id on sample data`(): Unit = transaction {
        val expected = sampleData.countries.random()
        val actual = repository.find(id = expected.id.value)

        assertEquals(expected, actual)
    }

    @Test
    fun `test find by alpha2 on sample data`(): Unit = transaction {
        val expected = sampleData.countries.random()
        val actual = repository.find(alpha2 = expected.alpha2)

        assertEquals(expected, actual)
    }

    @Test
    fun `test find by alpha3 on sample data`(): Unit = transaction {
        val expected = sampleData.countries.random()
        val actual = repository.find(alpha3 = expected.alpha3)

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
    fun `test find by nothing on sample data`(): Unit = transaction {
        assertFailsWith<InvalidArgumentException> {
            repository.find()
        }
    }

    @Test
    fun `test list on sample data`(): Unit = transaction {
        val expected = sampleData.countries
            .sortedBy { c -> c.id.value.toString() }
        val actual = repository.list()

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test list on sample data with size`(): Unit = transaction {
        val expected = sampleData.countries
            .sortedWith(compareBy({ c -> c.name }, { c -> c.id.value.toString() }))
            .take(2)
        val actual = repository.list(
            order = arrayOf(CountryTable.name to SortOrder.ASC),
            size = 2,
        )

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test list on sample data with options`(): Unit = transaction {
        val expected = sampleData.countries
            .sortedWith(compareBy({ c -> c.name }, { c -> c.id.value.toString() }))
            .drop(1 * 2)
            .take(2)
        val actual = repository.list(
            order = arrayOf(CountryTable.name to SortOrder.ASC),
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
    fun `test list states on sample data`() {
        val country = transaction { sampleData.countries.filterNot { c -> c.states.empty() }.random() }
        val expected = transaction {
            country
                .states
                .sortedBy { c -> c.id.value.toString() }
        }

        transaction {
            val actual = repository.listStates(country)

            assertContentEquals(expected, actual)
        }
    }

    @Test
    fun `test list states on sample data with size`() {
        val country = transaction { sampleData.countries.filterNot { c -> c.states.empty() }.random() }
        val expected = transaction {
            country
                .states
                .sortedWith(compareBy({ s -> s.name }, { s -> s.id.value.toString() }))
                .take(2)
        }

        transaction {
            val actual = repository.listStates(
                country = country,
                order = arrayOf(StateTable.name to SortOrder.ASC),
                size = 2,
            )

            assertContentEquals(expected, actual)
        }
    }

    @Test
    fun `test list states on sample data with options`() {
        val country = transaction { sampleData.countries.filterNot { c -> c.states.empty() }.random() }
        val expected = transaction {
            country
                .states
                .sortedWith(compareBy({ s -> s.name }, { s -> s.id.value.toString() }))
                .drop(1 * 2)
                .take(2)
        }

        transaction {
            val actual = repository.listStates(
                country = country,
                order = arrayOf(StateTable.name to SortOrder.ASC),
                page = 1,
                size = 2,
            )

            assertContentEquals(expected, actual)
        }
    }

    @Test
    fun `test list cities on sample data`() {
        val country = transaction { sampleData.countries.filterNot { c -> c.cities.empty() }.random() }
        val expected = transaction {
            country
                .cities
                .sortedBy { c -> c.id.value.toString() }
        }

        transaction {
            val actual = repository.listCities(country)

            assertContentEquals(expected, actual)
        }
    }

    @Test
    fun `test list cities on sample data with size`() {
        val country = transaction { sampleData.countries.filterNot { c -> c.cities.empty() }.random() }
        val expected = transaction {
            country
                .cities
                .sortedWith(compareBy({ c -> c.name }, { c -> c.id.value.toString() }))
                .take(2)
        }

        transaction {
            val actual = repository.listCities(
                country = country,
                order = arrayOf(CityTable.name to SortOrder.ASC),
                size = 2,
            )

            assertContentEquals(expected, actual)
        }
    }

    @Test
    fun `test list cities on sample data with options`() {
        val country = transaction { sampleData.countries.filterNot { c -> c.cities.empty() }.random() }
        val expected = transaction {
            country
                .cities
                .sortedWith(compareBy({ c -> c.name }, { c -> c.id.value.toString() }))
                .drop(1 * 2)
                .take(2)
        }

        transaction {
            val actual = repository.listCities(
                country = country,
                order = arrayOf(CityTable.name to SortOrder.ASC),
                page = 1,
                size = 2,
            )

            assertContentEquals(expected, actual)
        }
    }

    @Test
    fun `test create`(): Unit = transaction {
        val alpha2 = "CZ"
        val alpha3 = "CZE"
        val name = "Czechia"
        val localizedName = "Tschechien"

        val actual = repository.create(
            alpha2 = alpha2,
            alpha3 = alpha3,
            name = name,
            localizedName = localizedName,
        )

        assertNotNull(actual)
        assertEquals(alpha2, actual.alpha2)
        assertEquals(alpha3, actual.alpha3)
        assertEquals(name, actual.name)
        assertEquals(localizedName, actual.localizedName)
    }

    @Test
    fun `test create existing alpha2`(): Unit = transaction {
        val alpha2 = sampleData.countries.random().alpha2
        val alpha3 = "XXX"
        val name = "Name"
        val localizedName = "LocalizedName"

        assertFailsWith<DuplicateException> {
            repository.create(
                alpha2 = alpha2,
                alpha3 = alpha3,
                name = name,
                localizedName = localizedName,
            )
        }
    }

    @Test
    fun `test create existing alpha3`(): Unit = transaction {
        val alpha2 = "XX"
        val alpha3 = sampleData.countries.random().alpha3
        val name = "Name"
        val localizedName = "LocalizedName"

        assertFailsWith<DuplicateException> {
            repository.create(
                alpha2 = alpha2,
                alpha3 = alpha3,
                name = name,
                localizedName = localizedName,
            )
        }
    }

    @Test
    fun `test create existing name`(): Unit = transaction {
        val alpha2 = "XX"
        val alpha3 = "XXX"
        val name = sampleData.countries.random().name
        val localizedName = "LocalizedName"

        assertFailsWith<DuplicateException> {
            repository.create(
                alpha2 = alpha2,
                alpha3 = alpha3,
                name = name,
                localizedName = localizedName,
            )
        }
    }

    @Test
    fun `test update alpha2`(): Unit = transaction {
        val country = sampleData.countries.random()
        val alpha2 = "XX"
        val alpha3 = country.alpha3
        val name = country.name
        val localizedName = country.localizedName

        val actual = repository.update(
            id = country.id.value,
            alpha2 = alpha2,
            alpha3 = alpha3,
            name = name,
            localizedName = localizedName,
        )

        assertNotNull(actual)
        assertEquals(alpha2, actual.alpha2)
    }

    @Test
    fun `test update alpha3`(): Unit = transaction {
        val country = sampleData.countries.random()
        val alpha2 = country.alpha2
        val alpha3 = "XXX"
        val name = country.name
        val localizedName = country.localizedName

        val actual = repository.update(
            id = country.id.value,
            alpha2 = alpha2,
            alpha3 = alpha3,
            name = name,
            localizedName = localizedName,
        )

        assertNotNull(actual)
        assertEquals(alpha3, actual.alpha3)
    }

    @Test
    fun `test update name`(): Unit = transaction {
        val country = sampleData.countries.random()
        val alpha2 = country.alpha2
        val alpha3 = country.alpha3
        val name = "Name"
        val localizedName = country.localizedName

        val actual = repository.update(
            id = country.id.value,
            alpha2 = alpha2,
            alpha3 = alpha3,
            name = name,
            localizedName = localizedName,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
    }

    @Test
    fun `test update localizedName`(): Unit = transaction {
        val country = sampleData.countries.random()
        val alpha2 = country.alpha2
        val alpha3 = country.alpha3
        val name = country.name
        val localizedName = "LocalizedName"

        val actual = repository.update(
            id = country.id.value,
            alpha2 = alpha2,
            alpha3 = alpha3,
            name = name,
            localizedName = localizedName,
        )

        assertNotNull(actual)
        assertEquals(localizedName, actual.localizedName)
    }

    @Test
    fun `test update all`(): Unit = transaction {
        val country = sampleData.countries.random()
        val alpha2 = "XX"
        val alpha3 = "XXX"
        val name = "Name"
        val localizedName = "LocalizedName"

        val actual = repository.update(
            id = country.id.value,
            alpha2 = alpha2,
            alpha3 = alpha3,
            name = name,
            localizedName = localizedName,
        )

        assertNotNull(actual)
        assertEquals(alpha2, actual.alpha2)
        assertEquals(alpha3, actual.alpha3)
        assertEquals(name, actual.name)
        assertEquals(localizedName, actual.localizedName)
    }

    @Test
    fun `test update all to same`(): Unit = transaction {
        val country = sampleData.countries.random()
        val alpha2 = country.alpha2
        val alpha3 = country.alpha3
        val name = country.name
        val localizedName = country.localizedName

        val actual = repository.update(
            id = country.id.value,
            alpha2 = alpha2,
            alpha3 = alpha3,
            name = name,
            localizedName = localizedName,
        )

        assertNotNull(actual)
        assertEquals(alpha2, actual.alpha2)
        assertEquals(alpha3, actual.alpha3)
        assertEquals(name, actual.name)
        assertEquals(localizedName, actual.localizedName)
    }

    @Test
    fun `test update on not existing`(): Unit = transaction {
        val id = UUID.randomUUID()
        val alpha2 = "this"
        val alpha3 = "does"
        val name = "not"
        val localizedName = "matter"

        assertFailsWith<NotFoundException> {
            repository.update(
                id = id,
                alpha2 = alpha2,
                alpha3 = alpha3,
                name = name,
                localizedName = localizedName,
            )
        }
    }

    @Test
    fun `test update alpha2 to existing`(): Unit = transaction {
        val country = sampleData.countries.random()
        val alpha2 = sampleData.countries.filterNot { c -> c.alpha2 == country.alpha2 }.random().alpha2
        val alpha3 = country.alpha3
        val name = country.name
        val localizedName = country.localizedName

        assertFailsWith<DuplicateException> {
            repository.update(
                id = country.id.value,
                alpha2 = alpha2,
                alpha3 = alpha3,
                name = name,
                localizedName = localizedName,
            )
        }
    }

    @Test
    fun `test update alpha3 to existing`(): Unit = transaction {
        val country = sampleData.countries.random()
        val alpha2 = country.alpha2
        val alpha3 = sampleData.countries.filterNot { c -> c.alpha3 == country.alpha3 }.random().alpha3
        val name = country.name
        val localizedName = country.localizedName

        assertFailsWith<DuplicateException> {
            repository.update(
                id = country.id.value,
                alpha2 = alpha2,
                alpha3 = alpha3,
                name = name,
                localizedName = localizedName,
            )
        }
    }

    @Test
    fun `test update name to existing`(): Unit = transaction {
        val country = sampleData.countries.random()
        val alpha2 = country.alpha2
        val alpha3 = country.alpha3
        val name = sampleData.countries.filterNot { c -> c == country }.random().name
        val localizedName = country.localizedName

        assertFailsWith<DuplicateException> {
            repository.update(
                id = country.id.value,
                alpha2 = alpha2,
                alpha3 = alpha3,
                name = name,
                localizedName = localizedName,
            )
        }
    }

    @Test
    fun `test delete`(): Unit = transaction {
        val id = sampleData.countries.random().id.value

        assertNotNull(Country.findById(id))

        repository.delete(id)

        assertNull(Country.findById(id))
    }

    @Test
    fun `test delete not existing`(): Unit = transaction {
        val id = UUID.randomUUID()

        assertFailsWith<NotFoundException> {
            repository.delete(id)
        }
    }
}
