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

import de.fenste.ms.address.application.dtos.StateInputDto
import de.fenste.ms.address.domain.model.State
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
class StateControllerTest(
    @Autowired private val graphQlTester: GraphQlTester,
) {

    @BeforeTest
    fun `set up`() {
        SampleData.reset()
    }

    @Test
    fun `test list on sample data`() {
        val expected = SampleData.states.map { s -> s.id.value.toString() }.sorted()

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
    fun `test list on sample data with options`() {
        val expected = SampleData.states
            .map { s -> s.id.value.toString() }
            .sorted()
            .drop(2)
            .take(1)

        val query = """
            query {
                states(offset: 2, limit: 1) {
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
    fun `test find by id on sample data`() {
        val expected = SampleData.states.random().id.value.toString()

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
    fun `test find by id on non existing sample data`() {
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
    fun `test create`() {
        val name = "Name"
        val country = SampleData.countries.random()

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
        assertFalse(SampleData.states.map { s -> s.id.value.toString() }.contains(created))

        transaction {
            val actual = State.findById(UUID.fromString(created))
            assertNotNull(actual)
            assertEquals(name, actual.name)
            assertEquals(country, actual.country)
        }
    }

    @Test
    fun `test update all`() {
        val state = SampleData.states.random()
        val name = "Name"
        val country = transaction { SampleData.countries.filterNot { c -> c.states.contains(state) }.random() }

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
    fun `test delete`() {
        val id = SampleData.states.random().id.value

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
}
