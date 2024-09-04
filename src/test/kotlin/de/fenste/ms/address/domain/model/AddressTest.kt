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
class AddressTest(
    @Autowired private val sampleData: SampleDataConfig,
) {
    private lateinit var copy: Address

    @BeforeTest
    fun `set up`() {
        sampleData.reset()

        copy = transaction {
            Address.findById(sampleData.addresses[0].id)!!
        }
    }

    @Test
    fun `test equals`(): Unit = transaction {
        assertEquals(sampleData.addresses[0], sampleData.addresses[0])
        assertEquals(copy, copy)
        assertEquals(sampleData.addresses[0], copy)
        assertEquals(copy, sampleData.addresses[0])

        assertNotEquals(sampleData.addresses[0], sampleData.addresses[1])
        assertNotEquals(copy, sampleData.addresses[1])
        assertNotEquals(sampleData.addresses[1], sampleData.addresses[0])
        assertNotEquals(sampleData.addresses[1], copy)

        assertNotEquals<Address?>(copy, null)
        assertNotEquals<Address?>(null, sampleData.addresses[0])
    }

    @Test
    fun `test hashCode`(): Unit = transaction {
        assertEquals(sampleData.addresses[0].hashCode(), sampleData.addresses[0].hashCode())
        assertEquals(copy.hashCode(), copy.hashCode())
        assertEquals(sampleData.addresses[0].hashCode(), copy.hashCode())
        assertEquals(copy.hashCode(), sampleData.addresses[0].hashCode())

        assertNotEquals(sampleData.addresses[0].hashCode(), sampleData.addresses[1].hashCode())
        assertNotEquals(copy.hashCode(), sampleData.addresses[1].hashCode())
        assertNotEquals(sampleData.addresses[1].hashCode(), sampleData.addresses[0].hashCode())
        assertNotEquals(sampleData.addresses[1].hashCode(), copy.hashCode())

        assertNotEquals(copy.hashCode(), null.hashCode())
        assertNotEquals(null.hashCode(), sampleData.addresses[0].hashCode())
    }

    @Test
    fun `test toString`(): Unit = transaction {
        val cId = copy.id
        val pId = copy.street.id
        val cExpected = "Address(id=$cId, street=$pId, houseNumber=1, extra=null)"
        val cActual = sampleData.addresses[0].toString()
        assertEquals(cExpected, cActual)
    }
}
