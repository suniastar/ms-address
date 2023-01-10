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

import de.fenste.ms.address.application.dtos.CountryInputDto
import de.fenste.ms.address.domain.model.Country
import de.fenste.ms.address.test.SampleData
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.GraphQlTester
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
@AutoConfigureGraphQlTester
class CountryControllerTest(
    @Autowired private val graphQlTester: GraphQlTester,
) {

    @BeforeTest
    fun `set up`() {
        SampleData.reset()
    }

    @Test
    fun `list on sample data`() {
        val expected = SampleData.countries.map { c -> c.id.value.toString() }.sorted()

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
    fun `test list on sample data with options`() {
        val expected = SampleData.countries
            .map { c -> c.id.value.toString() }
            .sorted()
            .drop(2)
            .take(1)

        val query = """
            query {
                countries(offset: 2, limit: 1) {
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
    fun `test find by id on sample data`() {
        val expected = SampleData.countries.random().id.value.toString()

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
    fun `test find by alpha2 on non existing sample data`() {
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
    fun `test create`() {
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
        assertFalse(SampleData.countries.map { c -> c.id.value.toString() }.contains(created))

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
    fun `test update all`() {
        val country = SampleData.countries.random()
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
    fun `test delete`() {
        val id = SampleData.countries.random().id.value

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
}
