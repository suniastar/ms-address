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
class StateTableTest {

    @Test
    fun `test valueOf`() {
        assertEquals(StateTable.id, StateTable.valueOf("id"))
        assertEquals(StateTable.countryId, StateTable.valueOf("countryId"))
        assertEquals(StateTable.countryId, StateTable.valueOf("country_id"))
        assertEquals(StateTable.name, StateTable.valueOf("name"))

        assertEquals(StateTable.name, StateTable.valueOf("NAME"))
        assertEquals(StateTable.name, StateTable.valueOf("NaMe"))
        assertEquals(StateTable.name, StateTable.valueOf("nAME"))

        assertFailsWith<IllegalArgumentException> { StateTable.valueOf("does not exist") }
        assertFailsWith<IllegalArgumentException> { StateTable.valueOf("fail pls") }
        assertFailsWith<IllegalArgumentException> { StateTable.valueOf("error") }
    }
}
