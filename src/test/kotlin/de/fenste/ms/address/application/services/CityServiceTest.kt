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
import de.fenste.ms.address.application.dtos.CityInputDto
import de.fenste.ms.address.application.dtos.PostCodeDto
import de.fenste.ms.address.application.dtos.StateDto
import de.fenste.ms.address.config.SampleDataConfig
import de.fenste.ms.address.domain.model.City
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("sample")
class CityServiceTest(
    @Autowired private val sampleData: SampleDataConfig,
    @Autowired private val service: CityService,
) {

    @BeforeTest
    fun `set up`() {
        sampleData.reset()
    }

    @Test
    fun `test count`(): Unit = transaction {
        val expected = sampleData.cities.count()
        val actual = service.count()

        assertEquals(expected, actual)
    }

    @Test
    fun `test find by id on sample data`() {
        val expected = sampleData.cities.random().let { c -> CityDto(c) }
        val actual = service.find(id = expected.id)

        transaction { assertEquals(expected, actual) }
    }

    @Test
    fun `test find by id on non existing sample data`() {
        val actual = service.find(id = UUID.randomUUID())

        assertNull(actual)
    }

    @Test
    fun `test list on sample data`() {
        val expected = sampleData.cities
            .sortedBy { c -> c.id.value.toString() }
            .map { c -> CityDto(c) }
        val actual = service.list()

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `test list on sample data with options`() {
        val expected = sampleData.cities
            .sortedWith(compareBy({ c -> c.name }, { c -> c.id.value.toString() }))
            .drop(1 * 2)
            .take(2)
            .map { c -> CityDto(c) }
        val actual = service.list(
            sort = "name,asc",
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
    fun `test get state on sample data`() {
        val city = transaction { sampleData.cities.random() }
        val expected = transaction { StateDto(city.state) }

        transaction {
            val actual = service.getState(city.id.value)

            assertEquals(expected, actual)
        }
    }

    @Test
    fun `test list post codes on sample data`() {
        val city = transaction { sampleData.cities.filterNot { c -> c.postCodes.empty() }.random() }
        val expected = transaction {
            city
                .postCodes
                .sortedBy { p -> p.id.value.toString() }
                .map { p -> PostCodeDto(p) }
        }

        transaction {
            val actual = service.listPostCodes(city.id.value)

            assertContentEquals(expected, actual)
        }
    }

    @Test
    fun `test list post codes on sample data with options`() {
        val city = transaction { sampleData.cities.filterNot { c -> c.postCodes.empty() }.random() }
        val expected = transaction {
            city
                .postCodes
                .sortedWith(compareBy({ p -> p.code }, { p -> p.id.value.toString() }))
                .drop(1 * 2)
                .take(2)
                .map { p -> PostCodeDto(p) }
        }

        transaction {
            val actual = service.listPostCodes(
                id = city.id.value,
                sort = "code,asc",
                page = 1,
                size = 2,
            )

            assertContentEquals(expected, actual)
        }
    }

    @Test
    fun `test create`() {
        val name = "Name"
        val state = sampleData.states.random()

        val create = CityInputDto(
            name = name,
            state = state.id.value,
        )

        val actual = service.create(
            city = create,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        transaction {
            val created = City.findById(actual.id)
            assertNotNull(created)
        }
    }

    @Test
    fun `test update all`() {
        val city = transaction { sampleData.cities.random() }
        val name = "Name"
        val state = transaction { sampleData.states.filter { s -> s.cities.empty() }.random() }

        val update = CityInputDto(
            name = name,
            state = state.id.value,
        )

        val actual = service.update(
            id = city.id.value,
            city = update,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        transaction {
            val updated = City.findById(actual.id)
            assertNotNull(updated)
        }
    }

    @Test
    fun `test delete`() {
        val id = sampleData.cities.random().id.value

        transaction { assertNotNull(City.findById(id)) }

        service.delete(id)

        transaction { assertNull(City.findById(id)) }
    }
}
