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

import de.fenste.ms.address.infrastructure.tables.CountryTable
import de.fenste.ms.address.test.SampleData
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CountryTest {
    private lateinit var copy: Country

    @BeforeTest
    fun `set up`() {
        SampleData.reset()

        copy = transaction {
            Country
                .find { CountryTable.id eq SampleData.countries[0].id }
                .limit(1)
                .notForUpdate()
                .first()
        }
    }

    @Test
    fun `test equals`(): Unit = transaction {
        assertEquals(SampleData.countries[0], SampleData.countries[0])
        assertEquals(copy, copy)
        assertEquals(SampleData.countries[0], copy)
        assertEquals(copy, SampleData.countries[0])

        assertNotEquals(SampleData.countries[0], SampleData.countries[1])
        assertNotEquals(copy, SampleData.countries[1])
        assertNotEquals(SampleData.countries[1], SampleData.countries[0])
        assertNotEquals(SampleData.countries[1], copy)

        assertNotEquals<Country?>(copy, null)
        assertNotEquals<Country?>(null, SampleData.countries[0])
    }

    @Test
    fun `test hashCode`(): Unit = transaction {
        assertEquals(SampleData.countries[0].hashCode(), SampleData.countries[0].hashCode())
        assertEquals(copy.hashCode(), copy.hashCode())
        assertEquals(SampleData.countries[0].hashCode(), copy.hashCode())
        assertEquals(copy.hashCode(), SampleData.countries[0].hashCode())

        assertNotEquals(SampleData.countries[0].hashCode(), SampleData.countries[1].hashCode())
        assertNotEquals(copy.hashCode(), SampleData.countries[1].hashCode())
        assertNotEquals(SampleData.countries[1].hashCode(), SampleData.countries[0].hashCode())
        assertNotEquals(SampleData.countries[1].hashCode(), copy.hashCode())

        assertNotEquals(copy.hashCode(), null.hashCode())
        assertNotEquals(null.hashCode(), SampleData.countries[0].hashCode())
    }

    @Test
    fun `test toString`(): Unit = transaction {
        val cId = copy.id
        val cExpected =
            "Country(id='$cId', alpha2='DE', alpha3='DEU', name='Germany', localizedName='Deutschland')"
        val cActual = SampleData.countries[0].toString()
        assertEquals(cExpected, cActual)
    }
}
