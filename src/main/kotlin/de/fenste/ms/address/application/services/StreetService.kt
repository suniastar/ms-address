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

import de.fenste.ms.address.application.dtos.StreetDto
import de.fenste.ms.address.application.dtos.StreetInputDto
import de.fenste.ms.address.infrastructure.repositories.StreetRepository
import de.fenste.ms.address.infrastructure.tables.StreetTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class StreetService(
    @Autowired private val streetRepository: StreetRepository,
) {

    fun count(): Long = transaction {
        streetRepository.count()
    }

    fun list(
        page: Int? = null,
        size: Int? = null,
        sort: String? = null,
    ): List<StreetDto> = transaction {
        streetRepository
            .list(
                page = page,
                size = size,
                order = arrayOf(StreetTable.id to SortOrder.ASC),
            )
            .map { s -> StreetDto(s) }
    }

    fun find(
        id: UUID,
    ): StreetDto? = transaction {
        streetRepository
            .find(id)
            ?.let { s -> StreetDto(s) }
    }

    fun create(
        street: StreetInputDto,
    ): StreetDto = transaction {
        streetRepository
            .create(
                name = street.name,
                postCodeId = street.postCode,
            )
            .let { s -> StreetDto(s) }
    }

    fun update(
        id: UUID,
        street: StreetInputDto,
    ): StreetDto = transaction {
        streetRepository
            .update(
                id = id,
                name = street.name,
                postCodeId = street.postCode,
            )
            .let { s -> StreetDto(s) }
    }

    fun delete(
        id: UUID,
    ): Boolean = transaction {
        streetRepository.delete(
            id = id,
        )
        true
    }
}
