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

package de.fenste.ms.address.infrastructure.repositories

import de.fenste.ms.address.test.SampleData
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import java.util.UUID
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull

@DataJpaTest
class AddressRepositoryTest(
    @Autowired private val testEntityManager: TestEntityManager,
    @Autowired private val addressRepository: AddressRepository,
) {
    @BeforeEach
    fun `set up`() {
        SampleData.reset(testEntityManager)
    }

    @Test
    fun `test findAll`() {
        val expected = SampleData.addresses
        val actual = addressRepository.findAll()
        assertContentEquals(expected, actual)
    }

    @Test
    fun `test findAll sorted`() {
        val expected = SampleData.addresses.sortedBy { it.id.toString() }
        val actual = addressRepository.findAll(Sort.by("id"))
        assertContentEquals(expected, actual)
    }

    @Test
    fun `test findByIdOrNull with sample data`() {
        val expected = SampleData.addresses.random()
        val actual = addressRepository.findByIdOrNull(expected.id)
        assertEquals(expected, actual)
    }

    @Test
    fun `test findByIdOrNull with not existing` () {
        val uuid = UUID.randomUUID()
        val actual = addressRepository.findByIdOrNull(uuid)
        assertFalse(SampleData.addresses.map { it.id }.contains(uuid))
        assertNull(actual)
    }
}
