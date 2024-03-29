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

package de.fenste.ms.address.application.controllers.graphql

import de.fenste.ms.address.application.dtos.AddressDto
import de.fenste.ms.address.application.dtos.AddressInputDto
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import java.util.UUID

interface AddressGraphql {

    @SchemaMapping(typeName = "Query", field = "addresses")
    fun graphqlGetAddresses(
        @Argument page: Int? = null,
        @Argument size: Int? = null,
        @Argument sort: String? = null,
    ): List<AddressDto>

    @SchemaMapping(typeName = "Query", field = "address")
    fun graphqlGetAddress(
        @Argument id: UUID,
    ): AddressDto?

    @SchemaMapping(typeName = "Mutation", field = "createAddress")
    fun graphqlCreateAddress(
        @Argument address: AddressInputDto,
    ): AddressDto

    @SchemaMapping(typeName = "Mutation", field = "updateAddress")
    fun graphqlUpdateAddress(
        @Argument id: UUID,
        @Argument address: AddressInputDto,
    ): AddressDto

    @SchemaMapping(typeName = "Mutation", field = "deleteAddress")
    fun graphqlDeleteAddress(
        @Argument id: UUID,
    ): Boolean
}
