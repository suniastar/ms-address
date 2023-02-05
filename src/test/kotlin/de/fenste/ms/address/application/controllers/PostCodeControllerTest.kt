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
import de.fenste.ms.address.application.dtos.CityDto
import de.fenste.ms.address.application.dtos.PostCodeDto
import de.fenste.ms.address.application.dtos.PostCodeInputDto
import de.fenste.ms.address.config.SampleDataConfig
import de.fenste.ms.address.domain.model.PostCode
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
class PostCodeControllerTest(
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
        val expected = sampleData.postCodes
            .sortedBy { c -> c.id.value.toString() }
            .map { p -> p.id.value.toString() }

        val query = """
            query {
                postCodes {
                    id
                }
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(query)
            .execute()
            .path("postCodes.[*].id")
            .entityList(String::class.java)
            .get()
            .toList()

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `rest test list on sample data`() {
        val expected = sampleData.postCodes
            .sortedBy { c -> c.id.value.toString() }
            .map { p -> p.id.value.toString() }

        mockMvc
            .get("$BASE_URI/api/postcode") {
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
            .andExpect { jsonPath("_embedded.postCodeDtoes.[*].id") { value(expected) } }
    }

    @Test
    fun `graphql test list on sample data with options`() {
        val expected = sampleData.postCodes
            .sortedWith(compareBy({ p -> p.code }, { p -> p.id }))
            .drop(1 * 2)
            .take(2)
            .map { p -> p.id.value.toString() }

        val query = """
            query {
                postCodes(sort: "code,asc", page: 1, size: 2) {
                    id
                }
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(query)
            .execute()
            .path("postCodes.[*].id")
            .entityList(String::class.java)
            .get()
            .toList()

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `rest test list on sample data with options`() {
        val expected = sampleData.postCodes
            .sortedWith(compareBy({ p -> p.code }, { p -> p.id }))
            .drop(1 * 2)
            .take(2)
            .map { p -> p.id.value.toString() }

        mockMvc
            .get("$BASE_URI/api/postcode?page=1&size=2&sort=code,asc") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isOk() }
                content { contentType(MEDIA_TYPE_APPLICATION_HAL_JSON) }
            }
            .andExpect { jsonPath("page.size") { value(expected.count()) } }
            .andExpect { jsonPath("page.totalElements") { value(sampleData.postCodes.count()) } }
            .andExpect { jsonPath("page.number") { value(1) } }
            .andExpect { jsonPath("page.totalPages") { value(ceil(sampleData.postCodes.count() / 2f)) } }
            .andExpect { jsonPath("_links.first.href") { exists() } }
            .andExpect { jsonPath("_links.prev.href") { exists() } }
            .andExpect { jsonPath("_links.self.href") { exists() } }
            .andExpect { jsonPath("_links.next.href") { exists() } }
            .andExpect { jsonPath("_links.last.href") { exists() } }
            .andExpect { jsonPath("_embedded.postCodeDtoes.[*].id") { value(expected) } }
    }

    @Test
    fun `graphql test find by id on sample data`() {
        val expected = sampleData.postCodes.random().id.value.toString()

        val query = """
            query {
                postCode(id: "$expected") {
                    id
                }
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(query)
            .execute()
            .path("postCode.id")
            .entity(String::class.java)
            .get()

        transaction { assertEquals(expected, actual) }
    }

    @Test
    fun `rest test find by id on sample data`() {
        val expected = sampleData.postCodes.random().let { p -> PostCodeDto(p) }

        mockMvc
            .get("$BASE_URI/api/postcode/${expected.id}") {
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
    fun `graphql test find by id on non existing sample data`() {
        val query = """
            query {
                postCode(id: "${UUID.randomUUID()}") {
                    id
                }
            }
        """.trimIndent()

        graphQlTester
            .document(query)
            .execute()
            .path("postCode")
            .valueIsNull()
    }

    @Test
    fun `rest test find by id on non existing sample data`() {
        mockMvc
            .get("$BASE_URI/api/postcode/${UUID.randomUUID()}") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `graphql test create`() {
        val code = "CODE"
        val city = transaction { sampleData.cities.random() }

        val mutation = """
            mutation CreatePostCodeMutation(${D}postCode: PostCodeInput!) {
                createPostCode(postCode: ${D}postCode) {
                    id
                }
            }
        """.trimIndent()
        val create = PostCodeInputDto(
            code = code,
            city = city.id.value,
        )

        val created = graphQlTester
            .document(mutation)
            .variable("postCode", create.asMap())
            .execute()
            .path("createPostCode.id")
            .entity(String::class.java)
            .get()

        assertNotNull(created)
        assertFalse(sampleData.postCodes.map { p -> p.id.value.toString() }.contains(created))

        transaction {
            val actual = PostCode.findById(UUID.fromString(created))
            assertNotNull(actual)
            assertEquals(code, actual.code)
            assertEquals(city, actual.city)
        }
    }

    @Test
    fun `rest test create`() {
        val code = "CODE"
        val city = transaction { sampleData.cities.random() }

        val create = PostCodeInputDto(
            code = code,
            city = city.id.value,
        )
        val result = mockMvc
            .post("$BASE_URI/api/postcode") {
                contentType = MediaType.APPLICATION_JSON
                content = MAPPER.writeValueAsString(create)
            }
            .andExpect {
                status { isOk() }
                content { contentType(MEDIA_TYPE_APPLICATION_HAL_JSON) }
            }
            .andExpect { jsonPath("id") { exists() } }
            .andExpect { jsonPath("code") { value(code) } }
            .andExpect { jsonPath("_links.self") { exists() } }
            .andExpect { jsonPath("_links.city") { exists() } }
            .andExpect { jsonPath("_links.streets") { exists() } }
            .andReturn()

        val created = MAPPER.readTree(result.response.contentAsString).findValue("id").asText()
        assertFalse(sampleData.postCodes.map { p -> p.id.value.toString() }.contains(created))

        transaction {
            val actual = PostCode.findById(UUID.fromString(created))
            assertNotNull(actual)
            assertEquals(code, actual.code)
            assertEquals(city, actual.city)
        }
    }

    @Test
    fun `graphql test update all`() {
        val postCode = transaction { sampleData.postCodes.random() }
        val code = "CODE"
        val city = transaction { sampleData.cities.filterNot { c -> c.postCodes.contains(postCode) }.random() }

        val mutation = """
            mutation UpdatePostCodeMutation(${D}postCode: PostCodeInput!) {
                updatePostCode(id: "${postCode.id.value}", postCode: ${D}postCode) {
                    id
                }
            }
        """.trimIndent()
        val update = PostCodeInputDto(
            code = code,
            city = city.id.value,
        )

        val updated = graphQlTester
            .document(mutation)
            .variable("postCode", update.asMap())
            .execute()
            .path("updatePostCode.id")
            .entity(String::class.java)
            .get()

        assertNotNull(updated)

        transaction {
            val actual = PostCode.findById(UUID.fromString(updated))
            assertNotNull(actual)
            assertEquals(code, actual.code)
            assertEquals(city, actual.city)
        }
    }

    @Test
    fun `rest test update all`() {
        val postCode = transaction { sampleData.postCodes.random() }
        val code = "CODE"
        val city = transaction { sampleData.cities.filterNot { c -> c.postCodes.contains(postCode) }.random() }

        val update = PostCodeInputDto(
            code = code,
            city = city.id.value,
        )
        val result = mockMvc
            .put("$BASE_URI/api/postcode/${postCode.id.value}") {
                contentType = MediaType.APPLICATION_JSON
                content = MAPPER.writeValueAsString(update)
            }
            .andExpect {
                status { isOk() }
                content { contentType(MEDIA_TYPE_APPLICATION_HAL_JSON) }
            }
            .andExpect { jsonPath("id") { value(postCode.id.value.toString()) } }
            .andExpect { jsonPath("code") { value(code) } }
            .andExpect { jsonPath("_links.self") { exists() } }
            .andExpect { jsonPath("_links.city") { exists() } }
            .andExpect { jsonPath("_links.streets") { exists() } }
            .andReturn()

        val updated = MAPPER.readTree(result.response.contentAsString).findValue("id").asText()

        transaction {
            val actual = PostCode.findById(UUID.fromString(updated))
            assertNotNull(actual)
            assertEquals(code, actual.code)
            assertEquals(city, actual.city)
        }
    }

    @Test
    fun `graphql test delete`() {
        val id = sampleData.postCodes.random().id.value

        transaction { assertNotNull(PostCode.findById(id)) }

        val mutation = """
            mutation {
                deletePostCode(id: "$id")
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(mutation)
            .execute()
            .path("deletePostCode")
            .entity(Boolean::class.java)
            .get()

        assertTrue(actual)
        transaction { assertNull(PostCode.findById(id)) }
    }

    @Test
    fun `rest test delete`() {
        val id = sampleData.postCodes.random().id.value

        transaction { assertNotNull(PostCode.findById(id)) }

        val result = mockMvc
            .delete("$BASE_URI/api/postcode/$id") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
            .andReturn()

        val actual = result.response.contentAsString.toBoolean()
        assertTrue(actual)
        transaction { assertNull(PostCode.findById(id)) }
    }

    @Test
    fun `rest test get post code city on sample data`() {
        val postCode = transaction { sampleData.postCodes.random() }
        val expected = transaction { CityDto(postCode.city) }

        mockMvc
            .get("$BASE_URI/api/postcode/${postCode.id.value}/city") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isOk() }
                content { contentType(MEDIA_TYPE_APPLICATION_HAL_JSON) }
            }
            .andExpect { jsonPath("name") { value(expected.name) } }
            .andExpect { jsonPath("id") { value(expected.id.toString()) } }
            .andExpect { jsonPath("_links.self") { exists() } }
            .andExpect { jsonPath("_links.country") { exists() } }
            .andExpect { jsonPath("_links.state") { exists() } }
            .andExpect { jsonPath("_links.postcodes") { exists() } }
    }

    @Test
    fun `rest test get post code city on non existing sample data`() {
        mockMvc
            .get("$BASE_URI/api/postcode/${UUID.randomUUID()}/city") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `rest test get post code streets`() {
        val postCode = transaction { sampleData.postCodes.filterNot { p -> p.streets.empty() }.random() }
        val expected = transaction {
            postCode.streets
                .sortedBy { s -> s.id.value.toString() }
                .map { s -> s.id.value.toString() }
        }

        mockMvc
            .get("$BASE_URI/api/postcode/${postCode.id.value}/streets") {
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
    fun `rest test get post code streets on non existing sample data`() {
        mockMvc
            .get("$BASE_URI/api/postcode/${UUID.randomUUID()}/streets") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isNotFound() }
            }
    }
}
