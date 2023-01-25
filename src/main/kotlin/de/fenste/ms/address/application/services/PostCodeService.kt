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

import de.fenste.ms.address.application.dtos.PostCodeDto
import de.fenste.ms.address.application.dtos.PostCodeInputDto
import de.fenste.ms.address.infrastructure.repositories.PostCodeRepository
import de.fenste.ms.address.infrastructure.tables.PostCodeTable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PostCodeService(
    @Autowired private val postCodeRepository: PostCodeRepository,
) {

    fun count(): Long = transaction {
        postCodeRepository.count()
    }

    @Suppress("UnusedPrivateMember") // TODO implement sort
    fun list(
        page: Int? = null,
        size: Int? = null,
        sort: String? = null,
    ): List<PostCodeDto> = transaction {
        postCodeRepository
            .list(
                page = page,
                size = size,
                order = arrayOf(PostCodeTable.id to SortOrder.ASC),
            )
            .map { p -> PostCodeDto(p) }
    }

    fun find(
        id: UUID,
    ): PostCodeDto? = transaction {
        postCodeRepository
            .find(id)
            ?.let { p -> PostCodeDto(p) }
    }

    fun create(
        postCode: PostCodeInputDto,
    ): PostCodeDto = transaction {
        postCodeRepository
            .create(
                code = postCode.code,
                cityId = postCode.city,
            )
            .let { p -> PostCodeDto(p) }
    }

    fun update(
        id: UUID,
        postCode: PostCodeInputDto,
    ): PostCodeDto = transaction {
        postCodeRepository
            .update(
                id = id,
                code = postCode.code,
                cityId = postCode.city,
            )
            .let { p -> PostCodeDto(p) }
    }

    fun delete(
        id: UUID,
    ): Boolean = transaction {
        postCodeRepository
            .delete(
                id = id,
            )
        true
    }
}
