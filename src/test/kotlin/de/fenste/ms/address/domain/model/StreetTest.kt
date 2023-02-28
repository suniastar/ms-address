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
class StreetTest(
    @Autowired private val sampleData: SampleDataConfig,
) {
    private lateinit var copy: Street

    @BeforeTest
    fun `set up`() {
        sampleData.reset()

        copy = transaction {
            Street.findById(sampleData.streets[0].id)!!
        }
    }

    @Test
    fun `test equals`(): Unit = transaction {
        assertEquals(sampleData.streets[0], sampleData.streets[0])
        assertEquals(copy, copy)
        assertEquals(sampleData.streets[0], copy)
        assertEquals(copy, sampleData.streets[0])

        assertNotEquals(sampleData.streets[0], sampleData.streets[1])
        assertNotEquals(copy, sampleData.streets[1])
        assertNotEquals(sampleData.streets[1], sampleData.streets[0])
        assertNotEquals(sampleData.streets[1], copy)

        assertNotEquals<Street?>(copy, null)
        assertNotEquals<Street?>(null, sampleData.streets[0])
    }

    @Test
    fun `test hashCode`(): Unit = transaction {
        assertEquals(sampleData.streets[0].hashCode(), sampleData.streets[0].hashCode())
        assertEquals(copy.hashCode(), copy.hashCode())
        assertEquals(sampleData.streets[0].hashCode(), copy.hashCode())
        assertEquals(copy.hashCode(), sampleData.streets[0].hashCode())

        assertNotEquals(sampleData.streets[0].hashCode(), sampleData.streets[1].hashCode())
        assertNotEquals(copy.hashCode(), sampleData.streets[1].hashCode())
        assertNotEquals(sampleData.streets[1].hashCode(), sampleData.streets[0].hashCode())
        assertNotEquals(sampleData.streets[1].hashCode(), copy.hashCode())

        assertNotEquals(copy.hashCode(), null.hashCode())
        assertNotEquals(null.hashCode(), sampleData.streets[0].hashCode())
    }

    @Test
    fun `test toString`(): Unit = transaction {
        val cId = copy.id
        val pId = copy.postCode.id
        val cExpected = "Street(id=$cId, postCode=$pId, name=Street One)"
        val cActual = sampleData.streets[0].toString()
        assertEquals(cExpected, cActual)
    }
}
