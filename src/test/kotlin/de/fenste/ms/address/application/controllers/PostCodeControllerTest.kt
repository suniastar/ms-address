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

import de.fenste.ms.address.application.dtos.CityDto
import de.fenste.ms.address.application.dtos.PostCodeDto
import de.fenste.ms.address.application.dtos.PostCodeInputDto
import de.fenste.ms.address.domain.model.PostCode
import de.fenste.ms.address.test.SampleData
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
class PostCodeControllerTest(
    @Autowired private val controller: PostCodeController,
) {

    @BeforeTest
    fun `set up`() {
        SampleData.reset()
    }

    @Test
    fun `test list on sample data`() {
        val expected = SampleData.postCodes.sortedBy { p -> p.id.value.toString() }.map { p -> PostCodeDto(p) }
        val actual = controller.postCodes()

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `test list on sample data with options`() {
        val expected = SampleData.postCodes
            .sortedBy { p -> p.id.value.toString() }
            .drop(2)
            .take(1)
            .map { p -> PostCodeDto(p) }
        val actual = controller.postCodes(
            offset = 2,
            limit = 1,
        )

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `test find by id on sample data`() {
        val expected = SampleData.postCodes.random().let { p -> PostCodeDto(p) }
        val actual = controller.postCode(id = expected.id)

        transaction { assertEquals(expected, actual) }
    }

    @Test
    fun `test find by id on non existing sample data`() {
        val actual = controller.postCode(id = UUID.randomUUID())

        assertNull(actual)
    }

    @Test
    fun `test create`() {
        val code = "CODE"
        val city = transaction { SampleData.cities.random() }
        val create = PostCodeInputDto(
            code = code,
            city = city.id.value,
        )

        val actual = controller.createPostCode(
            postCode = create,
        )

        assertNotNull(actual)
        assertEquals(code, actual.code)
        transaction { assertEquals(CityDto(city), actual.city) }
    }

    @Test
    fun `test update all`() {
        val postCode = transaction { SampleData.postCodes.random() }
        val code = "CODE"
        val city = transaction { SampleData.cities.filterNot { c -> c.postCodes.contains(postCode) }.random() }
        val update = PostCodeInputDto(
            code = code,
            city = city.id.value,
        )

        val actual = controller.updatePostCode(
            id = postCode.id.value,
            postCode = update,
        )

        assertNotNull(actual)
        assertEquals(code, actual.code)
        transaction { assertEquals(CityDto(city), actual.city) }
    }

    @Test
    @Ignore // TODO allow cascade deletion?
    fun `test delete`() {
        val id = SampleData.postCodes.random().id.value

        controller.deletePostCode(id)

        transaction { assertNull(PostCode.findById(id)) }
    }
}
