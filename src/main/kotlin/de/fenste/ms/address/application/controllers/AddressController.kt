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

package de.fenste.ms.address.application.controllers

import de.fenste.ms.address.application.dtos.AddressDto
import de.fenste.ms.address.application.dtos.AddressInputDto
import de.fenste.ms.address.application.services.AddressService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
@Suppress("unused")
class AddressController(
    @Autowired private val addressService: AddressService,
) {
    @SchemaMapping(field = "addresses", typeName = "Query")
    fun addresses(
        @Argument limit: Int? = null,
        @Argument offset: Int? = null,
    ): List<AddressDto>? = addressService.list(
        limit = limit,
        offset = offset?.toLong(),
    )

    @SchemaMapping(field = "address", typeName = "Query")
    fun address(
        @Argument id: UUID,
    ): AddressDto? = addressService.find(
        id = id,
    )

    @SchemaMapping(field = "createAddress", typeName = "Mutation")
    fun createAddress(
        @Argument address: AddressInputDto,
    ): AddressDto = addressService.create(
        address = address,
    )

    @SchemaMapping(field = "updateAddress", typeName = "Mutation")
    fun updateAddress(
        @Argument id: UUID,
        @Argument address: AddressInputDto,
    ): AddressDto = addressService.update(
        id = id,
        address = address,
    )

    @SchemaMapping(field = "deleteAddress", typeName = "Mutation")
    fun deleteAddress(
        @Argument id: UUID,
    ): Boolean = addressService.delete(
        id = id,
    )
}
