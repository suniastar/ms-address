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

package de.fenste.ms.address.application.controllers

import de.fenste.ms.address.application.controllers.api.AddressApi
import de.fenste.ms.address.application.controllers.graphql.AddressGraphql
import de.fenste.ms.address.application.dtos.AddressDto
import de.fenste.ms.address.application.dtos.AddressInputDto
import de.fenste.ms.address.application.dtos.StreetDto
import de.fenste.ms.address.application.services.AddressService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.PagedModel
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
@Suppress("unused")
class AddressController(
    @Autowired private val addressService: AddressService,
) : AddressApi, AddressGraphql {

    override fun restGetAddresses(
        page: Int?,
        size: Int?,
        sort: String?,
    ): PagedModel<AddressDto> = graphqlGetAddresses(
        page = page,
        size = size,
        sort = sort,
    )
        .let { list ->
            val e = addressService.count()
            val p = page?.toLong() ?: 0L
            val s = size?.toLong() ?: e
            val t = (e + s - 1) / s
            PagedModel.of(
                list,
                PagedModel.PageMetadata(s, p, e, t),
                AddressApi.generatePageLinks(s, p, t, sort),
            )
        }

    override fun graphqlGetAddresses(
        page: Int?,
        size: Int?,
        sort: String?,
    ): List<AddressDto> = addressService
        .list(
            page = page,
            size = size,
            sort = sort,
        )

    override fun restGetAddress(
        id: UUID,
    ): EntityModel<AddressDto>? = graphqlGetAddress(
        id = id,
    )
        ?.let { a -> EntityModel.of(a) }

    override fun graphqlGetAddress(
        id: UUID,
    ): AddressDto? = addressService
        .find(
            id = id,
        )

    override fun restCreateAddress(
        address: AddressInputDto,
    ): EntityModel<AddressDto> = graphqlCreateAddress(
        address = address,
    )
        .let { a -> EntityModel.of(a) }

    override fun graphqlCreateAddress(
        address: AddressInputDto,
    ): AddressDto = addressService
        .create(
            address = address,
        )

    override fun restUpdateAddress(
        id: UUID,
        address: AddressInputDto,
    ): EntityModel<AddressDto> = graphqlUpdateAddress(
        id = id,
        address = address,
    )
        .let { a -> EntityModel.of(a) }

    override fun graphqlUpdateAddress(
        id: UUID,
        address: AddressInputDto,
    ): AddressDto = addressService
        .update(
            id = id,
            address = address,
        )

    override fun restDeleteAddress(
        id: UUID,
    ): Boolean = graphqlDeleteAddress(
        id = id,
    )

    override fun graphqlDeleteAddress(
        id: UUID,
    ): Boolean = addressService
        .delete(
            id = id,
        )

    override fun restGetAddressStreet(
        id: UUID,
    ): EntityModel<StreetDto> {
        TODO("Not yet implemented")
    }
}
