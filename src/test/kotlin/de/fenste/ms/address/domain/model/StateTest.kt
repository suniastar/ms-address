/*
 * Copyright (c) 2022 Frederik Enste <frederik@fenste.de>.
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

import de.fenste.ms.address.infrastructure.tables.StateTable
import de.fenste.ms.address.test.SampleData
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class StateTest {
    private lateinit var copy: State

    @BeforeTest
    fun `set up`() {
        SampleData.reset()

        copy = transaction {
            State
                .find { StateTable.id eq SampleData.states[0].id }
                .limit(1)
                .notForUpdate()
                .first()
        }
    }

    @Test
    fun `test equals`(): Unit = transaction {
        assertEquals(SampleData.states[0], SampleData.states[0])
        assertEquals(copy, copy)
        assertEquals(SampleData.states[0], copy)
        assertEquals(copy, SampleData.states[0])

        assertNotEquals(SampleData.states[0], SampleData.states[1])
        assertNotEquals(copy, SampleData.states[1])
        assertNotEquals(SampleData.states[1], SampleData.states[0])
        assertNotEquals(SampleData.states[1], copy)

        assertNotEquals<State?>(copy, null)
        assertNotEquals<State?>(null, SampleData.states[0])
    }

    @Test
    fun `test hashCode`(): Unit = transaction {
        assertEquals(SampleData.states[0].hashCode(), SampleData.states[0].hashCode())
        assertEquals(copy.hashCode(), copy.hashCode())
        assertEquals(SampleData.states[0].hashCode(), copy.hashCode())
        assertEquals(copy.hashCode(), SampleData.states[0].hashCode())

        assertNotEquals(SampleData.states[0].hashCode(), SampleData.states[1].hashCode())
        assertNotEquals(copy.hashCode(), SampleData.states[1].hashCode())
        assertNotEquals(SampleData.states[1].hashCode(), SampleData.states[0].hashCode())
        assertNotEquals(SampleData.states[1].hashCode(), copy.hashCode())

        assertNotEquals(copy.hashCode(), null.hashCode())
        assertNotEquals(null.hashCode(), SampleData.states[0].hashCode())
    }

    @Test
    fun `test toString`(): Unit = transaction {
        val cId = copy.id
        val pId = copy.country.id
        val cExpected = "State(id='$cId', country='$pId', name='Berlin')"
        val cActual = SampleData.states[0].toString()
        assertEquals(cExpected, cActual)
    }
}
