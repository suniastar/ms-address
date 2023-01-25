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

import de.fenste.ms.address.application.dtos.AddressDto
import de.fenste.ms.address.application.dtos.AddressInputDto
import de.fenste.ms.address.application.dtos.StreetDto
import de.fenste.ms.address.config.SampleDataConfig
import de.fenste.ms.address.domain.model.Address
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("sample")
class AddressServiceTest(
    @Autowired private val sampleData: SampleDataConfig,
    @Autowired private val service: AddressService,
) {

    @BeforeTest
    fun `set up`() {
        sampleData.reset()
    }

    @Test
    fun `test count`(): Unit = transaction {
        val expected = sampleData.addresses.count().toLong()
        val actual = service.count()

        assertEquals(expected, actual)
    }

    @Test
    fun `test list on sample data`() {
        val expected = sampleData.addresses
            .sortedBy { a -> a.id.value.toString() }
            .map { s -> AddressDto(s) }
        val actual = service.list()

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    @Ignore
    fun `test list on sample data with options`() {
        val expected = sampleData.addresses
            .sortedWith(compareBy({ a -> a.houseNumber }, { a -> a.id }))
            .drop(1 * 2)
            .take(2)
            .map { a -> AddressDto(a) }
        val actual = service.list(
            sort = "houseNumber,asc",
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
    fun `test find by id on sample data`() {
        val expected = sampleData.addresses.random().let { a -> AddressDto(a) }
        val actual = service.find(id = expected.id)

        transaction { assertEquals(expected, actual) }
    }

    @Test
    fun `test find by id on non existing sample data`() {
        val actual = service.find(id = UUID.randomUUID())

        assertNull(actual)
    }

    @Test
    fun `test create`() {
        val houseNumber = "42"
        val street = sampleData.streets.random()

        val create = AddressInputDto(
            houseNumber = houseNumber,
            street = street.id.value,
        )

        val actual = service.create(
            address = create,
        )

        assertNotNull(actual)
        assertEquals(houseNumber, actual.houseNumber)
        assertNull(actual.extra)
        transaction { assertEquals(StreetDto(street), actual.street) }
    }

    @Test
    fun `test update all`() {
        val address = sampleData.addresses.random()
        val houseNumber = "42"
        val extra = "extra"
        val street = transaction { sampleData.streets.filterNot { s -> s.addresses.contains(address) }.random() }

        val update = AddressInputDto(
            houseNumber = "42",
            extra = extra,
            street = street.id.value,
        )

        val actual = service.update(
            id = address.id.value,
            address = update,
        )

        assertNotNull(actual)
        assertEquals(houseNumber, actual.houseNumber)
        assertNotNull(actual.extra)
        assertEquals(extra, actual.extra)
        transaction { assertEquals(StreetDto(street), actual.street) }
    }

    @Test
    fun `test delete`() {
        val id = sampleData.addresses.random().id.value

        transaction { assertNotNull(Address.findById(id)) }

        service.delete(id)

        transaction { assertNull(Address.findById(id)) }
    }
}
