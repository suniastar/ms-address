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

package de.fenste.ms.address.application.controllers

import de.fenste.ms.address.application.dtos.AddressInputDto
import de.fenste.ms.address.config.SampleDataConfig
import de.fenste.ms.address.domain.model.Address
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.test.context.ActiveProfiles
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("sample")
@AutoConfigureGraphQlTester
class AddressControllerTest(
    @Autowired private val sampleData: SampleDataConfig,
    @Autowired private val graphQlTester: GraphQlTester,
) {

    @BeforeTest
    fun `set up`() {
        sampleData.reset()
    }

    @Test
    fun `test list on sample data`() {
        val expected = sampleData.addresses
            .sortedBy { c -> c.id.value.toString() }
            .map { a -> a.id.value.toString() }

        val query = """
            query {
                addresses {
                    id
                }
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(query)
            .execute()
            .path("addresses.[*].id")
            .entityList(String::class.java)
            .get()
            .toList()

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    @Ignore
    fun `test list on sample data with options`() {
        val expected = sampleData.addresses
            .sortedWith(compareBy({ a -> a.houseNumber }, { a -> a.id }))
            .drop(1 * 2)
            .take(2)
            .map { a -> a.id.value.toString() }

        val query = """
            query {
                addresses(sort: "houseNumber,asc", page: 1, size: 2) {
                    id
                }
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(query)
            .execute()
            .path("addresses.[*].id")
            .entityList(String::class.java)
            .get()
            .toList()

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `test find by id on sample data`() {
        val expected = sampleData.addresses.random().id.value.toString()

        val query = """
            query {
                address(id: "$expected") {
                    id
                }
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(query)
            .execute()
            .path("address.id")
            .entity(String::class.java)
            .get()

        transaction { assertEquals(expected, actual) }
    }

    @Test
    fun `test find by id on non existing sample data`() {
        val query = """
            query {
                address(id: "${UUID.randomUUID()}") {
                    id
                }
            }
        """.trimIndent()

        graphQlTester
            .document(query)
            .execute()
            .path("address")
            .valueIsNull()
    }

    @Test
    fun `test create`() {
        val houseNumber = "42"
        val street = sampleData.streets.random()

        val mutation = """
            mutation CreateAddressMutation(${D}address: AddressInput!) {
                createAddress(address: ${D}address) {
                    id
                }
            }
        """.trimIndent()
        val create = AddressInputDto(
            houseNumber = houseNumber,
            street = street.id.value,
        )

        val created = graphQlTester
            .document(mutation)
            .variable("address", create.asMap())
            .execute()
            .path("createAddress.id")
            .entity(String::class.java)
            .get()

        assertNotNull(created)
        assertFalse(sampleData.addresses.map { a -> a.id.value.toString() }.contains(created))

        transaction {
            val actual = Address.findById(UUID.fromString(created))
            assertNotNull(actual)
            assertEquals(houseNumber, actual.houseNumber)
            assertNull(actual.extra)
            assertEquals(street, actual.street)
        }
    }

    @Test
    fun `test update all`() {
        val address = sampleData.addresses.random()
        val houseNumber = "42"
        val extra = "extra"
        val street = transaction { sampleData.streets.filterNot { s -> s.addresses.contains(address) }.random() }

        val mutation = """
            mutation UpdateAddressMutation(${D}address: AddressInput!) {
                updateAddress(id: "${address.id.value}", address: ${D}address) {
                    id
                }
            }
        """.trimIndent()
        val update = AddressInputDto(
            houseNumber = houseNumber,
            extra = extra,
            street = street.id.value,
        )

        val updated = graphQlTester
            .document(mutation)
            .variable("address", update.asMap())
            .execute()
            .path("updateAddress.id")
            .entity(String::class.java)
            .get()

        assertNotNull(updated)

        transaction {
            val actual = Address.findById(UUID.fromString(updated))
            assertNotNull(actual)
            assertEquals(houseNumber, actual.houseNumber)
            assertNotNull(actual.extra)
            assertEquals(extra, actual.extra)
            assertEquals(street, actual.street)
        }
    }

    @Test
    fun `test delete`() {
        val id = sampleData.addresses.random().id.value

        transaction { assertNotNull(Address.findById(id)) }

        val mutation = """
            mutation {
                deleteAddress(id: "$id")
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(mutation)
            .execute()
            .path("deleteAddress")
            .entity(Boolean::class.java)
            .get()

        assertTrue(actual)
        transaction { assertNull(Address.findById(id)) }
    }
}
