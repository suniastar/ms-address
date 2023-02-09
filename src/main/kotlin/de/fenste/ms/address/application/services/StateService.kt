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
import de.fenste.ms.address.application.dtos.CountryDto
import de.fenste.ms.address.application.dtos.StateDto
import de.fenste.ms.address.application.dtos.StateInputDto
import de.fenste.ms.address.application.util.parseSortOrder
import de.fenste.ms.address.domain.exception.NotFoundException
import de.fenste.ms.address.infrastructure.repositories.StateRepository
import de.fenste.ms.address.infrastructure.tables.CityTable
import de.fenste.ms.address.infrastructure.tables.StateTable
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class StateService(
    @Autowired private val stateRepository: StateRepository,
) {

    fun count(): Int = transaction {
        stateRepository.count()
    }

    fun find(
        id: UUID,
    ): StateDto? = transaction {
        stateRepository
            .find(id)
            ?.let { s -> StateDto(s) }
    }

    fun list(
        page: Int? = null,
        size: Int? = null,
        sort: String? = null,
    ): List<StateDto> = transaction {
        stateRepository
            .list(
                page = page,
                size = size,
                order = sort.parseSortOrder(StateTable::valueOf),
            )
            .map { s -> StateDto(s) }
    }

    fun getCountry(
        id: UUID,
    ): CountryDto = transaction {
        val state = stateRepository
            .find(
                id = id,
            )
            ?: throw NotFoundException("The state ($id) does not exist.")

        state
            .country
            .let { c -> CountryDto(c) }
    }

    fun listCities(
        id: UUID,
        page: Int? = null,
        size: Int? = null,
        sort: String? = null,
    ): List<CityDto> = transaction {
        val state = stateRepository
            .find(
                id = id,
            )
            ?: throw NotFoundException("The state ($id) does not exist.")

        stateRepository
            .listCities(
                state = state,
                page = page,
                size = size,
                order = sort.parseSortOrder(CityTable::valueOf),
            )
            .map { c -> CityDto(c) }
    }

    fun create(
        state: StateInputDto,
    ): StateDto = transaction {
        stateRepository
            .create(
                name = state.name,
                countryId = state.country,
            )
            .let { s -> StateDto(s) }
    }

    fun update(
        id: UUID,
        state: StateInputDto,
    ): StateDto = transaction {
        stateRepository
            .update(
                id = id,
                name = state.name,
                countryId = state.country,
            )
            .let { s -> StateDto(s) }
    }

    fun delete(
        id: UUID,
    ): Boolean = transaction {
        stateRepository
            .delete(
                id = id,
            )
        true
    }
}
