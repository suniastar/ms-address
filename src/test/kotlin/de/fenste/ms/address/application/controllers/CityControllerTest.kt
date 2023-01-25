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

import de.fenste.ms.address.application.dtos.CityInputDto
import de.fenste.ms.address.config.SampleDataConfig
import de.fenste.ms.address.domain.model.City
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
class CityControllerTest(
    @Autowired private val sampleData: SampleDataConfig,
    @Autowired private val graphQlTester: GraphQlTester,
) {

    @BeforeTest
    fun `set up`() {
        sampleData.reset()
    }

    @Test
    fun `test list on sample data`() {
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
    @Ignore
    fun `test list on sample data with options`() {
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
    fun `test find by id on sample data`() {
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
    fun `test find by id on non existing sample data`() {
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
    fun `test create`() {
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
    fun `test update all`() {
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
    fun `test delete`() {
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
}
