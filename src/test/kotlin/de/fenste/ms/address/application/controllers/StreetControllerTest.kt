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
import de.fenste.ms.address.application.dtos.PostCodeDto
import de.fenste.ms.address.application.dtos.StreetDto
import de.fenste.ms.address.application.dtos.StreetInputDto
import de.fenste.ms.address.config.SampleDataConfig
import de.fenste.ms.address.domain.model.Street
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
class StreetControllerTest(
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
        val expected = sampleData.streets
            .sortedBy { c -> c.id.value.toString() }
            .map { s -> s.id.value.toString() }

        val query = """
            query {
                streets {
                    id
                }
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(query)
            .execute()
            .path("streets.[*].id")
            .entityList(String::class.java)
            .get()
            .toList()

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `rest test list on sample data`() {
        val expected = sampleData.streets
            .sortedBy { c -> c.id.value.toString() }
            .map { s -> s.id.value.toString() }

        mockMvc
            .get("$BASE_URI/api/street") {
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
            .andExpect { jsonPath("_embedded.streetDtoes.[*].id") { value(expected) } }
    }

    @Test
    fun `graphql test list on sample data with options`() {
        val expected = sampleData.streets
            .sortedWith(compareBy({ c -> c.name }, { c -> c.id }))
            .drop(1 * 2)
            .take(2)
            .map { s -> s.id.value.toString() }

        val query = """
            query {
                streets(sort: "name,asc", page: 1, size: 2) {
                    id
                }
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(query)
            .execute()
            .path("streets.[*].id")
            .entityList(String::class.java)
            .get()
            .toList()

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `rest test list on sample data with options`() {
        val expected = sampleData.streets
            .sortedWith(compareBy({ c -> c.name }, { c -> c.id }))
            .drop(1 * 2)
            .take(2)
            .map { s -> s.id.value.toString() }

        mockMvc
            .get("$BASE_URI/api/street?page=1&size=2&sort=name,asc") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isOk() }
                content { contentType(MEDIA_TYPE_APPLICATION_HAL_JSON) }
            }
            .andExpect { jsonPath("page.size") { value(expected.count()) } }
            .andExpect { jsonPath("page.totalElements") { value(sampleData.streets.count()) } }
            .andExpect { jsonPath("page.number") { value(1) } }
            .andExpect { jsonPath("page.totalPages") { value(ceil(sampleData.streets.count() / 2f)) } }
            .andExpect { jsonPath("_links.first.href") { exists() } }
            .andExpect { jsonPath("_links.prev.href") { exists() } }
            .andExpect { jsonPath("_links.self.href") { exists() } }
            .andExpect { jsonPath("_links.next.href") { exists() } }
            .andExpect { jsonPath("_links.last.href") { exists() } }
            .andExpect { jsonPath("_embedded.streetDtoes.[*].id") { value(expected) } }
    }

    @Test
    fun `test find by id on sample data`() {
        val expected = sampleData.streets.random().let { s -> StreetDto(s) }

        mockMvc
            .get("$BASE_URI/api/street/${expected.id}") {
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
    fun `graphql test find by id on non existing sample data`() {
        val query = """
            query {
                street(id: "${UUID.randomUUID()}") {
                    id
                }
            }
        """.trimIndent()

        graphQlTester
            .document(query)
            .execute()
            .path("street")
            .valueIsNull()
    }

    @Test
    fun `rest test find by id on non existing sample data`() {
        mockMvc
            .get("$BASE_URI/api/street/${UUID.randomUUID()}") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `rest test create`() {
        val name = "Name"
        val postCode = sampleData.postCodes.random()

        val create = StreetInputDto(
            name = name,
            postCode = postCode.id.value,
        )
        val result = mockMvc
            .post("$BASE_URI/api/street") {
                contentType = MediaType.APPLICATION_JSON
                content = MAPPER.writeValueAsString(create)
            }
            .andExpect {
                status { isOk() }
                content { contentType(MEDIA_TYPE_APPLICATION_HAL_JSON) }
            }
            .andExpect { jsonPath("name") { value(name) } }
            .andExpect { jsonPath("id") { exists() } }
            .andExpect { jsonPath("_links.self") { exists() } }
            .andExpect { jsonPath("_links.postcode") { exists() } }
            .andExpect { jsonPath("_links.addresses") { exists() } }
            .andReturn()

        val created = MAPPER.readTree(result.response.contentAsString).findValue("id").asText()
        assertFalse(sampleData.streets.map { s -> s.id.value.toString() }.contains(created))

        transaction {
            val actual = Street.findById(UUID.fromString(created))
            assertNotNull(actual)
            assertEquals(name, actual.name)
            assertEquals(postCode, actual.postCode)
        }
    }

    @Test
    fun `graphql test update all`() {
        val street = sampleData.streets.random()
        val name = "Name"
        val postCode = transaction { sampleData.postCodes.filterNot { p -> p.streets.contains(street) }.random() }

        val mutation = """
            mutation UpdateStreetMutation(${D}street: StreetInput!) {
                updateStreet(id: "${street.id.value}", street: ${D}street) {
                    id
                }
            }
        """.trimIndent()
        val update = StreetInputDto(
            name = name,
            postCode = postCode.id.value,
        )

        val updated = graphQlTester
            .document(mutation)
            .variable("street", update.asMap())
            .execute()
            .path("updateStreet.id")
            .entity(String::class.java)
            .get()

        assertNotNull(updated)

        transaction {
            val actual = Street.findById(UUID.fromString(updated))
            assertNotNull(actual)
            assertEquals(name, actual.name)
            assertEquals(postCode, actual.postCode)
        }
    }

    @Test
    fun `rest test update all`() {
        val street = sampleData.streets.random()
        val name = "Name"
        val postCode = transaction { sampleData.postCodes.filterNot { p -> p.streets.contains(street) }.random() }

        val update = StreetInputDto(
            name = name,
            postCode = postCode.id.value,
        )
        val result = mockMvc
            .put("$BASE_URI/api/street/${street.id.value}") {
                contentType = MediaType.APPLICATION_JSON
                content = MAPPER.writeValueAsString(update)
            }
            .andExpect {
                status { isOk() }
                content { contentType(MEDIA_TYPE_APPLICATION_HAL_JSON) }
            }
            .andExpect { jsonPath("name") { value(name) } }
            .andExpect { jsonPath("id") { value(street.id.value.toString()) } }
            .andExpect { jsonPath("_links.self") { exists() } }
            .andExpect { jsonPath("_links.postcode") { exists() } }
            .andExpect { jsonPath("_links.addresses") { exists() } }
            .andReturn()

        val updated = MAPPER.readTree(result.response.contentAsString).findValue("id").asText()

        transaction {
            val actual = Street.findById(UUID.fromString(updated))
            assertNotNull(actual)
            assertEquals(name, actual.name)
            assertEquals(postCode, actual.postCode)
        }
    }

    @Test
    fun `graphql test delete`() {
        val id = sampleData.streets.random().id.value

        transaction { assertNotNull(Street.findById(id)) }

        val mutation = """
            mutation {
                deleteStreet(id: "$id")
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(mutation)
            .execute()
            .path("deleteStreet")
            .entity(Boolean::class.java)
            .get()

        assertTrue(actual)
        transaction { assertNull(Street.findById(id)) }
    }

    @Test
    fun `rest test delete`() {
        val id = sampleData.streets.random().id.value

        transaction { assertNotNull(Street.findById(id)) }

        val result = mockMvc
            .delete("$BASE_URI/api/street/$id") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
            .andReturn()

        val actual = result.response.contentAsString.toBoolean()
        assertTrue(actual)
        transaction { assertNull(Street.findById(id)) }
    }

    @Test
    fun `rest test get street post code on sample data`() {
        val street = transaction { sampleData.streets.random() }
        val expected = transaction { PostCodeDto(street.postCode) }

        mockMvc
            .get("$BASE_URI/api/street/${street.id.value}/postcode") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isOk() }
                content { contentType(MEDIA_TYPE_APPLICATION_HAL_JSON) }
            }
            .andExpect { jsonPath("id") { value(expected.id.toString()) } }
            .andExpect { jsonPath("code") { value(expected.code) } }
            .andExpect { jsonPath("_links.self") { exists() } }
            .andExpect { jsonPath("_links.city") { exists() } }
            .andExpect { jsonPath("_links.streets") { exists() } }
    }

    @Test
    fun `rest test get street post code on non existing sample data`() {
        mockMvc
            .get("$BASE_URI/api/street/${UUID.randomUUID()}/postcode") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `rest test get street addresses`() {
        val street = transaction { sampleData.streets.filterNot { s -> s.addresses.empty() }.random() }
        val expected = transaction {
            street.addresses
                .sortedBy { a -> a.id.value.toString() }
                .map { a -> a.id.value.toString() }
        }

        mockMvc
            .get("$BASE_URI/api/street/${street.id.value}/addresses") {
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
    fun `rest test get street addresses on non existing sample data`() {
        mockMvc
            .get("$BASE_URI/api/street/${UUID.randomUUID()}/addresses") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isNotFound() }
            }
    }
}
