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

import de.fenste.ms.address.application.dtos.CountryDto
import de.fenste.ms.address.application.dtos.CountryInputDto
import de.fenste.ms.address.domain.model.Country
import de.fenste.ms.address.test.SampleData
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
class CountryServiceTest(
    @Autowired private val service: CountryService,
) {

    @BeforeTest
    fun `set up`() {
        SampleData.reset()
    }

    @Test
    fun `list on sample data`() {
        val expected = SampleData.countries.sortedBy { c -> c.id.value.toString() }.map { c -> CountryDto(c) }
        val actual = service.list()

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test list on sample data with options`() {
        val expected = SampleData.countries
            .sortedBy { c -> c.id.value.toString() }
            .drop(2)
            .take(1)
            .map { c -> CountryDto(c) }
        val actual = service.list(
            offset = 2,
            limit = 1,
        )

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test list on no data`() {
        SampleData.clear()
        val list = service.list()

        assertNull(list)
    }

    @Test
    fun `test find by id on sample data`() {
        val expected = SampleData.countries.random().let { c -> CountryDto(c) }
        val actual = service.find(id = expected.id)

        assertEquals(expected, actual)
    }

    @Test
    fun `test find by alpha2 on non existing sample data`() {
        val actual = service.find(alpha2 = "XX")

        assertNull(actual)
    }

    @Test
    fun `test create`() {
        val alpha2 = "CZ"
        val alpha3 = "CZE"
        val name = "Czechia"
        val localizedName = "Tschechien"

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
        val country = SampleData.countries.random()
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
        val id = SampleData.countries.random().id.value

        transaction { assertNotNull(Country.findById(id)) }

        service.delete(id)

        transaction { assertNull(Country.findById(id)) }
    }
}
