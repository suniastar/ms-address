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

package de.fenste.ms.address.application.services

import de.fenste.ms.address.application.dtos.PostCodeDto
import de.fenste.ms.address.application.dtos.StreetDto
import de.fenste.ms.address.application.dtos.StreetInputDto
import de.fenste.ms.address.domain.model.Street
import de.fenste.ms.address.test.SampleData
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
class StreetServiceTest(
    @Autowired private val service: StreetService,
) {

    @BeforeTest
    fun `set up`() {
        SampleData.reset()
    }

    @Test
    fun `test list on sample data`() {
        val expected = SampleData.streets.sortedBy { s -> s.id.value.toString() }.map { s -> StreetDto(s) }
        val actual = service.list()

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `test list on sample data with options`() {
        val expected = SampleData.streets
            .sortedBy { s -> s.id.value.toString() }
            .drop(2)
            .take(1)
            .map { s -> StreetDto(s) }
        val actual = service.list(
            offset = 2,
            limit = 1,
        )

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `test list on no data`() {
        SampleData.clear()
        val list = service.list()

        assertNull(list)
    }

    @Test
    fun `test find by id on sample data`() {
        val expected = SampleData.streets.random().let { s -> StreetDto(s) }
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
        val name = "Name"
        val postCode = SampleData.postCodes.random()

        val create = StreetInputDto(
            name = name,
            postCode = postCode.id.value,
        )

        val actual = service.create(
            street = create,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        transaction { assertEquals(PostCodeDto(postCode), actual.postCode) }
    }

    @Test
    fun `test update all`() {
        val state = SampleData.streets.random()
        val name = "Name"
        val postCode = transaction { SampleData.postCodes.filterNot { p -> p.streets.contains(state) }.random() }

        val update = StreetInputDto(
            name = name,
            postCode = postCode.id.value,
        )

        val actual = service.update(
            id = state.id.value,
            street = update,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        transaction { assertEquals(PostCodeDto(postCode), actual.postCode) }
    }

    @Test
    fun `test delete`() {
        val id = SampleData.streets.random().id.value

        transaction { assertNotNull(Street.findById(id)) }

        service.delete(id)

        transaction { assertNull(Street.findById(id)) }
    }
}