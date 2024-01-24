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
import de.fenste.ms.address.infrastructure.tables.StateTable
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
class StateTest(
    @Autowired private val sampleData: SampleDataConfig,
) {
    private lateinit var copy: State

    @BeforeTest
    fun `set up`() {
        sampleData.reset()

        copy = transaction {
            State
                .find { StateTable.id eq sampleData.states[0].id }
                .limit(1)
                .notForUpdate()
                .first()
        }
    }

    @Test
    fun `test equals`(): Unit = transaction {
        assertEquals(sampleData.states[0], sampleData.states[0])
        assertEquals(copy, copy)
        assertEquals(sampleData.states[0], copy)
        assertEquals(copy, sampleData.states[0])

        assertNotEquals(sampleData.states[0], sampleData.states[1])
        assertNotEquals(copy, sampleData.states[1])
        assertNotEquals(sampleData.states[1], sampleData.states[0])
        assertNotEquals(sampleData.states[1], copy)

        assertNotEquals<State?>(copy, null)
        assertNotEquals<State?>(null, sampleData.states[0])
    }

    @Test
    fun `test hashCode`(): Unit = transaction {
        assertEquals(sampleData.states[0].hashCode(), sampleData.states[0].hashCode())
        assertEquals(copy.hashCode(), copy.hashCode())
        assertEquals(sampleData.states[0].hashCode(), copy.hashCode())
        assertEquals(copy.hashCode(), sampleData.states[0].hashCode())

        assertNotEquals(sampleData.states[0].hashCode(), sampleData.states[1].hashCode())
        assertNotEquals(copy.hashCode(), sampleData.states[1].hashCode())
        assertNotEquals(sampleData.states[1].hashCode(), sampleData.states[0].hashCode())
        assertNotEquals(sampleData.states[1].hashCode(), copy.hashCode())

        assertNotEquals(copy.hashCode(), null.hashCode())
        assertNotEquals(null.hashCode(), sampleData.states[0].hashCode())
    }

    @Test
    fun `test toString`(): Unit = transaction {
        val cId = copy.id
        val pId = copy.country.id
        val isPrinted = copy.isPrintedOnLabel
        val cExpected = "State(id=$cId, country=$pId, name=State One, isPrintedOnLabel=$isPrinted)"
        val cActual = sampleData.states[0].toString()
        assertEquals(cExpected, cActual)
    }
}
