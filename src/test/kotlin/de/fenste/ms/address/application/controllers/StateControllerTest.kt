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
import de.fenste.ms.address.application.dtos.StateDto
import de.fenste.ms.address.application.dtos.StateInputDto
import de.fenste.ms.address.config.SampleDataConfig
import de.fenste.ms.address.domain.model.State
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
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.test.fail

@SpringBootTest
@ActiveProfiles("sample")
@AutoConfigureGraphQlTester
@AutoConfigureMockMvc
class StateControllerTest(
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
        val expected = sampleData.states
            .sortedBy { c -> c.id.value.toString() }
            .map { s -> s.id.value.toString() }

        val query = """
            query {
                states {
                    id
                }
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(query)
            .execute()
            .path("states.[*].id")
            .entityList(String::class.java)
            .get()
            .toList()

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `rest test list on sample data`() {
        val expected = sampleData.states
            .sortedBy { c -> c.id.value.toString() }
            .map { s -> s.id.value.toString() }

        mockMvc
            .get("$BASE_URI/api/state") {
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
            .andExpect { jsonPath("_embedded.stateDtoes.[*].id") { value(expected) } }
    }

    @Test
    fun `graphql test list on sample data with options`() {
        val expected = sampleData.states
            .sortedWith(compareBy({ s -> s.name }, { s -> s.id }))
            .drop(1 * 2)
            .take(2)
            .map { s -> s.id.value.toString() }

        val query = """
            query {
                states(sort: "name,asc", page: 1, size: 2) {
                    id
                }
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(query)
            .execute()
            .path("states.[*].id")
            .entityList(String::class.java)
            .get()
            .toList()

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `rest test list on sample data with options`() {
        val expected = sampleData.states
            .sortedWith(compareBy({ s -> s.name }, { s -> s.id }))
            .drop(1 * 2)
            .take(2)
            .map { s -> s.id.value.toString() }

        mockMvc
            .get("$BASE_URI/api/state?page=1&size=2&sort=name,asc") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isOk() }
                content { contentType(MEDIA_TYPE_APPLICATION_HAL_JSON) }
            }
            .andExpect { jsonPath("page.size") { value(expected.count()) } }
            .andExpect { jsonPath("page.totalElements") { value(sampleData.states.count()) } }
            .andExpect { jsonPath("page.number") { value(1) } }
            .andExpect { jsonPath("page.totalPages") { value(ceil(sampleData.states.count() / 2f)) } }
            .andExpect { jsonPath("_links.first.href") { exists() } }
            .andExpect { jsonPath("_links.prev.href") { exists() } }
            .andExpect { jsonPath("_links.self.href") { exists() } }
            .andExpect { jsonPath("_links.next.href") { doesNotExist() } }
            .andExpect { jsonPath("_links.last.href") { exists() } }
            .andExpect { jsonPath("_embedded.stateDtoes.[*].id") { value(expected) } }
    }

    @Test
    fun `graphql test find by id on sample data`() {
        val expected = sampleData.states.random().id.value.toString()

        val query = """
            query {
                state(id: "$expected") {
                    id
                }
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(query)
            .execute()
            .path("state.id")
            .entity(String::class.java)
            .get()

        transaction { assertEquals(expected, actual) }
    }

    @Test
    fun `rest test find by id on sample data`() {
        val expected = sampleData.states.random().let { s -> StateDto(s) }

        mockMvc
            .get("$BASE_URI/api/state/${expected.id}") {
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
            .andExpect { jsonPath("_links.cities") { exists() } }
    }

    @Test
    fun `graphql test find by id on non existing sample data`() {
        val query = """
            query {
                state(id: "${UUID.randomUUID()}") {
                    id
                }
            }
        """.trimIndent()

        graphQlTester
            .document(query)
            .execute()
            .path("state")
            .valueIsNull()
    }

    @Test
    fun `rest test find by id on non existing sample data`() {
        mockMvc
            .get("$BASE_URI/api/state/${UUID.randomUUID()}") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `graphql test create`() {
        val name = "Name"
        val country = sampleData.countries.random()

        val mutation = """
            mutation CreateStateMutation(${D}state: StateInput!) {
                createState(state: ${D}state) {
                    id
                }
            }
        """.trimIndent()
        val create = StateInputDto(
            name = name,
            country = country.id.value,
        )

        val created = graphQlTester
            .document(mutation)
            .variable("state", create.asMap())
            .execute()
            .path("createState.id")
            .entity(String::class.java)
            .get()

        assertNotNull(created)
        assertFalse(sampleData.states.map { s -> s.id.value.toString() }.contains(created))

        transaction {
            val actual = State.findById(UUID.fromString(created))
            assertNotNull(actual)
            assertEquals(name, actual.name)
            assertEquals(country, actual.country)
        }
    }

    @Test
    fun `rest test create`() {
        val name = "Name"
        val country = sampleData.countries.random()

        val create = StateInputDto(
            name = name,
            country = country.id.value,
        )
        val result = mockMvc
            .post("$BASE_URI/api/state") {
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
            .andExpect { jsonPath("_links.country") { exists() } }
            .andExpect { jsonPath("_links.cities") { exists() } }
            .andReturn()

        val created = MAPPER.readTree(result.response.contentAsString).findValue("id").asText()
        assertFalse(sampleData.states.map { s -> s.id.value.toString() }.contains(created))

        transaction {
            val actual = State.findById(UUID.fromString(created))
            assertNotNull(actual)
            assertEquals(name, actual.name)
            assertEquals(country, actual.country)
        }
    }

    @Test
    fun `graphql test update all`() {
        val state = sampleData.states.random()
        val name = "Name"
        val country = transaction { sampleData.countries.filterNot { c -> c.states.contains(state) }.random() }

        val mutation = """
            mutation UpdateStateMutation(${D}state: StateInput!) {
                updateState(id: "${state.id.value}", state: ${D}state) {
                    id
                }
            }
        """.trimIndent()
        val update = StateInputDto(
            name = name,
            country = country.id.value,
        )

        val updated = graphQlTester
            .document(mutation)
            .variable("state", update.asMap())
            .execute()
            .path("updateState.id")
            .entity(String::class.java)
            .get()

        assertNotNull(updated)

        transaction {
            val actual = State.findById(UUID.fromString(updated))
            assertNotNull(actual)
            assertEquals(name, actual.name)
            assertEquals(country, actual.country)
        }
    }

    @Test
    fun `rest test update all`() {
        val state = sampleData.states.random()
        val name = "Name"
        val country = transaction { sampleData.countries.filterNot { c -> c.states.contains(state) }.random() }

        val update = StateInputDto(
            name = name,
            country = country.id.value,
        )
        val result = mockMvc
            .put("$BASE_URI/api/state/${state.id.value}") {
                contentType = MediaType.APPLICATION_JSON
                content = MAPPER.writeValueAsString(update)
            }
            .andExpect {
                status { isOk() }
                content { contentType(MEDIA_TYPE_APPLICATION_HAL_JSON) }
            }
            .andExpect { jsonPath("name") { value(name) } }
            .andExpect { jsonPath("id") { value(state.id.value.toString()) } }
            .andExpect { jsonPath("_links.self") { exists() } }
            .andExpect { jsonPath("_links.country") { exists() } }
            .andExpect { jsonPath("_links.cities") { exists() } }
            .andReturn()

        val updated = MAPPER.readTree(result.response.contentAsString).findValue("id").asText()

        transaction {
            val actual = State.findById(UUID.fromString(updated))
            assertNotNull(actual)
            assertEquals(name, actual.name)
            assertEquals(country, actual.country)
        }
    }

    @Test
    fun `graphql test delete`() {
        val id = sampleData.states.random().id.value

        transaction { assertNotNull(State.findById(id)) }

        val mutation = """
            mutation {
                deleteState(id: "$id")
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(mutation)
            .execute()
            .path("deleteState")
            .entity(Boolean::class.java)
            .get()

        assertTrue(actual)
        transaction { assertNull(State.findById(id)) }
    }

    @Test
    fun `rest test delete`() {
        val id = sampleData.states.random().id.value

        transaction { assertNotNull(State.findById(id)) }

        val result = mockMvc
            .delete("$BASE_URI/api/state/$id") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
            .andReturn()

        val actual = result.response.contentAsString.toBoolean()
        assertTrue(actual)
        transaction { assertNull(State.findById(id)) }
    }

    @Test
    @Ignore // TODO implement (get inspired by dto tests for graphql)
    fun `rest test get state country`() {
        fail("Not implemented yet")
    }

    @Test
    @Ignore // TODO implement (get inspired by dto tests for graphql)
    fun `rest test get state cities`() {
        fail("Not implemented yet")
    }
}
