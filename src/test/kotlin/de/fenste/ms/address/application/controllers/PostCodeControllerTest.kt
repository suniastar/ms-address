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

import de.fenste.ms.address.application.dtos.PostCodeInputDto
import de.fenste.ms.address.domain.model.PostCode
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
class PostCodeControllerTest(
    @Autowired private val graphQlTester: GraphQlTester,
) {

    @BeforeTest
    fun `set up`() {
        SampleData.reset()
    }

    @Test
    fun `test list on sample data`() {
        val expected = SampleData.postCodes.map { p -> p.id.value.toString() }.sorted()

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
    fun `test list on sample data with options`() {
        val expected = SampleData.postCodes
            .map { p -> p.id.value.toString() }
            .sorted()
            .drop(2)
            .take(1)

        val query = """
            query {
                postCodes(offset: 2, limit: 1) {
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
    fun `test find by id on sample data`() {
        val expected = SampleData.postCodes.random().id.value.toString()

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
    fun `test find by id on non existing sample data`() {
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
    fun `test create`() {
        val code = "CODE"
        val city = transaction { SampleData.cities.random() }

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
        assertFalse(SampleData.postCodes.map { p -> p.id.value.toString() }.contains(created))

        transaction {
            val actual = PostCode.findById(UUID.fromString(created))
            assertNotNull(actual)
            assertEquals(code, actual.code)
            assertEquals(city, actual.city)
        }
    }

    @Test
    fun `test update all`() {
        val postCode = transaction { SampleData.postCodes.random() }
        val code = "CODE"
        val city = transaction { SampleData.cities.filterNot { c -> c.postCodes.contains(postCode) }.random() }

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
    fun `test delete`() {
        val id = SampleData.postCodes.random().id.value

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
}
