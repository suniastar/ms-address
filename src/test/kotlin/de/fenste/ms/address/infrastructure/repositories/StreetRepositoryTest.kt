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
import de.fenste.ms.address.domain.model.Street
import de.fenste.ms.address.infrastructure.tables.StreetTable
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
class StreetRepositoryTest(
    @Autowired private val sampleData: SampleDataConfig,
    @Autowired private val repository: StreetRepository,
) {

    @BeforeTest
    fun `set up`() {
        sampleData.reset()
    }

    @Test
    fun `test count`(): Unit = transaction {
        val expected = sampleData.streets.count()
        val actual = repository.count()

        assertEquals(expected, actual)
    }

    @Test
    fun `test list on sample data`(): Unit = transaction {
        val expected = sampleData.streets
            .sortedBy { s -> s.id.value.toString() }
        val actual = repository.list()

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test list on sample data with size`(): Unit = transaction {
        val expected = sampleData.streets
            .sortedWith(compareBy({ s -> s.name }, { s -> s.id }))
            .take(2)
        val actual = repository.list(
            order = arrayOf(StreetTable.name to SortOrder.ASC),
            size = 2,
        )

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test list on sample data with options`(): Unit = transaction {
        val expected = sampleData.streets
            .sortedWith(compareBy({ s -> s.name }, { s -> s.id }))
            .drop(1 * 2)
            .take(2)
        val actual = repository.list(
            order = arrayOf(StreetTable.name to SortOrder.ASC),
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
        val expected = sampleData.streets.random()
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
        val postCode = sampleData.postCodes.random()

        val actual = repository.create(
            name = name,
            postCodeId = postCode.id.value,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        assertEquals(postCode, actual.postCode)
    }

    @Test
    fun `test create existing`(): Unit = transaction {
        val postCode = sampleData.postCodes.filterNot { p -> p.streets.empty() }.random()
        val name = postCode.streets.toList().random().name

        assertFailsWith<IllegalArgumentException> {
            repository.create(
                name = name,
                postCodeId = postCode.id.value,
            )
        }
    }

    @Test
    fun `test create non existing post code`(): Unit = transaction {
        val name = "Name"
        val postCodeId = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repository.create(
                name = name,
                postCodeId = postCodeId,
            )
        }
    }

    @Test
    fun `test update name`(): Unit = transaction {
        val street = sampleData.streets.random()
        val name = "Name"
        val postCode = street.postCode

        val actual = repository.update(
            id = street.id.value,
            name = name,
            postCodeId = postCode.id.value,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
    }

    @Test
    fun `test update post code`(): Unit = transaction {
        val street = sampleData.streets.random()
        val name = street.name
        val postCode = sampleData.postCodes.filterNot { p -> p.streets.contains(street) }.random()

        val actual = repository.update(
            id = street.id.value,
            name = name,
            postCodeId = postCode.id.value,
        )

        assertNotNull(actual)
        assertEquals(postCode, actual.postCode)
    }

    @Test
    fun `test update all`(): Unit = transaction {
        val street = sampleData.streets.random()
        val name = "Name"
        val postCode = sampleData.postCodes.filterNot { p -> p.streets.contains(street) }.random()

        val actual = repository.update(
            id = street.id.value,
            name = name,
            postCodeId = postCode.id.value,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        assertEquals(postCode, actual.postCode)
    }

    @Test
    fun `test update all to same`(): Unit = transaction {
        val street = sampleData.streets.random()
        val name = street.name
        val postCode = street.postCode

        val actual = repository.update(
            id = street.id.value,
            name = name,
            postCodeId = postCode.id.value,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        assertEquals(postCode, actual.postCode)
    }

    @Test
    fun `test update on not existing`(): Unit = transaction {
        val id = UUID.randomUUID()
        val name = "doesn't matter"
        val postCodeId = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = id,
                name = name,
                postCodeId = postCodeId,
            )
        }
    }

    @Test
    fun `test update name to existing`(): Unit = transaction {
        val street = sampleData.streets.filter { s -> s.postCode.streets.count() >= 2 }.random()
        val name = street.postCode.streets.filterNot { s -> s == street }.random().name
        val postCode = street.postCode

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = street.id.value,
                name = name,
                postCodeId = postCode.id.value,
            )
        }
    }

    @Test
    fun `test update post code to not existing`(): Unit = transaction {
        val state = sampleData.streets.random()
        val name = state.name
        val postCodeId = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = state.id.value,
                name = name,
                postCodeId = postCodeId,
            )
        }
    }

    @Test
    fun `test delete`(): Unit = transaction {
        val id = sampleData.streets.random().id.value

        transaction { assertNotNull(Street.findById(id)) }

        repository.delete(id)

        transaction { assertNull(Street.findById(id)) }
    }

    @Test
    fun `test delete not existing`(): Unit = transaction {
        val id = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repository.delete(id)
        }
    }
}
