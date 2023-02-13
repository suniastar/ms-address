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
import de.fenste.ms.address.domain.model.PostCode
import de.fenste.ms.address.infrastructure.tables.PostCodeTable
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
class PostCodeRepositoryTest(
    @Autowired private val sampleData: SampleDataConfig,
    @Autowired private val repository: PostCodeRepository,
) {

    @BeforeTest
    fun `set up`() {
        sampleData.reset()
    }

    @Test
    fun `test count`(): Unit = transaction {
        val expected = sampleData.postCodes.count()
        val actual = repository.count()

        assertEquals(expected, actual)
    }

    @Test
    fun `test find by id on sample data`(): Unit = transaction {
        val expected = sampleData.postCodes.random()
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
        val expected = sampleData.postCodes
            .sortedBy { p -> p.id.value.toString() }
        val actual = repository.list()

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test list on sample data with size`(): Unit = transaction {
        val expected = sampleData.postCodes
            .sortedWith(compareBy({ p -> p.code }, { p -> p.id.value.toString() }))
            .take(2)
        val actual = repository.list(
            order = arrayOf(PostCodeTable.code to SortOrder.ASC),
            size = 2,
        )

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test list on sample data with options`(): Unit = transaction {
        val expected = sampleData.postCodes
            .sortedWith(compareBy({ p -> p.code }, { p -> p.id.value.toString() }))
            .drop(1 * 2)
            .take(2)
        val actual = repository.list(
            order = arrayOf(PostCodeTable.code to SortOrder.ASC),
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
    fun `test list streets on sample data`() {
        val postCode = transaction { sampleData.postCodes.filterNot { p -> p.streets.empty() }.random() }
        val expected = transaction {
            postCode
                .streets
                .sortedBy { s -> s.id.value.toString() }
        }

        transaction {
            val actual = repository.listStreets(postCode)

            assertContentEquals(expected, actual)
        }
    }

    @Test
    fun `test list streets on sample data with size`() {
        val postCode = transaction { sampleData.postCodes.filterNot { p -> p.streets.empty() }.random() }
        val expected = transaction {
            postCode
                .streets
                .sortedWith(compareBy({ s -> s.name }, { s -> s.id.value.toString() }))
                .take(2)
        }

        transaction {
            val actual = repository.listStreets(
                postCode = postCode,
                order = arrayOf(StreetTable.name to SortOrder.ASC),
                size = 2,
            )

            assertContentEquals(expected, actual)
        }
    }

    @Test
    fun `test list streets on sample data with options`() {
        val postCode = transaction { sampleData.postCodes.filterNot { p -> p.streets.empty() }.random() }
        val expected = transaction {
            postCode
                .streets
                .sortedWith(compareBy({ s -> s.name }, { s -> s.id.value.toString() }))
                .drop(1 * 2)
                .take(2)
        }

        transaction {
            val actual = repository.listStreets(
                postCode = postCode,
                order = arrayOf(StreetTable.name to SortOrder.ASC),
                page = 1,
                size = 2,
            )

            assertContentEquals(expected, actual)
        }
    }

    @Test
    fun `test create`(): Unit = transaction {
        val code = "CODE"
        val city = sampleData.cities.random()

        val actual = repository.create(
            code = code,
            cityId = city.id.value,
        )

        assertNotNull(actual)
        assertEquals(code, actual.code)
        assertEquals(city, actual.city)
    }

    @Test
    fun `test create existing`(): Unit = transaction {
        val city = sampleData.cities.filterNot { p -> p.postCodes.empty() }.random()
        val code = city.postCodes.first().code

        assertFailsWith<DuplicateException> {
            repository.create(
                code = code,
                cityId = city.id.value,
            )
        }
    }

    @Test
    fun `test create non existing city`(): Unit = transaction {
        val code = "Code"
        val cityId = UUID.randomUUID()

        assertFailsWith<NotFoundException> {
            repository.create(
                code = code,
                cityId = cityId,
            )
        }
    }

    @Test
    fun `test update code`(): Unit = transaction {
        val postCode = sampleData.postCodes.random()
        val code = "CODE"
        val city = postCode.city

        val actual = repository.update(
            id = postCode.id.value,
            code = code,
            cityId = city.id.value,
        )

        assertNotNull(actual)
        assertEquals(code, actual.code)
    }

    @Test
    fun `test update city`(): Unit = transaction {
        val postCode = sampleData.postCodes.random()
        val code = postCode.code
        val city = sampleData.cities.filterNot { c -> c.postCodes.contains(postCode) }.random()

        val actual = repository.update(
            id = postCode.id.value,
            code = code,
            cityId = city.id.value,
        )

        assertNotNull(actual)
        assertEquals(city, actual.city)
    }

    @Test
    fun `test update all`(): Unit = transaction {
        val postCode = sampleData.postCodes.random()
        val code = "CODE"
        val city = sampleData.cities.filterNot { c -> c.postCodes.contains(postCode) }.random()

        val actual = repository.update(
            id = postCode.id.value,
            code = code,
            cityId = city.id.value,
        )

        assertNotNull(actual)
        assertEquals(code, actual.code)
        assertEquals(city, actual.city)
    }

    @Test
    fun `test update all to same`(): Unit = transaction {
        val postCode = sampleData.postCodes.random()
        val code = postCode.code
        val city = postCode.city

        val actual = repository.update(
            id = postCode.id.value,
            code = code,
            cityId = city.id.value,
        )

        assertNotNull(actual)
        assertEquals(code, actual.code)
        assertEquals(city, actual.city)
    }

    @Test
    fun `test update on not existing`(): Unit = transaction {
        val id = UUID.randomUUID()

        assertFailsWith<NotFoundException> {
            repository.update(
                id = id,
                code = "doesn't matter",
                cityId = UUID.randomUUID(),
            )
        }
    }

    @Test
    fun `test update name to existing`(): Unit = transaction {
        val postCode = sampleData.postCodes.filter { p -> p.city.postCodes.count() >= 2 }.random()
        val code = postCode.city.postCodes.filterNot { p -> p == postCode }.random().code
        val city = postCode.city

        assertFailsWith<DuplicateException> {
            repository.update(
                id = postCode.id.value,
                code = code,
                cityId = city.id.value,
            )
        }
    }

    @Test
    fun `test update city to not existing`(): Unit = transaction {
        val postCode = sampleData.postCodes.first()
        val code = postCode.code
        val cityId = UUID.randomUUID()

        assertFailsWith<NotFoundException> {
            repository.update(
                id = postCode.id.value,
                code = code,
                cityId = cityId,
            )
        }
    }

    @Test
    fun `test delete`(): Unit = transaction {
        val id = sampleData.postCodes.random().id.value

        assertNotNull(PostCode.findById(id))

        repository.delete(id)

        assertNull(PostCode.findById(id))
    }

    @Test
    fun `test delete not existing`(): Unit = transaction {
        val id = UUID.randomUUID()

        assertFailsWith<NotFoundException> {
            repository.delete(id)
        }
    }
}
