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

import de.fenste.ms.address.application.dtos.AddressDto
import de.fenste.ms.address.application.dtos.AddressInputDto
import de.fenste.ms.address.application.dtos.StreetDto
import de.fenste.ms.address.application.util.parseSortOrder
import de.fenste.ms.address.domain.exception.NotFoundException
import de.fenste.ms.address.infrastructure.repositories.AddressRepository
import de.fenste.ms.address.infrastructure.tables.AddressTable
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AddressService(
    @Autowired private val addressRepository: AddressRepository,
) {

    fun count(): Int = transaction {
        addressRepository.count()
    }

    fun find(
        id: UUID,
    ): AddressDto? = transaction {
        addressRepository
            .find(id)
            ?.let { a -> AddressDto(a) }
    }

    fun list(
        page: Int? = null,
        size: Int? = null,
        sort: String? = null,
    ): List<AddressDto> = transaction {
        addressRepository
            .list(
                page = page,
                size = size,
                order = sort.parseSortOrder(AddressTable::valueOf),
            )
            .map { a -> AddressDto(a) }
    }

    fun getStreet(
        id: UUID,
    ): StreetDto = transaction {
        val address = addressRepository
            .find(
                id = id,
            )
            ?: throw NotFoundException("The address ($id) does not exist.")

        address
            .street
            .let { s -> StreetDto(s) }
    }

    fun create(
        address: AddressInputDto,
    ): AddressDto = transaction {
        addressRepository
            .create(
                houseNumber = address.houseNumber,
                extra = address.extra,
                streetId = address.street,
            )
            .let { a -> AddressDto(a) }
    }

    fun update(
        id: UUID,
        address: AddressInputDto,
    ): AddressDto = transaction {
        addressRepository
            .update(
                id = id,
                houseNumber = address.houseNumber,
                extra = address.extra,
                streetId = address.street,
            )
            .let { a -> AddressDto(a) }
    }

    fun delete(
        id: UUID,
    ): Boolean = transaction {
        addressRepository
            .delete(
                id = id,
            )
        true
    }
}
