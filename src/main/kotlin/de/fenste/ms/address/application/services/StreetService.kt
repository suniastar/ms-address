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

import de.fenste.ms.address.application.dtos.responses.StreetDto
import de.fenste.ms.address.infrastructure.repository.StreetRepository
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class StreetService(
    @Autowired private val streetRepository: StreetRepository,
) {
    fun streets(
        limit: Int? = null,
        offset: Long? = null,
    ): List<StreetDto>? = transaction {
        streetRepository
            .list(
                limit = limit,
                offset = offset ?: 0L,
            )
            .map { s -> StreetDto(s) }
            .ifEmpty { null }
    }

    fun street(
        id: UUID,
    ): StreetDto? = transaction {
        streetRepository
            .find(id)
            ?.let { s -> StreetDto(s) }
    }
}
