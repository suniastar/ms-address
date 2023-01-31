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

package de.fenste.ms.address.infrastructure.tables

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@SpringBootTest
@ActiveProfiles("sample")
class CountryTableTest {

    @Test
    fun `test valueOf`() {
        assertEquals(CountryTable.id, CountryTable.valueOf("id"))
        assertEquals(CountryTable.alpha2, CountryTable.valueOf("alpha2"))
        assertEquals(CountryTable.alpha3, CountryTable.valueOf("alpha3"))
        assertEquals(CountryTable.name, CountryTable.valueOf("name"))
        assertEquals(CountryTable.localizedName, CountryTable.valueOf("localizedName"))
        assertEquals(CountryTable.localizedName, CountryTable.valueOf("localized_name"))

        assertEquals(CountryTable.name, CountryTable.valueOf("NAME"))
        assertEquals(CountryTable.name, CountryTable.valueOf("NaMe"))
        assertEquals(CountryTable.name, CountryTable.valueOf("nAME"))

        assertFailsWith<IllegalArgumentException> { CountryTable.valueOf("does not exist") }
        assertFailsWith<IllegalArgumentException> { CountryTable.valueOf("fail pls") }
        assertFailsWith<IllegalArgumentException> { CountryTable.valueOf("error") }
    }
}
