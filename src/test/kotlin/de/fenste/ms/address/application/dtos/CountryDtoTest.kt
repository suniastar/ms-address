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

package de.fenste.ms.address.application.dtos

import de.fenste.ms.address.config.SampleDataConfig
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertContentEquals

@SpringBootTest
@ActiveProfiles("sample")
class CountryDtoTest(
    @Autowired private val sampleData: SampleDataConfig,
) {

    @BeforeTest
    fun `set up`() {
        sampleData.reset()
    }

    @Test
    fun `test list states on sample data`() {
        val country = transaction { sampleData.countries.filterNot { c -> c.states.empty() }.random() }
        val expected = transaction {
            country.states
                .sortedBy { s -> s.id.value.toString() }
                .map { s -> StateDto(s) }
        }
        val actual = CountryDto(country).states

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `test list states on sample data with empty states`() {
        val country = transaction { sampleData.countries.filter { c -> c.states.empty() }.random() }
        val expected = emptyList<StateDto>()
        val actual = CountryDto(country).states

        transaction { assertContentEquals(expected, actual) }
    }

    @Test
    fun `test list cities on sample data`() {
        val country = transaction { sampleData.countries.filterNot { c -> c.cities.empty() }.random() }
        val expected = transaction {
            country.cities
                .sortedBy { s -> s.id.value.toString() }
                .map { s -> CityDto(s) }
        }
        val actual = CountryDto(country).cities

        transaction { assertContentEquals(expected, actual) }
    }
}
