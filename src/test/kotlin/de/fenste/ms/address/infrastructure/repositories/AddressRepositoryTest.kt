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
import de.fenste.ms.address.domain.model.Address
import de.fenste.ms.address.infrastructure.tables.AddressTable
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
class AddressRepositoryTest(
    @Autowired private val sampleData: SampleDataConfig,
    @Autowired private val repository: AddressRepository,
) {

    @BeforeTest
    fun `set up`() {
        sampleData.reset()
    }

    @Test
    fun `test count`(): Unit = transaction {
        val expected = sampleData.addresses.count().toLong()
        val actual = repository.count()

        assertEquals(expected, actual)
    }

    @Test
    fun `test list on sample data`(): Unit = transaction {
        val expected = sampleData.addresses
            .sortedBy { a -> a.id.value.toString() }
        val actual = repository.list()

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test list on sample data with size`(): Unit = transaction {
        val expected = sampleData.addresses
            .sortedWith(compareBy({ a -> a.houseNumber }, { a -> a.id }))
            .take(2)
        val actual = repository.list(
            order = arrayOf(AddressTable.houseNumber to SortOrder.ASC),
            size = 2,
        )

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test list on sample data with options`(): Unit = transaction {
        val expected = sampleData.addresses
            .sortedWith(compareBy({ a -> a.houseNumber }, { a -> a.id }))
            .drop(1 * 2)
            .take(2)
        val actual = repository.list(
            order = arrayOf(AddressTable.houseNumber to SortOrder.ASC),
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
        val expected = sampleData.addresses.random()
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
        val houseNumber = "42"
        val street = sampleData.streets.random()

        val actual = repository.create(
            houseNumber = houseNumber,
            extra = null,
            streetId = street.id.value,
        )

        assertNotNull(actual)
        assertEquals(houseNumber, actual.houseNumber)
        assertNull(actual.extra)
        assertEquals(street, actual.street)
    }

    @Test
    fun `test create all`(): Unit = transaction {
        val houseNumber = "42"
        val extra = "extra"
        val street = sampleData.streets.random()

        val actual = repository.create(
            houseNumber = houseNumber,
            extra = extra,
            streetId = street.id.value,
        )

        assertNotNull(actual)
        assertEquals(houseNumber, actual.houseNumber)
        assertNotNull(actual.extra)
        assertEquals(extra, actual.extra)
        assertEquals(street, actual.street)
    }

    @Test
    fun `test create existing`(): Unit = transaction {
        val street = sampleData.streets.filterNot { s -> s.addresses.empty() }.random()
        val (houseNumber, extra) = street.addresses.toList().random().let { a -> a.houseNumber to a.extra }

        assertFailsWith<IllegalArgumentException> {
            repository.create(
                houseNumber = houseNumber,
                extra = extra,
                streetId = street.id.value,
            )
        }
    }

    @Test
    fun `test create non existing street`(): Unit = transaction {
        val houseNumber = "42"
        val streetId = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repository.create(
                houseNumber = houseNumber,
                extra = null,
                streetId = streetId,
            )
        }
    }

    @Test
    fun `test update house number`(): Unit = transaction {
        val address = sampleData.addresses.random()
        val houseNumber = "42"
        val extra = address.extra
        val street = address.street

        val actual = repository.update(
            id = address.id.value,
            houseNumber = houseNumber,
            extra = extra,
            streetId = street.id.value,
        )

        assertNotNull(actual)
        assertEquals(houseNumber, actual.houseNumber)
        assertEquals(extra, actual.extra)
        assertEquals(street, actual.street)
    }

    @Test
    fun `test update extra`(): Unit = transaction {
        val address = sampleData.addresses.random()
        val houseNumber = address.houseNumber
        val extra = "new extra"
        val street = address.street

        val actual = repository.update(
            id = address.id.value,
            houseNumber = houseNumber,
            extra = extra,
            streetId = street.id.value,
        )

        assertNotNull(actual)
        assertEquals(houseNumber, actual.houseNumber)
        assertEquals(extra, actual.extra)
        assertEquals(street, actual.street)
    }

    @Test
    fun `test update remove extra`(): Unit = transaction {
        val address = sampleData.addresses.filterNot { a -> a.extra == null }.random()
        val houseNumber = address.houseNumber
        val street = address.street

        val actual = repository.update(
            id = address.id.value,
            houseNumber = houseNumber,
            extra = null,
            streetId = street.id.value,
        )

        assertNotNull(actual)
        assertEquals(houseNumber, actual.houseNumber)
        assertNull(actual.extra)
        assertEquals(street, actual.street)
    }

    @Test
    fun `test update street`(): Unit = transaction {
        val address = sampleData.addresses.random()
        val houseNumber = address.houseNumber
        val extra = address.extra
        val street = sampleData.streets
            .filter { s -> s.addresses.none { a -> a.houseNumber == address.houseNumber && a.extra == a.extra } }
            .random()

        val actual = repository.update(
            id = address.id.value,
            houseNumber = houseNumber,
            extra = extra,
            streetId = street.id.value,
        )

        assertNotNull(actual)
        assertEquals(houseNumber, actual.houseNumber)
        assertEquals(extra, actual.extra)
        assertEquals(street, actual.street)
    }

    @Test
    fun `test update all`(): Unit = transaction {
        val address = sampleData.addresses.random()
        val houseNumber = "42"
        val extra = "new extra"
        val street = sampleData.streets.filterNot { s -> s.addresses.contains(address) }.random()

        val actual = repository.update(
            id = address.id.value,
            houseNumber = houseNumber,
            extra = extra,
            streetId = street.id.value,
        )

        assertNotNull(actual)
        assertEquals(houseNumber, actual.houseNumber)
        assertEquals(extra, actual.extra)
        assertEquals(street, actual.street)
    }

    @Test
    fun `test update all to same`(): Unit = transaction {
        val address = sampleData.addresses.random()
        val houseNumber = address.houseNumber
        val extra = address.extra
        val street = address.street

        val actual = repository.update(
            id = address.id.value,
            houseNumber = houseNumber,
            extra = extra,
            streetId = street.id.value,
        )

        assertNotNull(actual)
        assertEquals(houseNumber, actual.houseNumber)
        assertEquals(extra, actual.extra)
        assertEquals(street, actual.street)
    }

    @Test
    fun `test update on not existing`(): Unit = transaction {
        val id = UUID.randomUUID()
        val houseNumber = "doesn't matter"
        val streetId = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = id,
                houseNumber = houseNumber,
                extra = null,
                streetId = streetId,
            )
        }
    }

    @Test
    fun `test update house number and extra to existing`(): Unit = transaction {
        val address = sampleData.addresses.filter { s -> s.street.addresses.count() >= 2 }.random()
        val street = address.street
        val (houseNumber, extra) = street.addresses
            .filterNot { a -> a == address }
            .random()
            .let { a -> a.houseNumber to a.extra }

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = address.id.value,
                houseNumber = houseNumber,
                extra = extra,
                streetId = street.id.value,
            )
        }
    }

    @Test
    fun `test update street to not existing`(): Unit = transaction {
        val address = sampleData.addresses.random()
        val houseNumber = address.houseNumber
        val extra = address.extra
        val streetId = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repository.update(
                id = address.id.value,
                houseNumber = houseNumber,
                extra = extra,
                streetId = streetId,
            )
        }
    }

    @Test
    fun `test delete`(): Unit = transaction {
        val id = sampleData.addresses.random().id.value

        assertNotNull(Address.findById(id))

        repository.delete(id)

        assertNull(Address.findById(id))
    }

    @Test
    fun `test delete not existing`(): Unit = transaction {
        val id = UUID.randomUUID()

        assertFailsWith<IllegalArgumentException> {
            repository.delete(id)
        }
    }
}
