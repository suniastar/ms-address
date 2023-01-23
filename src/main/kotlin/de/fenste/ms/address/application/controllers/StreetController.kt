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

import de.fenste.ms.address.application.controllers.api.StreetApi
import de.fenste.ms.address.application.controllers.graphql.StreetGraphql
import de.fenste.ms.address.application.dtos.AddressDto
import de.fenste.ms.address.application.dtos.PostCodeDto
import de.fenste.ms.address.application.dtos.StreetDto
import de.fenste.ms.address.application.dtos.StreetInputDto
import de.fenste.ms.address.application.services.StreetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.PagedModel
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class StreetController(
    @Autowired private val streetService: StreetService,
) : StreetApi, StreetGraphql {

    override fun restGetStreets(
        page: Int?,
        size: Int?,
        sort: String?,
    ): PagedModel<StreetDto> = graphqlGetStreets(
        page = page,
        size = size,
        sort = sort,
    )
        .let { list ->
            val e = streetService.count()
            val p = page?.toLong() ?: 0L
            val s = size?.toLong() ?: e
            val t = (e + s - 1) / s
            PagedModel.of(
                list,
                PagedModel.PageMetadata(s, p, e, t),
                StreetApi.generatePageLinks(s, p, t, sort),
            )
        }

    override fun graphqlGetStreets(
        page: Int?,
        size: Int?,
        sort: String?,
    ): List<StreetDto> = streetService
        .list(
            page = page,
            size = size,
            sort = sort,
        )

    override fun restGetStreet(
        id: UUID,
    ): EntityModel<StreetDto>? = graphqlGetStreet(
        id = id,
    )
        ?.let { s -> EntityModel.of(s) }

    override fun graphqlGetStreet(
        id: UUID,
    ): StreetDto? = streetService
        .find(
            id = id,
        )

    override fun restCreateStreet(
        street: StreetInputDto,
    ): EntityModel<StreetDto> = graphqlCreateStreet(
        street = street,
    )
        .let { s -> EntityModel.of(s) }

    override fun graphqlCreateStreet(
        street: StreetInputDto,
    ): StreetDto = streetService
        .create(
            street = street,
        )

    override fun restUpdateStreet(
        id: UUID,
        street: StreetInputDto,
    ): EntityModel<StreetDto> = graphqlUpdateStreet(
        id = id,
        street = street,
    )
        .let { s -> EntityModel.of(s) }

    override fun graphqlUpdateStreet(
        id: UUID,
        street: StreetInputDto,
    ): StreetDto = streetService
        .update(
            id = id,
            street = street,
        )

    override fun restDeleteStreet(
        id: UUID,
    ): Boolean = graphqlDeleteStreet(
        id = id,
    )

    override fun graphqlDeleteStreet(
        id: UUID,
    ): Boolean = streetService
        .delete(
            id = id,
        )

    override fun restGetStreetPostCode(
        id: UUID,
    ): EntityModel<PostCodeDto> {
        TODO("Not yet implemented")
    }

    override fun restGetStreetAddresses(
        id: UUID,
        page: Int?,
        size: Int?,
        sort: String?,
    ): PagedModel<AddressDto> {
        TODO("Not yet implemented")
    }
}
