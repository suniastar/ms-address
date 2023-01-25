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

package de.fenste.ms.address.application.services

import de.fenste.ms.address.application.dtos.CityDto
import de.fenste.ms.address.application.dtos.PostCodeDto
import de.fenste.ms.address.application.dtos.PostCodeInputDto
import de.fenste.ms.address.config.SampleDataConfig
import de.fenste.ms.address.domain.model.PostCode
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("sample")
class PostCodeServiceTest(
    @Autowired private val sampleData: SampleDataConfig,
    @Autowired private val service: PostCodeService,
) {

    @BeforeTest
    fun `set up`() {
        sampleData.reset()
    }

    @Test
    fun `test count`(): Unit = transaction {
        val expected = sampleData.postCodes.count().toLong()
        val actual = service.count()

        assertEquals(expected, actual)
    }

    @Test
    fun `test list on sample data`() {
        val expected = sampleData.postCodes
            .sortedBy { p -> p.id.value.toString() }
            .map { p -> PostCodeDto(p) }
        val actual = service.list()

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    @Ignore
    fun `test list on sample data with options`() {
        val expected = sampleData.postCodes
            .sortedWith(compareBy({ p -> p.code }, { p -> p.id }))
            .drop(1 * 2)
            .take(2)
            .map { p -> PostCodeDto(p) }
        val actual = service.list(
            sort = "code,asc",
            page = 1,
            size = 2,
        )

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `test list on no data`() {
        sampleData.clear()
        val list = service.list()

        assertNotNull(list)
        assertTrue(list.isEmpty())
    }

    @Test
    fun `test find by id on sample data`() {
        val expected = sampleData.postCodes.random().let { p -> PostCodeDto(p) }
        val actual = service.find(id = expected.id)

        transaction { assertEquals(expected, actual) }
    }

    @Test
    fun `test find by id on non existing sample data`() {
        val actual = service.find(id = UUID.randomUUID())

        assertNull(actual)
    }

    @Test
    fun `test create`() {
        val code = "CODE"
        val city = transaction { sampleData.cities.random() }
        val create = PostCodeInputDto(
            code = code,
            city = city.id.value,
        )

        val actual = service.create(
            postCode = create,
        )

        assertNotNull(actual)
        assertEquals(code, actual.code)
        transaction { assertEquals(CityDto(city), actual.city) }
    }

    @Test
    fun `test update all`() {
        val postCode = transaction { sampleData.postCodes.random() }
        val code = "CODE"
        val city = transaction {
            sampleData.cities.filterNot { c -> c.postCodes.contains(postCode) }.random()
        }
        val update = PostCodeInputDto(
            code = code,
            city = city.id.value,
        )

        val actual = service.update(
            id = postCode.id.value,
            postCode = update,
        )

        assertNotNull(actual)
        assertEquals(code, actual.code)
        transaction { assertEquals(CityDto(city), actual.city) }
    }

    @Test
    fun `test delete`() {
        val id = sampleData.postCodes.random().id.value

        transaction { assertNotNull(PostCode.findById(id)) }

        service.delete(id)

        transaction { assertNull(PostCode.findById(id)) }
    }
}
