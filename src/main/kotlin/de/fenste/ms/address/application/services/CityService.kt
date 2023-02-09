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
import de.fenste.ms.address.application.dtos.CountryDto
import de.fenste.ms.address.application.dtos.PostCodeDto
import de.fenste.ms.address.application.dtos.StateDto
import de.fenste.ms.address.application.util.parseSortOrder
import de.fenste.ms.address.domain.exception.NotFoundException
import de.fenste.ms.address.infrastructure.repositories.CityRepository
import de.fenste.ms.address.infrastructure.tables.CityTable
import de.fenste.ms.address.infrastructure.tables.PostCodeTable
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class CityService(
    @Autowired private val cityRepository: CityRepository,
) {

    fun count(): Int = transaction {
        cityRepository.count()
    }

    fun find(
        id: UUID,
    ): CityDto? = transaction {
        cityRepository
            .find(id)
            ?.let { c -> CityDto(c) }
    }

    fun list(
        page: Int? = null,
        size: Int? = null,
        sort: String? = null,
    ): List<CityDto> = transaction {
        cityRepository
            .list(
                page = page,
                size = size,
                order = sort.parseSortOrder(CityTable::valueOf),
            )
            .map { c -> CityDto(c) }
    }

    fun getCountry(
        id: UUID,
    ): CountryDto = transaction {
        val city = cityRepository
            .find(
                id = id,
            )
            ?: throw NotFoundException("The city ($id) does not exist.")

        city
            .country
            .let { c -> CountryDto(c) }
    }

    fun getState(
        id: UUID,
    ): StateDto? = transaction {
        val city = cityRepository
            .find(
                id = id,
            )
            ?: throw NotFoundException("The city ($id) does not exist.")

        city
            .state
            ?.let { s -> StateDto(s) }
    }

    fun listPostCodes(
        id: UUID,
        page: Int? = null,
        size: Int? = null,
        sort: String? = null,
    ): List<PostCodeDto> = transaction {
        val city = cityRepository
            .find(
                id = id,
            )
            ?: throw NotFoundException("The city ($id) does not exist.")

        cityRepository
            .listPostCodes(
                city = city,
                page = page,
                size = size,
                order = sort.parseSortOrder(PostCodeTable::valueOf),
            )
            .map { p -> PostCodeDto(p) }
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
