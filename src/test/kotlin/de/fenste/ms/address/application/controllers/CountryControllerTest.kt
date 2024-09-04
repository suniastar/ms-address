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
import de.fenste.ms.address.application.dtos.CountryDto
import de.fenste.ms.address.application.dtos.CountryInputDto
import de.fenste.ms.address.config.SampleDataConfig
import de.fenste.ms.address.domain.model.Country
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
class CountryControllerTest(
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
    fun `graphql test find by id on sample data`() {
        val expected = sampleData.countries.random().id.value.toString()

        val query = """
            query {
                country(id: "$expected") {
                    id
                }
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(query)
            .execute()
            .path("country.id")
            .entity(String::class.java)
            .get()

        assertEquals(expected, actual)
    }

    @Test
    fun `rest test find by id on sample data`() {
        val expected = sampleData.countries.random().let { c -> CountryDto(c) }

        mockMvc
            .get("$BASE_URI/api/country/${expected.id}") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isOk() }
                content { contentType(MEDIA_TYPE_APPLICATION_HAL_JSON) }
            }
            .andExpect { jsonPath("name") { value(expected.name) } }
            .andExpect { jsonPath("id") { value(expected.id.toString()) } }
            .andExpect { jsonPath("localizedName") { value(expected.localizedName) } }
            .andExpect { jsonPath("alpha2") { value(expected.alpha2) } }
            .andExpect { jsonPath("alpha3") { value(expected.alpha3) } }
            .andExpect { jsonPath("_links.self") { exists() } }
            .andExpect { jsonPath("_links.states") { exists() } }
            .andExpect { jsonPath("_links.cities") { exists() } }
    }

    @Test
    fun `graphql test find by id on non existing sample data`() {
        val query = """
            query {
                country(id: "${UUID.randomUUID()}") {
                    id
                }
            }
        """.trimIndent()

        graphQlTester
            .document(query)
            .execute()
            .path("country")
            .valueIsNull()
    }

    @Test
    fun `rest test find by id on non existing sample data`() {
        mockMvc
            .get("$BASE_URI/api/country/${UUID.randomUUID()}") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `graphql list on sample data`() {
        val expected = sampleData.countries
            .sortedBy { c -> c.id.value.toString() }
            .map { c -> c.id.value.toString() }

        val query = """
            query {
                countries {
                    id
                }
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(query)
            .execute()
            .path("countries.[*].id")
            .entityList(String::class.java)
            .get()
            .toList()

        assertContentEquals(expected, actual)
    }

    @Test
    fun `rest list on sample data`() {
        val expected = sampleData.countries
            .sortedBy { c -> c.id.value.toString() }
            .map { c -> c.id.value.toString() }

        mockMvc
            .get("$BASE_URI/api/country") {
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
            .andExpect { jsonPath("_embedded.countryDtoes.[*].id") { value(expected) } }
    }

    @Test
    fun `graphql test list on sample data with options`() {
        val expected = sampleData.countries
            .sortedWith(compareBy({ c -> c.name }, { c -> c.id }))
            .drop(1 * 2)
            .take(2)
            .map { c -> c.id.value.toString() }

        val query = """
            query {
                countries(sort: "name,asc", page: 1, size: 2) {
                    id
                }
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(query)
            .execute()
            .path("countries.[*].id")
            .entityList(String::class.java)
            .get()
            .toList()

        assertContentEquals(expected, actual)
    }

    @Test
    fun `rest test list on sample data with options`() {
        val expected = sampleData.countries
            .sortedWith(compareBy({ c -> c.name }, { c -> c.id }))
            .drop(1 * 2)
            .take(2)
            .map { c -> c.id.value.toString() }

        mockMvc
            .get("$BASE_URI/api/country?page=1&size=2&sort=name,asc") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isOk() }
                content { contentType(MEDIA_TYPE_APPLICATION_HAL_JSON) }
            }
            .andExpect { jsonPath("page.size") { value(expected.count()) } }
            .andExpect { jsonPath("page.totalElements") { value(sampleData.countries.count()) } }
            .andExpect { jsonPath("page.number") { value(1) } }
            .andExpect { jsonPath("page.totalPages") { value(ceil(sampleData.countries.count() / 2f)) } }
            .andExpect { jsonPath("_links.first.href") { exists() } }
            .andExpect { jsonPath("_links.prev.href") { exists() } }
            .andExpect { jsonPath("_links.self.href") { exists() } }
            .andExpect { jsonPath("_links.next.href") { doesNotExist() } }
            .andExpect { jsonPath("_links.last.href") { exists() } }
            .andExpect { jsonPath("_embedded.countryDtoes.[*].id") { value(expected) } }
    }

    @Test
    fun `rest test get country states on sample data`() {
        val country = transaction { sampleData.countries.filterNot { c -> c.states.empty() }.random() }
        val expected = transaction {
            country.states
                .sortedBy { s -> s.id.value.toString() }
                .map { s -> s.id.value.toString() }
        }

        mockMvc
            .get("$BASE_URI/api/country/${country.id.value}/states") {
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
    fun `rest test get country states on sample data with options`() {
        val country = transaction { sampleData.countries.filterNot { c -> c.states.empty() }.random() }
        val expected = transaction {
            country.states
                .sortedWith(compareBy({ s -> s.name }, { s -> s.id.value.toString() }))
                .drop(1 * 1)
                .take(1)
                .map { s -> s.id.value.toString() }
        }

        mockMvc
            .get("$BASE_URI/api/country/${country.id.value}/states?sort=name,asc&page=1&size=1") {
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
    @kotlin.test.Ignore
    fun `rest test get country cities on non existing sample data`() {
        mockMvc
            .get("$BASE_URI/api/country/${UUID.randomUUID()}/cities") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `graphql test create`() {
        val alpha2 = "CZ"
        val alpha3 = "CZE"
        val name = "Czechia"
        val localizedName = "Tschechien"

        val mutation = """
            mutation CreateCountryMutation(${D}country: CountryInput!) {
                createCountry(country: ${D}country) {
                    id
                }
            }
        """.trimIndent()
        val create = CountryInputDto(
            alpha2 = alpha2,
            alpha3 = alpha3,
            name = name,
            localizedName = localizedName,
        )

        val created = graphQlTester
            .document(mutation)
            .variable("country", create.asMap())
            .execute()
            .path("createCountry.id")
            .entity(String::class.java)
            .get()

        assertNotNull(created)
        assertFalse(sampleData.countries.map { c -> c.id.value.toString() }.contains(created))

        transaction {
            val actual = Country.findById(UUID.fromString(created))
            assertNotNull(actual)
            assertEquals(alpha2, actual.alpha2)
            assertEquals(alpha3, actual.alpha3)
            assertEquals(name, actual.name)
            assertEquals(localizedName, actual.localizedName)
        }
    }

    @Test
    fun `rest test create`() {
        val alpha2 = "CZ"
        val alpha3 = "CZE"
        val name = "Czechia"
        val localizedName = "Tschechien"

        val create = CountryInputDto(
            alpha2 = alpha2,
            alpha3 = alpha3,
            name = name,
            localizedName = localizedName,
        )
        val result = mockMvc
            .post("$BASE_URI/api/country") {
                contentType = MediaType.APPLICATION_JSON
                content = MAPPER.writeValueAsString(create)
            }
            .andExpect {
                status { isOk() }
                content { contentType(MEDIA_TYPE_APPLICATION_HAL_JSON) }
            }
            .andExpect { jsonPath("name") { value(name) } }
            .andExpect { jsonPath("id") { exists() } }
            .andExpect { jsonPath("localizedName") { value(localizedName) } }
            .andExpect { jsonPath("alpha2") { value(alpha2) } }
            .andExpect { jsonPath("alpha3") { value(alpha3) } }
            .andExpect { jsonPath("_links.self") { exists() } }
            .andExpect { jsonPath("_links.states") { exists() } }
            .andExpect { jsonPath("_links.cities") { exists() } }
            .andReturn()

        val created = MAPPER.readTree(result.response.contentAsString).findValue("id").asText()
        assertFalse(sampleData.countries.map { c -> c.id.value.toString() }.contains(created))

        transaction {
            val actual = Country.findById(UUID.fromString(created))
            assertNotNull(actual)
            assertEquals(alpha2, actual.alpha2)
            assertEquals(alpha3, actual.alpha3)
            assertEquals(name, actual.name)
            assertEquals(localizedName, actual.localizedName)
        }
    }

    @Test
    fun `graphql test update all`() {
        val country = sampleData.countries.random()
        val alpha2 = "XX"
        val alpha3 = "XXX"
        val name = "Name"
        val localizedName = "LocalizedName"

        val mutation = """
            mutation UpdateCountryMutation(${D}country: CountryInput!) {
                updateCountry(id: "${country.id.value}",country: ${D}country) {
                    id
                }
            }
        """.trimIndent()
        val update = CountryInputDto(
            alpha2 = alpha2,
            alpha3 = alpha3,
            name = name,
            localizedName = localizedName,
        )

        val updated = graphQlTester
            .document(mutation)
            .variable("country", update.asMap())
            .execute()
            .path("updateCountry.id")
            .entity(String::class.java)
            .get()

        assertNotNull(updated)

        transaction {
            val actual = Country.findById(UUID.fromString(updated))
            assertNotNull(actual)
            assertEquals(alpha2, actual.alpha2)
            assertEquals(alpha3, actual.alpha3)
            assertEquals(name, actual.name)
            assertEquals(localizedName, actual.localizedName)
        }
    }

    @Test
    fun `rest test update all`() {
        val country = sampleData.countries.random()
        val alpha2 = "XX"
        val alpha3 = "XXX"
        val name = "Name"
        val localizedName = "LocalizedName"

        val update = CountryInputDto(
            alpha2 = alpha2,
            alpha3 = alpha3,
            name = name,
            localizedName = localizedName,
        )
        val result = mockMvc
            .put("$BASE_URI/api/country/${country.id.value}") {
                contentType = MediaType.APPLICATION_JSON
                content = MAPPER.writeValueAsString(update)
            }
            .andExpect {
                status { isOk() }
                content { contentType(MEDIA_TYPE_APPLICATION_HAL_JSON) }
            }
            .andExpect { jsonPath("name") { value(name) } }
            .andExpect { jsonPath("id") { value(country.id.value.toString()) } }
            .andExpect { jsonPath("localizedName") { value(localizedName) } }
            .andExpect { jsonPath("alpha2") { value(alpha2) } }
            .andExpect { jsonPath("alpha3") { value(alpha3) } }
            .andExpect { jsonPath("_links.self") { exists() } }
            .andExpect { jsonPath("_links.states") { exists() } }
            .andExpect { jsonPath("_links.cities") { exists() } }
            .andReturn()

        val updated = MAPPER.readTree(result.response.contentAsString).findValue("id").asText()

        transaction {
            val actual = Country.findById(UUID.fromString(updated))
            assertNotNull(actual)
            assertEquals(alpha2, actual.alpha2)
            assertEquals(alpha3, actual.alpha3)
            assertEquals(name, actual.name)
            assertEquals(localizedName, actual.localizedName)
        }
    }

    @Test
    fun `graphql test delete`() {
        val id = sampleData.countries.random().id.value

        transaction { assertNotNull(Country.findById(id)) }

        val mutation = """
            mutation {
                deleteCountry(id: "$id")
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(mutation)
            .execute()
            .path("deleteCountry")
            .entity(Boolean::class.java)
            .get()

        assertTrue(actual)
        transaction { assertNull(Country.findById(id)) }
    }

    @Test
    fun `rest test delete`() {
        val id = sampleData.countries.random().id.value

        transaction { assertNotNull(Country.findById(id)) }

        val result = mockMvc
            .delete("$BASE_URI/api/country/$id") {
                contentType = MediaType.APPLICATION_JSON
            }
            .andExpect {
                status { isOk() }
                content { contentType(MediaType.APPLICATION_JSON) }
            }
            .andReturn()

        val actual = result.response.contentAsString.toBoolean()
        assertTrue(actual)
        transaction { assertNull(Country.findById(id)) }
    }
}
