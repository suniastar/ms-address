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
import kotlin.test.assertEquals
import kotlin.test.assertNull

@SpringBootTest
@ActiveProfiles("sample")
class CityDtoTest(
    @Autowired private val sampleData: SampleDataConfig,
) {

    @BeforeTest
    fun `set up`() {
        sampleData.reset()
    }

    @Test
    fun `test get country on sample data`() {
        val city = transaction { sampleData.cities.random() }
        val expected = transaction { CountryDto(city.country) }
        val actual = CityDto(city).country

        transaction { assertEquals(expected, actual) }
    }

    @Test
    fun `test get state on sample data`() {
        val city = transaction { sampleData.cities.filterNot { c -> c.state == null }.random() }
        val expected = transaction { StateDto(city.state!!) }
        val actual = CityDto(city).state

        transaction { assertEquals(expected, actual) }
    }

    @Test
    fun `test get state on sample data with no state`() {
        val city = transaction { sampleData.cities.filter { c -> c.state == null }.random() }
        val actual = CityDto(city).state

        transaction { assertNull(actual) }
    }

    @Test
    fun `test list post codes on sample data`() {
        val city = transaction { sampleData.cities.filterNot { c -> c.postCodes.empty() }.random() }
        val expected = transaction {
            city.postCodes
                .sortedBy { p -> p.id.value.toString() }
                .map { p -> PostCodeDto(p) }
        }
        val actual = CityDto(city).postCodes

        transaction { assertContentEquals(expected, actual) }
    }
}
