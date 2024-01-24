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
import de.fenste.ms.address.application.dtos.PostCodeDto
import de.fenste.ms.address.application.dtos.PostCodeInputDto
import de.fenste.ms.address.application.dtos.StreetDto
import de.fenste.ms.address.application.util.parseSortOrder
import de.fenste.ms.address.domain.exception.NotFoundException
import de.fenste.ms.address.infrastructure.repositories.PostCodeRepository
import de.fenste.ms.address.infrastructure.tables.PostCodeTable
import de.fenste.ms.address.infrastructure.tables.StreetTable
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class PostCodeService(
    @Autowired private val postCodeRepository: PostCodeRepository,
) {

    fun count(): Int = transaction {
        postCodeRepository.count()
    }

    fun find(
        id: UUID,
    ): PostCodeDto? = transaction {
        postCodeRepository
            .find(id)
            ?.let { p -> PostCodeDto(p) }
    }

    fun list(
        page: Int? = null,
        size: Int? = null,
        sort: String? = null,
    ): List<PostCodeDto> = transaction {
        postCodeRepository
            .list(
                page = page,
                size = size,
                order = sort.parseSortOrder(PostCodeTable::valueOf),
            )
            .map { p -> PostCodeDto(p) }
    }

    fun getCity(
        id: UUID,
    ): CityDto = transaction {
        val postCode = postCodeRepository
            .find(
                id = id,
            )
            ?: throw NotFoundException("The post code ($id) does not exist.")

        postCode
            .city
            .let { c -> CityDto(c) }
    }

    fun listStreets(
        id: UUID,
        page: Int? = null,
        size: Int? = null,
        sort: String? = null,
    ): List<StreetDto> = transaction {
        val postCode = postCodeRepository
            .find(
                id = id,
            )
            ?: throw NotFoundException("The post code ($id) does not exist.")

        postCodeRepository
            .listStreets(
                postCode = postCode,
                page = page,
                size = size,
                order = sort.parseSortOrder(StreetTable::valueOf),
            )
            .map { s -> StreetDto(s) }
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
