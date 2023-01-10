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

package de.fenste.ms.address.config

import de.fenste.ms.address.infrastructure.repositories.AddressRepository
import de.fenste.ms.address.infrastructure.repositories.CityRepository
import de.fenste.ms.address.infrastructure.repositories.CountryRepository
import de.fenste.ms.address.infrastructure.repositories.PostCodeRepository
import de.fenste.ms.address.infrastructure.repositories.StateRepository
import de.fenste.ms.address.infrastructure.repositories.StreetRepository
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

@ActiveProfiles("sample")
@SpringBootTest
class SampleDataConfigTest(
    @Autowired private val countryRepository: CountryRepository,
    @Autowired private val stateRepository: StateRepository,
    @Autowired private val cityRepository: CityRepository,
    @Autowired private val postCodeRepository: PostCodeRepository,
    @Autowired private val streetRepository: StreetRepository,
    @Autowired private val addressRepository: AddressRepository,
) {
    private companion object {
        inline fun <T> SizedIterable<T>?.test(message: () -> String) {
            assertNotNull(this)
            assertFalse(empty(), message.invoke())
            this.forEach { println(it) }
        }
    }

    @Test
    fun `test repository list`(): Unit = transaction {
        countryRepository.list().test { "CountryRepository is empty." }
        stateRepository.list().test { "StateRepository is empty." }
        cityRepository.list().test { "CityRepository is empty." }
        postCodeRepository.list().test { "PostCodeRepository is empty." }
        streetRepository.list().test { "StreetRepository is empty." }
        addressRepository.list().test { "AddressRepository is empty." }
    }
}
