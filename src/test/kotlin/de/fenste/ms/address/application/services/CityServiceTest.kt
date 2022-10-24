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

import de.fenste.ms.address.application.dtos.CityDto
import de.fenste.ms.address.application.dtos.CityInputDto
import de.fenste.ms.address.application.dtos.CountryDto
import de.fenste.ms.address.application.dtos.StateDto
import de.fenste.ms.address.domain.model.City
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
class CityServiceTest(
    @Autowired private val service: CityService,
) {

    @BeforeTest
    fun `set up`() {
        SampleData.reset()
    }

    @Test
    fun `test list on sample data`() {
        val expected = SampleData.cities.sortedBy { c -> c.id.value.toString() }.map { c -> CityDto(c) }
        val actual = service.list()

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `test list on sample data with options`() {
        val expected = SampleData.cities
            .sortedBy { c -> c.id.value.toString() }
            .drop(2)
            .take(1)
            .map { c -> CityDto(c) }
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
        val expected = SampleData.cities.random().let { c -> CityDto(c) }
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
        val country = transaction { SampleData.countries.filter { c -> c.states.empty() }.random() }

        val create = CityInputDto(
            name = name,
            country = country.id.value,
        )

        val actual = service.create(
            city = create,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        transaction { assertEquals((CountryDto(country)), actual.country) }
        assertNull(actual.state)
    }

    @Test
    fun `test create all`() {
        val country = transaction { SampleData.countries.filterNot { c -> c.states.empty() }.random() }
        val name = "Name"
        val state = transaction { country.states.toList().random() }

        val create = CityInputDto(
            name = name,
            country = country.id.value,
            state = state.id.value,
        )

        val actual = service.create(
            city = create,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        transaction { assertEquals((CountryDto(country)), actual.country) }
        assertNotNull(actual.state)
        transaction { assertEquals(StateDto(state), actual.state) }
    }

    @Test
    fun `test update all`() {
        val city = transaction { SampleData.cities.filterNot { c -> c.state == null }.random() }
        val name = "Name"
        val country = transaction {
            SampleData.countries.filterNot { c -> c == city.country || c.states.empty() }.random()
        }
        val state = transaction { country.states.toList().random() }

        val update = CityInputDto(
            name = name,
            country = country.id.value,
            state = state.id.value,
        )

        val actual = service.update(
            id = city.id.value,
            city = update,
        )

        assertNotNull(actual)
        assertEquals(name, actual.name)
        transaction { assertEquals(CountryDto(country), actual.country) }
        assertNotNull(actual.state)
        transaction { assertEquals(StateDto(state), actual.state) }
    }

    @Test
    @Ignore // TODO allow cascade deletion?
    fun `test delete`() {
        val id = SampleData.states.random().id.value

        service.delete(id)

        transaction { assertNull(City.findById(id)) }
    }
}
