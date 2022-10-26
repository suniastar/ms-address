/*
 * Copyright (c) 2022 Frederik Enste <frederik@fenste.de>.
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

import de.fenste.ms.address.application.dtos.StreetInputDto
import de.fenste.ms.address.domain.model.Street
import de.fenste.ms.address.test.SampleData
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.GraphQlTester
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
@AutoConfigureGraphQlTester
class StreetControllerTest(
    @Autowired private val graphQlTester: GraphQlTester,
) {

    @BeforeTest
    fun `set up`() {
        SampleData.reset()
    }

    @Test
    fun `test list on sample data`() {
        val expected = SampleData.streets.map { s -> s.id.value.toString() }.sorted()

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
    fun `test list on sample data with options`() {
        val expected = SampleData.streets
            .map { s -> s.id.value.toString() }
            .sorted()
            .drop(2)
            .take(1)

        val query = """
            query {
                streets(offset: 2, limit: 1) {
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
    fun `test find by id on sample data`() {
        val expected = SampleData.streets.random().id.value.toString()

        val query = """
            query {
                street(id: "$expected") {
                    id
                }
            }
        """.trimIndent()

        val actual = graphQlTester
            .document(query)
            .execute()
            .path("street.id")
            .entity(String::class.java)
            .get()

        transaction { assertEquals(expected, actual) }
    }

    @Test
    fun `test find by id on non existing sample data`() {
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
    fun `test create`() {
        val name = "Name"
        val postCode = SampleData.postCodes.random()

        val mutation = """
            mutation CreateStreetMutation(${D}street: StreetInput!) {
                createStreet(street: ${D}street) {
                    id
                }
            }
        """.trimIndent()
        val create = StreetInputDto(
            name = name,
            postCode = postCode.id.value,
        )

        val created = graphQlTester
            .document(mutation)
            .variable("street", create.asMap())
            .execute()
            .path("createStreet.id")
            .entity(String::class.java)
            .get()

        assertNotNull(created)
        assertFalse(SampleData.streets.map { s -> s.id.value.toString() }.contains(created))

        transaction {
            val actual = Street.findById(UUID.fromString(created))
            assertNotNull(actual)
            assertEquals(name, actual.name)
            assertEquals(postCode, actual.postCode)
        }
    }

    @Test
    fun `test update all`() {
        val state = SampleData.streets.random()
        val name = "Name"
        val postCode = transaction { SampleData.postCodes.filterNot { p -> p.streets.contains(state) }.random() }

        val mutation = """
            mutation UpdateStreetMutation(${D}street: StreetInput!) {
                updateStreet(id: "${state.id.value}", street: ${D}street) {
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
    @Ignore // TODO allow cascade deletion?
    fun `test delete`() {
        val id = SampleData.states.random().id.value

        assertNull(Street.findById(id))
    }
}
