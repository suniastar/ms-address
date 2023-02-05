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

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import de.fenste.ms.address.application.dtos.AddressDto
import de.fenste.ms.address.application.dtos.AddressInputDto
import de.fenste.ms.address.application.dtos.StreetDto
import de.fenste.ms.address.config.SampleDataConfig
import de.fenste.ms.address.domain.model.Address
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.GraphQlTester
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import java.util.UUID
import kotlin.math.ceil
import kotlin.test.BeforeTest
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
@AutoConfigureMockMvc
class AddressControllerTest(
    @Autowired private val sampleData: SampleDataConfig,
    @Autowired private val graphQlTester: GraphQlTester,
    @Autowired private val mockMvc: MockMvc,
) {

    private companion object {
        private const val BASE_URI = "http://localhost"
        private val MAPPER = jacksonObjectMapper()
    }

    @BeforeTest
    fun `set up`() {
        sampleData.reset()
    }

    @Test
    fun `graphql test list on sample data`() {
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
    fun `rest test list on sample data`() {
        val expected = sampleData.addresses
            .sortedBy { c -> c.id.value.toString() }
            .map { a -> a.id.value.toString() }

        mockMvc
            .get("$BASE_URI/api/address") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isOk() }
                content { contentType(MEDIA_TYPE_APPLICATION_HAL_JSON) }
            }
            .andExpect { jsonPath("page.size") { value(expected.count()) } }
            .andExpect { jsonPath("page.totalElements") { value(expected.count()) } }
            .andExpect { jsonPath("page.number") { value(0) } }
            .andExpect { jsonPath("page.totalPages") { value(1) } }
            .andExpect { jsonPath("_links.first.href") { doesNotExist() } }
            .andExpect { jsonPath("_links.prev.href") { doesNotExist() } }
            .andExpect { jsonPath("_links.self.href") { exists() } }
            .andExpect { jsonPath("_links.next.href") { doesNotExist() } }
            .andExpect { jsonPath("_links.last.href") { doesNotExist() } }
            .andExpect { jsonPath("_embedded.addressDtoes.[*].id") { value(expected) } }
    }

    @Test
    fun `graphql test list on sample data with options`() {
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
    fun `rest test list on sample data with options`() {
        val expected = sampleData.addresses
            .sortedWith(compareBy({ a -> a.houseNumber }, { a -> a.id }))
            .drop(1 * 2)
            .take(2)
            .map { a -> a.id.value.toString() }

        mockMvc
            .get("$BASE_URI/api/address?page=1&size=2&sort=house_number,asc") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isOk() }
                content { contentType(MEDIA_TYPE_APPLICATION_HAL_JSON) }
            }
            .andExpect { jsonPath("page.size") { value(expected.count()) } }
            .andExpect { jsonPath("page.totalElements") { value(sampleData.addresses.count()) } }
            .andExpect { jsonPath("page.number") { value(1) } }
            .andExpect { jsonPath("page.totalPages") { value(ceil(sampleData.addresses.count() / 2f)) } }
            .andExpect { jsonPath("_links.first.href") { exists() } }
            .andExpect { jsonPath("_links.prev.href") { exists() } }
            .andExpect { jsonPath("_links.self.href") { exists() } }
            .andExpect { jsonPath("_links.next.href") { exists() } }
            .andExpect { jsonPath("_links.last.href") { exists() } }
            .andExpect { jsonPath("_embedded.addressDtoes.[*].id") { value(expected) } }
    }

    @Test
    fun `graphql test find by id on sample data`() {
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
    fun `rest test find by id on sample data`() {
        val expected = sampleData.addresses.random().let { a -> AddressDto(a) }

        mockMvc
            .get("$BASE_URI/api/address/${expected.id}") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isOk() }
                content { contentType(MEDIA_TYPE_APPLICATION_HAL_JSON) }
            }
            .andExpect { jsonPath("id") { value(expected.id.toString()) } }
            .andExpect { jsonPath("extra") { value(expected.extra) } }
            .andExpect { jsonPath("houseNumber") { value(expected.houseNumber) } }
            .andExpect { jsonPath("_links.self") { exists() } }
            .andExpect { jsonPath("_links.street") { exists() } }
    }

    @Test
    fun `graphql test find by id on non existing sample data`() {
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
    fun `rest test find by id on non existing sample data`() {
        mockMvc
            .get("$BASE_URI/api/address/${UUID.randomUUID()}") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `graphql test create`() {
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
    fun `rest test create`() {
        val houseNumber = "42"
        val street = sampleData.streets.random()

        val create = AddressInputDto(
            houseNumber = houseNumber,
            street = street.id.value,
        )
        val result = mockMvc
            .post("$BASE_URI/api/address") {
                contentType = MediaType.APPLICATION_JSON
                content = MAPPER.writeValueAsString(create)
            }
            .andExpect {
                status { isOk() }
                content { contentType(MEDIA_TYPE_APPLICATION_HAL_JSON) }
            }
            .andExpect { jsonPath("id") { exists() } }
            .andExpect { jsonPath("extra") { value(null) } }
            .andExpect { jsonPath("houseNumber") { value(houseNumber) } }
            .andExpect { jsonPath("_links.self") { exists() } }
            .andExpect { jsonPath("_links.street") { exists() } }
            .andReturn()

        val created = MAPPER.readTree(result.response.contentAsString).findValue("id").asText()
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
    fun `graphql test update all`() {
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
    fun `rest test update all`() {
        val address = sampleData.addresses.random()
        val houseNumber = "42"
        val extra = "extra"
        val street = transaction { sampleData.streets.filterNot { s -> s.addresses.contains(address) }.random() }

        val update = AddressInputDto(
            houseNumber = houseNumber,
            extra = extra,
            street = street.id.value,
        )
        val result = mockMvc
            .put("$BASE_URI/api/address/${address.id.value}") {
                contentType = MediaType.APPLICATION_JSON
                content = MAPPER.writeValueAsString(update)
            }
            .andExpect {
                status { isOk() }
                content { contentType(MEDIA_TYPE_APPLICATION_HAL_JSON) }
            }
            .andExpect { jsonPath("id") { value(address.id.value.toString()) } }
            .andExpect { jsonPath("extra") { value(extra) } }
            .andExpect { jsonPath("houseNumber") { value(houseNumber) } }
            .andExpect { jsonPath("_links.self") { exists() } }
            .andExpect { jsonPath("_links.street") { exists() } }
            .andReturn()

        val updated = MAPPER.readTree(result.response.contentAsString).findValue("id").asText()

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
    fun `graphql test delete`() {
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

    @Test
    fun `rest test delete`() {
        val id = sampleData.addresses.random().id.value

        transaction { assertNotNull(Address.findById(id)) }

        val result = mockMvc
            .delete("$BASE_URI/api/address/$id") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
            .andReturn()

        val actual = result.response.contentAsString.toBoolean()
        assertTrue(actual)
        transaction { assertNull(Address.findById(id)) }
    }

    @Test
    fun `rest test get address street on sample data`() {
        val address = transaction { sampleData.addresses.random() }
        val expected = transaction { StreetDto(address.street) }

        mockMvc
            .get("$BASE_URI/api/address/${address.id.value}/street") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isOk() }
                content { contentType(MEDIA_TYPE_APPLICATION_HAL_JSON) }
            }
            .andExpect { jsonPath("name") { value(expected.name) } }
            .andExpect { jsonPath("id") { value(expected.id.toString()) } }
            .andExpect { jsonPath("_links.self") { exists() } }
            .andExpect { jsonPath("_links.postcode") { exists() } }
            .andExpect { jsonPath("_links.addresses") { exists() } }
    }

    @Test
    fun `rest test get address street on non existing sample data`() {
        mockMvc
            .get("$BASE_URI/api/address/${UUID.randomUUID()}/street") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isNotFound() }
            }
    }
}
