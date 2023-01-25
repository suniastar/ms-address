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

package de.fenste.ms.address.application.services

import de.fenste.ms.address.application.dtos.CityDto
import de.fenste.ms.address.application.dtos.CityInputDto
import de.fenste.ms.address.infrastructure.repositories.CityRepository
import de.fenste.ms.address.infrastructure.tables.CityTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CityService(
    @Autowired private val cityRepository: CityRepository,
) {

    fun count(): Long = transaction {
        cityRepository.count()
    }

    @Suppress("UnusedPrivateMember") // TODO implement sort
    fun list(
        page: Int? = null,
        size: Int? = null,
        sort: String? = null,
    ): List<CityDto> = transaction {
        cityRepository
            .list(
                page = page,
                size = size,
                order = arrayOf(CityTable.id to SortOrder.ASC),
            )
            .map { c -> CityDto(c) }
    }

    fun find(
        id: UUID,
    ): CityDto? = transaction {
        cityRepository
            .find(id)
            ?.let { c -> CityDto(c) }
    }

    fun create(
        city: CityInputDto,
    ): CityDto = transaction {
        cityRepository
            .create(
                name = city.name,
                countryId = city.country,
                stateId = city.state,
            )
            .let { c -> CityDto(c) }
    }

    fun update(
        id: UUID,
        city: CityInputDto,
    ): CityDto = transaction {
        cityRepository
            .update(
                id = id,
                name = city.name,
                countryId = city.country,
                stateId = city.state,
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
