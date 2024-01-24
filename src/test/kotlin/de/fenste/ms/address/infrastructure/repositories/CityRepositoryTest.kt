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
import de.fenste.ms.address.domain.exception.NotFoundException
import de.fenste.ms.address.domain.model.City
import de.fenste.ms.address.infrastructure.tables.CityTable
import de.fenste.ms.address.infrastructure.tables.PostCodeTable
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
    fun `test list on sample data`(): Unit = transaction {
        val expected = sampleData.cities
            .sortedBy { c -> c.id.value.toString() }
        val actual = repository.list()

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test list on sample data with size`(): Unit = transaction {
        val expected = sampleData.cities
            .sortedWith(compareBy({ c -> c.name }, { c -> c.id.value.toString() }))
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
            .sortedWith(compareBy({ c -> c.name }, { c -> c.id.value.toString() }))
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
    fun `test list post codes on sample data`() {
        val city = transaction { sampleData.cities.filterNot { c -> c.postCodes.empty() }.random() }
        val expected = transaction {
            city
                .postCodes
                .sortedBy { c -> c.id.value.toString() }
        }

        transaction {
            val actual = repository.listPostCodes(city)

            assertContentEquals(expected, actual)
        }
    }

    @Test
    fun `test list post codes on sample data with size`() {
        val city = transaction { sampleData.cities.filterNot { c -> c.postCodes.empty() }.random() }
        val expected = transaction {
            city
                .postCodes
                .sortedWith(compareBy({ c -> c.code }, { c -> c.id.value.toString() }))
                .take(2)
        }

        transaction {
            val actual = repository.listPostCodes(
                city = city,
                order = arrayOf(PostCodeTable.code to SortOrder.ASC),
                size = 2,
            )

            assertContentEquals(expected, actual)
        }
    }

    @Test
    fun `test list post codes on sample data with options`() {
        val city = transaction { sampleData.cities.filterNot { c -> c.postCodes.empty() }.random() }
        val expected = transaction {
            city
                .postCodes
                .sortedWith(compareBy({ c -> c.code }, { c -> c.id.value.toString() }))
                .drop(1 * 2)
                .take(2)
        }

        transaction {
            val actual = repository.listPostCodes(
                city = city,
                order = arrayOf(PostCodeTable.id to SortOrder.ASC),
                page = 1,
                size = 2,
            )

            assertContentEquals(expected, actual)
        }
    }

    @Test
    fun `test create`(): Unit = transaction {
        val name = "Name"
        val state = sampleData.states.random()

        val actual = repository.create(
            name = name,
            stateId = state.id.value,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        assertEquals(state, actual.state)
    }

    @Test
    fun `test create existing`(): Unit = transaction {
        val country = sampleData.countries.filterNot { c -> c.states.empty() }.random()
        val state = country.states.filterNot { s -> s.cities.empty() }.random()
        val name = state.cities.first().name

        assertFailsWith<DuplicateException> {
            repository.create(
                name = name,
                stateId = state.id.value,
            )
        }
    }

    @Test
    fun `test create non existing state`(): Unit = transaction {
        val name = "Name"
        val stateId = UUID.randomUUID()

        assertFailsWith<NotFoundException> {
            repository.create(
                name = name,
                stateId = stateId,
            )
        }
    }

    @Test
    fun `test update name`(): Unit = transaction {
        val city = sampleData.cities.random()
        val name = "Name"
        val state = city.state

        val actual = repository.update(
            id = city.id.value,
            name = name,
            stateId = state.id.value,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
    }

    @Test
    fun `test update state`(): Unit = transaction {
        val city = sampleData.cities.random()
        val name = city.name
        val state = sampleData.states.filter { s -> s.cities.empty() }.random()

        val actual = repository.update(
            id = city.id.value,
            name = name,
            stateId = state.id.value,
        )

        assertNotNull(actual)
        assertNotNull(actual.state)
        assertEquals(state, actual.state)
    }

    @Test
    fun `test update all`(): Unit = transaction {
        val city = sampleData.cities.random()
        val name = "Name"
        val state = sampleData.states.filter { s -> s.cities.empty() }.random()

        val actual = repository.update(
            id = city.id.value,
            name = name,
            stateId = state.id.value,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        assertNotNull(actual.state)
        assertEquals(state, actual.state)
    }

    @Test
    fun `test update all to same`(): Unit = transaction {
        val city = sampleData.cities.random()
        val name = city.name
        val state = city.state

        val actual = repository.update(
            id = city.id.value,
            name = name,
            stateId = state.id.value,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        assertNotNull(actual.state)
        assertEquals(state, actual.state)
    }

    @Test
    fun `test update on not existing`(): Unit = transaction {
        val id = UUID.randomUUID()
        val name = "doesn't matter"
        val stateId = UUID.randomUUID()

        assertFailsWith<NotFoundException> {
            repository.update(
                id = id,
                name = name,
                stateId = stateId,
            )
        }
    }

    @Test
    fun `test update name to existing`(): Unit = transaction {
        val city = sampleData.cities.filter { c -> c.state.cities.count() >= 2 }.random()
        val name = city.state.cities.filterNot { c -> c == city }.random().name
        val state = city.state

        assertFailsWith<DuplicateException> {
            repository.update(
                id = city.id.value,
                name = name,
                stateId = state.id.value,
            )
        }
    }

    @Test
    fun `test update state to not existing`(): Unit = transaction {
        val city = sampleData.cities.first()
        val name = city.name
        val stateId = UUID.randomUUID()

        assertFailsWith<NotFoundException> {
            repository.update(
                id = city.id.value,
                name = name,
                stateId = stateId,
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

        assertFailsWith<NotFoundException> {
            repository.delete(id)
        }
    }
}
