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

package de.fenste.ms.address.domain.model

import de.fenste.ms.address.config.SampleDataConfig
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@SpringBootTest
@ActiveProfiles("sample")
class CityTest(
    @Autowired private val sampleData: SampleDataConfig,
) {
    private lateinit var copy: City

    @BeforeTest
    fun `set up`() {
        sampleData.reset()

        copy = transaction {
            City.findById(sampleData.cities[0].id)!!
        }
    }

    @Test
    fun `test equals`(): Unit = transaction {
        assertEquals(sampleData.cities[0], sampleData.cities[0])
        assertEquals(copy, copy)
        assertEquals(sampleData.cities[0], copy)
        assertEquals(copy, sampleData.cities[0])

        assertNotEquals(sampleData.cities[0], sampleData.cities[1])
        assertNotEquals(copy, sampleData.cities[1])
        assertNotEquals(sampleData.cities[1], sampleData.cities[0])
        assertNotEquals(sampleData.cities[1], copy)

        assertNotEquals<City?>(copy, null)
        assertNotEquals<City?>(null, sampleData.cities[0])
    }

    @Test
    fun `test hashCode`(): Unit = transaction {
        assertEquals(sampleData.cities[0].hashCode(), sampleData.cities[0].hashCode())
        assertEquals(copy.hashCode(), copy.hashCode())
        assertEquals(sampleData.cities[0].hashCode(), copy.hashCode())
        assertEquals(copy.hashCode(), sampleData.cities[0].hashCode())

        assertNotEquals(sampleData.cities[0].hashCode(), sampleData.cities[1].hashCode())
        assertNotEquals(copy.hashCode(), sampleData.cities[1].hashCode())
        assertNotEquals(sampleData.cities[1].hashCode(), sampleData.cities[0].hashCode())
        assertNotEquals(sampleData.cities[1].hashCode(), copy.hashCode())

        assertNotEquals(copy.hashCode(), null.hashCode())
        assertNotEquals(null.hashCode(), sampleData.cities[0].hashCode())
    }

    @Test
    fun `test toString`(): Unit = transaction {
        val cId = copy.id
        val pIdC = copy.country.id
        val pIdS = copy.state?.id
        val cExpected = "City(id='$cId', country='$pIdC', state='$pIdS', name='City One')"
        val cActual = sampleData.cities[0].toString()
        assertEquals(cExpected, cActual)
    }
}
