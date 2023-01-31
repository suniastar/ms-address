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
import de.fenste.ms.address.application.dtos.CityInputDto
import de.fenste.ms.address.config.SampleDataConfig
import de.fenste.ms.address.domain.model.City
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
class CityControllerTest(
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
        val expected = sampleData.cities
            .sortedBy { c -> c.id.value.toString() }
            .map { c -> c.id.value.toString() }

        val query = """
            query {
                cities {
                    id
                }
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(query)
            .execute()
            .path("cities.[*].id")
            .entityList(String::class.java)
            .get()
            .toList()

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `rest test list on sample data`() {
        val expected = sampleData.cities
            .sortedBy { c -> c.id.value.toString() }
            .map { c -> c.id.value.toString() }

        mockMvc
            .get("$BASE_URI/api/city") {
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
            .andExpect { jsonPath("_embedded.cityDtoes.[*].id") { value(expected) } }
    }

    @Test
    fun `graphql test list on sample data with options`() {
        val expected = sampleData.cities
            .sortedWith(compareBy({ c -> c.name }, { c -> c.id }))
            .drop(1 * 2)
            .take(2)
            .map { c -> c.id.value.toString() }

        val query = """
            query {
                cities(sort: "name,asc", page: 1, size: 2) {
                    id
                }
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(query)
            .execute()
            .path("cities.[*].id")
            .entityList(String::class.java)
            .get()
            .toList()

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `rest test list on sample data with options`() {
        val expected = sampleData.cities
            .sortedWith(compareBy({ c -> c.name }, { c -> c.id }))
            .drop(1 * 2)
            .take(2)
            .map { c -> c.id.value.toString() }

        mockMvc
            .get("$BASE_URI/api/city?page=1&size=2&sort=name,asc") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isOk() }
                content { contentType(MEDIA_TYPE_APPLICATION_HAL_JSON) }
            }
            .andExpect { jsonPath("page.size") { value(expected.count()) } }
            .andExpect { jsonPath("page.totalElements") { value(sampleData.cities.count()) } }
            .andExpect { jsonPath("page.number") { value(1) } }
            .andExpect { jsonPath("page.totalPages") { value(ceil(sampleData.cities.count() / 2f)) } }
            .andExpect { jsonPath("_links.first.href") { exists() } }
            .andExpect { jsonPath("_links.prev.href") { exists() } }
            .andExpect { jsonPath("_links.self.href") { exists() } }
            .andExpect { jsonPath("_links.next.href") { exists() } }
            .andExpect { jsonPath("_links.last.href") { exists() } }
            .andExpect { jsonPath("_embedded.cityDtoes.[*].id") { value(expected) } }
    }

    @Test
    fun `graphql test find by id on sample data`() {
        val expected = sampleData.cities.random().id.value.toString()

        val query = """
            query {
                city(id: "$expected") {
                    id
                }
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(query)
            .execute()
            .path("city.id")
            .entity(String::class.java)
            .get()

        transaction { assertEquals(expected, actual) }
    }

    @Test
    fun `rest test find by id on sample data`() {
        val expected = sampleData.cities.random().let { c -> CityDto(c) }

        mockMvc
            .get("$BASE_URI/api/city/${expected.id}") {
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
    fun `graphql test find by id on non existing sample data`() {
        val query = """
            query {
                city(id: "${UUID.randomUUID()}") {
                    id
                }
            }
        """.trimIndent()

        graphQlTester
            .document(query)
            .execute()
            .path("city")
            .valueIsNull()
    }

    @Test
    fun `rest test find by id on non existing sample data`() {
        mockMvc
            .get("$BASE_URI/api/city/${UUID.randomUUID()}") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `graphql test create`() {
        val name = "Name"
        val country = transaction { sampleData.countries.filter { c -> c.states.empty() }.random() }

        val mutation = """
            mutation CreateCityMutation(${D}city: CityInput!) {
                createCity(city: ${D}city) {
                    id
                }
            }
        """.trimIndent()
        val create = CityInputDto(
            name = name,
            country = country.id.value,
        )

        val created = graphQlTester
            .document(mutation)
            .variable("city", create.asMap())
            .execute()
            .path("createCity.id")
            .entity(String::class.java)
            .get()

        assertNotNull(created)
        assertFalse(sampleData.cities.map { c -> c.id.value.toString() }.contains(created))

        transaction {
            val actual = City.findById(UUID.fromString(created))
            assertNotNull(actual)
            assertEquals(name, actual.name)
            assertEquals(country, actual.country)
            assertNull(actual.state)
        }
    }

    @Test
    fun `rest test create`() {
        val name = "Name"
        val country = transaction { sampleData.countries.filter { c -> c.states.empty() }.random() }

        val create = CityInputDto(
            name = name,
            country = country.id.value,
        )
        val result = mockMvc
            .post("$BASE_URI/api/city") {
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
            .andExpect { jsonPath("_links.state") { exists() } }
            .andExpect { jsonPath("_links.postcodes") { exists() } }
            .andReturn()

        val created = MAPPER.readTree(result.response.contentAsString).findValue("id").asText()
        assertFalse(sampleData.cities.map { c -> c.id.value.toString() }.contains(created))

        transaction {
            val actual = City.findById(UUID.fromString(created))
            assertNotNull(actual)
            assertEquals(name, actual.name)
            assertEquals(country, actual.country)
            assertNull(actual.state)
        }
    }

    @Test
    fun `graphql test update all`() {
        val city = transaction { sampleData.cities.filterNot { c -> c.state == null }.random() }
        val name = "Name"
        val country = transaction {
            sampleData.countries.filterNot { c -> c == city.country || c.states.empty() }.random()
        }
        val state = transaction { country.states.toList().random() }

        val mutation = """
            mutation UpdateCityMutation(${D}city: CityInput!) {
                updateCity(id: "${city.id.value}", city: ${D}city) {
                    id
                }
            }
        """.trimIndent()
        val update = CityInputDto(
            name = name,
            country = country.id.value,
            state = state.id.value,
        )

        val updated = graphQlTester
            .document(mutation)
            .variable("city", update.asMap())
            .execute()
            .path("updateCity.id")
            .entity(String::class.java)
            .get()

        assertNotNull(updated)

        transaction {
            val actual = City.findById(UUID.fromString(updated))
            assertNotNull(actual)
            assertEquals(name, actual.name)
            assertEquals(country, actual.country)
            assertNotNull(actual.state)
            assertEquals(state, actual.state)
        }
    }

    @Test
    fun `rest test update all`() {
        val city = transaction { sampleData.cities.filterNot { c -> c.state == null }.random() }
        val name = "Name"
        val country = transaction {
            sampleData.countries.filterNot { c -> c == city.country || c.states.empty() }.random()
        }
        val state = transaction { country.states.toList().random() }

        val update = CityInputDto(
            name = name,
            country = country.id.value,
            state = state.id.value,
        )
        val result = mockMvc
            .put("$BASE_URI/api/city/${city.id.value}") {
                contentType = MediaType.APPLICATION_JSON
                content = MAPPER.writeValueAsString(update)
            }
            .andExpect {
                status { isOk() }
                content { contentType(MEDIA_TYPE_APPLICATION_HAL_JSON) }
            }
            .andExpect { jsonPath("name") { value(name) } }
            .andExpect { jsonPath("id") { value(city.id.value.toString()) } }
            .andExpect { jsonPath("_links.self") { exists() } }
            .andExpect { jsonPath("_links.country") { exists() } }
            .andExpect { jsonPath("_links.state") { exists() } }
            .andExpect { jsonPath("_links.postcodes") { exists() } }
            .andReturn()

        val updated = MAPPER.readTree(result.response.contentAsString).findValue("id").asText()

        transaction {
            val actual = City.findById(UUID.fromString(updated))
            assertNotNull(actual)
            assertEquals(name, actual.name)
            assertEquals(country, actual.country)
            assertNotNull(actual.state)
            assertEquals(state, actual.state)
        }
    }

    @Test
    fun `graphql test delete`() {
        val id = sampleData.cities.random().id.value

        transaction { assertNotNull(City.findById(id)) }

        val mutation = """
            mutation {
                deleteCity(id: "$id")
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(mutation)
            .execute()
            .path("deleteCity")
            .entity(Boolean::class.java)
            .get()

        assertTrue(actual)
        transaction { assertNull(City.findById(id)) }
    }

    @Test
    fun `rest test delete`() {
        val id = sampleData.cities.random().id.value

        transaction { assertNotNull(City.findById(id)) }

        val result = mockMvc
            .delete("$BASE_URI/api/city/$id") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
            .andReturn()

        val actual = result.response.contentAsString.toBoolean()
        assertTrue(actual)
        transaction { assertNull(City.findById(id)) }
    }

    @Test
    @Ignore // TODO implement (get inspired by dto tests for graphql)
    fun `rest test get city country`() {
        fail("Not implemented yet")
    }

    @Test
    @Ignore // TODO implement (get inspired by dto tests for graphql)
    fun `rest test get city state`() {
        fail("Not implemented yet")
    }

    @Test
    @Ignore // TODO implement (get inspired by dto tests for graphql)
    fun `rest test get city post codes`() {
        fail("Not implemented yet")
    }
}
