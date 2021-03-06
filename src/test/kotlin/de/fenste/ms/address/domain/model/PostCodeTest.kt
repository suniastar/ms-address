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

import de.fenste.ms.address.test.SampleData
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class PostCodeTest {
    private lateinit var copy: PostCode

    @BeforeTest
    fun `set up`() {
        SampleData.reset()

        copy = transaction {
            PostCode.findById(SampleData.postCodes[0].id)!!
        }
    }

    @Test
    fun `test equals`(): Unit = transaction {
        assertEquals(SampleData.postCodes[0], SampleData.postCodes[0])
        assertEquals(copy, copy)
        assertEquals(SampleData.postCodes[0], copy)
        assertEquals(copy, SampleData.postCodes[0])

        assertNotEquals(SampleData.postCodes[0], SampleData.postCodes[1])
        assertNotEquals(copy, SampleData.postCodes[1])
        assertNotEquals(SampleData.postCodes[1], SampleData.postCodes[0])
        assertNotEquals(SampleData.postCodes[1], copy)

        assertNotEquals<PostCode?>(copy, null)
        assertNotEquals<PostCode?>(null, SampleData.postCodes[0])
    }

    @Test
    fun `test hashCode`(): Unit = transaction {
        assertEquals(SampleData.postCodes[0].hashCode(), SampleData.postCodes[0].hashCode())
        assertEquals(copy.hashCode(), copy.hashCode())
        assertEquals(SampleData.postCodes[0].hashCode(), copy.hashCode())
        assertEquals(copy.hashCode(), SampleData.postCodes[0].hashCode())

        assertNotEquals(SampleData.postCodes[0].hashCode(), SampleData.postCodes[1].hashCode())
        assertNotEquals(copy.hashCode(), SampleData.postCodes[1].hashCode())
        assertNotEquals(SampleData.postCodes[1].hashCode(), SampleData.postCodes[0].hashCode())
        assertNotEquals(SampleData.postCodes[1].hashCode(), copy.hashCode())

        assertNotEquals(copy.hashCode(), null.hashCode())
        assertNotEquals(null.hashCode(), SampleData.postCodes[0].hashCode())
    }

    @Test
    fun `test toString`(): Unit = transaction {
        val cId = copy.id
        val pId = copy.city.id
        val cExpected = "PostCode(id='$cId', city='$pId', code='10557')"
        val cActual = SampleData.postCodes[0].toString()
        assertEquals(cExpected, cActual)
    }
}
