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

import de.fenste.ms.address.application.dtos.requests.CreateCountryDto
import de.fenste.ms.address.application.dtos.requests.UpdateCountryDto
import de.fenste.ms.address.application.dtos.responses.CountryDto
import de.fenste.ms.address.domain.model.Country
import de.fenste.ms.address.test.SampleData
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
class CountryControllerTest(
    @Autowired private val controller: CountryController,
) {

    @BeforeTest
    fun `set up`() {
        SampleData.reset()
    }

    @Test
    fun `list on sample data`() {
        val expected = SampleData.countries.sortedBy { c -> c.id.value.toString() }.map { c -> CountryDto(c) }
        val actual = controller.countries()

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test list on sample data with options`() {
        val expected = SampleData.countries
            .sortedBy { c -> c.id.value.toString() }
            .drop(2)
            .take(1)
            .map { c -> CountryDto(c) }
        val actual = controller.countries(
            offset = 2,
            limit = 1,
        )

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test find by id on sample data`() {
        val expected = SampleData.countries.random().let { c -> CountryDto(c) }
        val actual = controller.country(id = expected.id)

        assertEquals(expected, actual)
    }

    @Test
    fun `test find by alpha2 on non existing sample data`() {
        val actual = controller.country(alpha2 = "XX")

        assertNull(actual)
    }

    @Test
    fun `test create`() {
        val create = CreateCountryDto(
            alpha2 = "CZ",
            alpha3 = "CZE",
            name = "Czechia",
            localizedName = "Tschechien",
        )

        val actual = controller.createCountry(
            country = create,
        )

        assertNotNull(actual.id)
        assertEquals(create.alpha2, actual.alpha2)
        assertEquals(create.alpha3, actual.alpha3)
        assertEquals(create.name, actual.name)
        assertEquals(create.localizedName, actual.localizedName)
    }

    @Test
    fun `test update all`() {
        val sampleId = SampleData.countries.random().id.value.toString()
        val update = UpdateCountryDto(
            id = sampleId,
            alpha2 = "XX",
            alpha3 = "XXX",
            name = "Name",
            localizedName = "LocalizedName",
        )

        val actual = controller.updateCountry(
            country = update,
        )

        assertNotNull(actual)
        assertEquals(update.alpha2, actual.alpha2)
        assertEquals(update.alpha3, actual.alpha3)
        assertEquals(update.name, actual.name)
        assertEquals(update.localizedName, actual.localizedName)
    }

    @Test
    fun `test update nothing`() {
        val expected = SampleData.countries.random().let { c -> CountryDto(c) }
        val update = UpdateCountryDto(
            id = expected.id,
        )

        val actual = controller.updateCountry(
            country = update,
        )

        assertEquals(expected, actual)
    }

    @Test
    @Ignore // TODO allow cascade deletion?
    fun `test delete`() {
        val sampleId = SampleData.countries.random().id.value.toString()

        controller.deleteCountry(sampleId)

        assertNull(Country.findById(UUID.fromString(sampleId)))
    }
}
