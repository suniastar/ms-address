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

package de.fenste.ms.address.application.dtos

import de.fenste.ms.address.config.SampleDataConfig
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles("sample")
class AddressDtoTest(
    @Autowired private val sampleData: SampleDataConfig,
) {

    @BeforeTest
    fun `set up`() {
        sampleData.reset()
    }

    @Test
    fun `test get street on sample data`() {
        val address = transaction { sampleData.addresses.random() }
        val expected = transaction { StreetDto(address.street) }
        val actual = AddressDto(address).street

        transaction { assertEquals(expected, actual) }
    }
}
