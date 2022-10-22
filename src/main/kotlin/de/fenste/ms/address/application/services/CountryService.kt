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

package de.fenste.ms.address.application.services

import de.fenste.ms.address.application.dtos.CountryDto
import de.fenste.ms.address.application.dtos.CountryInputDto
import de.fenste.ms.address.infrastructure.repositories.CountryRepository
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CountryService(
    @Autowired private val countryRepository: CountryRepository,
) {

    fun list(
        limit: Int? = null,
        offset: Long? = null,
    ): List<CountryDto>? = transaction {
        countryRepository
            .list(
                limit = limit,
                offset = offset ?: 0L,
            )
            .map { c -> CountryDto(c) }
            .ifEmpty { null }
    }

    fun find(
        id: UUID? = null,
        alpha2: String? = null,
        alpha3: String? = null,
    ): CountryDto? = transaction {
        countryRepository
            .find(
                id = id,
                alpha2 = alpha2,
                alpha3 = alpha3,
            )
            ?.let { c -> CountryDto(c) }
    }

    fun create(
        country: CountryInputDto,
    ): CountryDto = transaction {
        countryRepository
            .create(
                alpha2 = country.alpha2,
                alpha3 = country.alpha3,
                name = country.name,
                localizedName = country.localizedName,
            )
            .let { c -> CountryDto(c) }
    }

    fun update(
        id: UUID,
        country: CountryInputDto,
    ): CountryDto = transaction {
        countryRepository
            .update(
                id = id,
                alpha2 = country.alpha2,
                alpha3 = country.alpha3,
                name = country.name,
                localizedName = country.localizedName,
            )
            .let { c -> CountryDto(c) }
    }

    fun delete(
        id: UUID,
    ): Boolean = transaction {
        countryRepository
            .delete(
                id = id,
            )
        true
    }
}
