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

import de.fenste.ms.address.application.dtos.StateDto
import de.fenste.ms.address.application.dtos.StateInputDto
import de.fenste.ms.address.infrastructure.repositories.StateRepository
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class StateService(
    @Autowired private val stateRepository: StateRepository,
) {
    fun list(
        limit: Int? = null,
        offset: Long? = null,
    ): List<StateDto>? = transaction {
        stateRepository
            .list(
                limit = limit,
                offset = offset ?: 0L,
            )
            .map { s -> StateDto(s) }
            .ifEmpty { null }
    }

    fun find(
        id: UUID,
    ): StateDto? = transaction {
        stateRepository
            .find(id)
            ?.let { s -> StateDto(s) }
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
