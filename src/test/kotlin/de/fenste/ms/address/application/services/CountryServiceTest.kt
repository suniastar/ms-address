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
import de.fenste.ms.address.application.dtos.CountryDto
import de.fenste.ms.address.application.dtos.CountryInputDto
import de.fenste.ms.address.application.dtos.StateDto
import de.fenste.ms.address.config.SampleDataConfig
import de.fenste.ms.address.domain.model.Country
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("sample")
class CountryServiceTest(
    @Autowired private val sampleData: SampleDataConfig,
    @Autowired private val service: CountryService,
) {

    @BeforeTest
    fun `set up`() {
        sampleData.reset()
    }

    @Test
    fun `test count`(): Unit = transaction {
        val expected = sampleData.countries.count()
        val actual = service.count()

        assertEquals(expected, actual)
    }

    @Test
    fun `test find by id on sample data`() {
        val expected = sampleData.countries.random().let { c -> CountryDto(c) }
        val actual = service.find(id = expected.id)

        assertEquals(expected, actual)
    }

    @Test
    fun `test find by alpha2 on non existing sample data`() {
        val actual = service.find(alpha2 = "XX")

        assertNull(actual)
    }

    @Test
    fun `list on sample data`() {
        val expected = sampleData.countries
            .sortedBy { c -> c.id.value.toString() }
            .map { c -> CountryDto(c) }
        val actual = service.list()

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test list on sample data with options`() {
        val expected = sampleData.countries
            .sortedWith(compareBy({ c -> c.name }, { c -> c.id.value.toString() }))
            .drop(1 * 2)
            .take(2)
            .map { c -> CountryDto(c) }
        val actual = service.list(
            sort = "name,asc",
            page = 1,
            size = 2,
        )

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test list on no data`() {
        sampleData.clear()
        val list = service.list()

        assertNotNull(list)
        assertTrue(list.isEmpty())
    }

    @Test
    fun `test list states on sample data`() {
        val country = transaction { sampleData.countries.filterNot { c -> c.states.empty() }.random() }
        val expected = transaction {
            country
                .states
                .sortedBy { c -> c.id.value.toString() }
                .map { s -> StateDto(s) }
        }

        transaction {
            val actual = service.listStates(country.id.value)

            assertContentEquals(expected, actual)
        }
    }

    @Test
    fun `test list states on sample data with options`() {
        val country = transaction { sampleData.countries.filterNot { c -> c.states.empty() }.random() }
        val expected = transaction {
            country
                .states
                .sortedWith(compareBy({ s -> s.name }, { s -> s.id.value.toString() }))
                .drop(1 * 2)
                .take(2)
                .map { s -> StateDto(s) }
        }

        transaction {
            val actual = service.listStates(
                id = country.id.value,
                sort = "name,asc",
                page = 1,
                size = 2,
            )

            assertContentEquals(expected, actual)
        }
    }

    @Test
    fun `test list cities on sample data`() {
        val country = transaction { sampleData.countries.filterNot { c -> c.cities.empty() }.random() }
        val expected = transaction {
            country
                .cities
                .sortedBy { c -> c.id.value.toString() }
                .map { c -> CityDto(c) }
        }

        transaction {
            val actual = service.listCities(country.id.value)

            assertContentEquals(expected, actual)
        }
    }

    @Test
    fun `test list cities on sample data with options`() {
        val country = transaction { sampleData.countries.filterNot { c -> c.cities.empty() }.random() }
        val expected = transaction {
            country
                .cities
                .sortedWith(compareBy({ c -> c.name }, { c -> c.id.value.toString() }))
                .drop(1 * 2)
                .take(2)
                .map { c -> CityDto(c) }
        }

        transaction {
            val actual = service.listCities(
                id = country.id.value,
                sort = "name,asc",
                page = 1,
                size = 2,
            )

            assertContentEquals(expected, actual)
        }
    }

    @Test
    fun `test create`() {
        val alpha2 = "C9"
        val alpha3 = "C09"
        val name = "Country Nine"
        val localizedName = "земля"

        val create = CountryInputDto(
            alpha2 = alpha2,
            alpha3 = alpha3,
            name = name,
            localizedName = localizedName,
        )

        val actual = service.create(
            country = create,
        )

        assertNotNull(actual)
        assertEquals(alpha2, actual.alpha2)
        assertEquals(alpha3, actual.alpha3)
        assertEquals(name, actual.name)
        assertEquals(localizedName, actual.localizedName)
    }

    @Test
    fun `test update all`() {
        val country = sampleData.countries.random()
        val alpha2 = "XX"
        val alpha3 = "XXX"
        val name = "Name"
        val localizedName = "LocalizedName"

        val update = CountryInputDto(
            alpha2 = alpha2,
            alpha3 = alpha3,
            name = name,
            localizedName = localizedName,
        )

        val actual = service.update(
            id = country.id.value,
            country = update,
        )

        assertNotNull(actual)
        assertEquals(alpha2, actual.alpha2)
        assertEquals(alpha3, actual.alpha3)
        assertEquals(name, actual.name)
        assertEquals(localizedName, actual.localizedName)
    }

    @Test
    fun `test delete`() {
        val id = sampleData.countries.random().id.value

        transaction { assertNotNull(Country.findById(id)) }

        service.delete(id)

        transaction { assertNull(Country.findById(id)) }
    }
}
