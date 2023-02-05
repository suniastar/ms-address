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
class AddressTableTest {

    @Test
    fun `test valueOf`() {
        assertEquals(AddressTable.id, AddressTable.valueOf("id"))
        assertEquals(AddressTable.streetId, AddressTable.valueOf("streetId"))
        assertEquals(AddressTable.streetId, AddressTable.valueOf("street_id"))
        assertEquals(AddressTable.houseNumber, AddressTable.valueOf("houseNumber"))
        assertEquals(AddressTable.houseNumber, AddressTable.valueOf("house_number"))
        assertEquals(AddressTable.extra, AddressTable.valueOf("extra"))

        assertEquals(AddressTable.extra, AddressTable.valueOf("EXTRA"))
        assertEquals(AddressTable.extra, AddressTable.valueOf("ExTrA"))
        assertEquals(AddressTable.extra, AddressTable.valueOf("exTRA"))

        assertFailsWith<IllegalArgumentException> { AddressTable.valueOf("does not exist") }
        assertFailsWith<IllegalArgumentException> { AddressTable.valueOf("fail pls") }
        assertFailsWith<IllegalArgumentException> { AddressTable.valueOf("error") }
    }
}
