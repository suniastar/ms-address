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

import de.fenste.ms.address.application.dtos.requests.CreateCityDto
import de.fenste.ms.address.application.dtos.requests.UpdateCityDto
import de.fenste.ms.address.application.dtos.responses.CityDto
import de.fenste.ms.address.infrastructure.repositories.CityRepository
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CityService(
    @Autowired private val cityRepository: CityRepository,
) {
    fun list(
        limit: Int? = null,
        offset: Long? = null,
    ): List<CityDto>? = transaction {
        cityRepository
            .list(
                limit = limit,
                offset = offset ?: 0L,
            )
            .map { c -> CityDto(c) }
            .ifEmpty { null }
    }

    fun find(
        id: UUID,
    ): CityDto? = transaction {
        cityRepository
            .find(id)
            ?.let { c -> CityDto(c) }
    }

    fun create(
        create: CreateCityDto,
    ): CityDto = transaction {
        cityRepository
            .create(
                name = create.name,
                countryId = UUID.fromString(create.country),
                stateId = create.state?.let { s -> UUID.fromString(s) },
            )
            .let { c -> CityDto(c) }
    }

    fun update(
        update: UpdateCityDto,
    ): CityDto = transaction {
        cityRepository.update(
            id = UUID.fromString(update.id),
            name = update.name,
            countryId = update.country?.let { c -> UUID.fromString(c) },
            stateId = update.state?.let { s -> UUID.fromString(s) },
        )
            .let { c -> CityDto(c) }
    }

    fun delete(
        id: UUID,
    ): Boolean = transaction {
        cityRepository
            .delete(
                id = id,
            )
        true
    }
}
