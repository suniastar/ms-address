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
class PostCodeTest(
    @Autowired private val sampleData: SampleDataConfig,
) {
    private lateinit var copy: PostCode

    @BeforeTest
    fun `set up`() {
        sampleData.reset()

        copy = transaction {
            PostCode.findById(sampleData.postCodes[0].id)!!
        }
    }

    @Test
    fun `test equals`(): Unit = transaction {
        assertEquals(sampleData.postCodes[0], sampleData.postCodes[0])
        assertEquals(copy, copy)
        assertEquals(sampleData.postCodes[0], copy)
        assertEquals(copy, sampleData.postCodes[0])

        assertNotEquals(sampleData.postCodes[0], sampleData.postCodes[1])
        assertNotEquals(copy, sampleData.postCodes[1])
        assertNotEquals(sampleData.postCodes[1], sampleData.postCodes[0])
        assertNotEquals(sampleData.postCodes[1], copy)

        assertNotEquals<PostCode?>(copy, null)
        assertNotEquals<PostCode?>(null, sampleData.postCodes[0])
    }

    @Test
    fun `test hashCode`(): Unit = transaction {
        assertEquals(sampleData.postCodes[0].hashCode(), sampleData.postCodes[0].hashCode())
        assertEquals(copy.hashCode(), copy.hashCode())
        assertEquals(sampleData.postCodes[0].hashCode(), copy.hashCode())
        assertEquals(copy.hashCode(), sampleData.postCodes[0].hashCode())

        assertNotEquals(sampleData.postCodes[0].hashCode(), sampleData.postCodes[1].hashCode())
        assertNotEquals(copy.hashCode(), sampleData.postCodes[1].hashCode())
        assertNotEquals(sampleData.postCodes[1].hashCode(), sampleData.postCodes[0].hashCode())
        assertNotEquals(sampleData.postCodes[1].hashCode(), copy.hashCode())

        assertNotEquals(copy.hashCode(), null.hashCode())
        assertNotEquals(null.hashCode(), sampleData.postCodes[0].hashCode())
    }

    @Test
    fun `test toString`(): Unit = transaction {
        val cId = copy.id
        val pId = copy.city.id
        val cExpected = "PostCode(id='$cId', city='$pId', code='10557')"
        val cActual = sampleData.postCodes[0].toString()
        assertEquals(cExpected, cActual)
    }
}
