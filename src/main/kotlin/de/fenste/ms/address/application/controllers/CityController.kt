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

import de.fenste.ms.address.application.controllers.api.CityApi
import de.fenste.ms.address.application.controllers.graphql.CityGraphql
import de.fenste.ms.address.application.dtos.CityDto
import de.fenste.ms.address.application.dtos.CityInputDto
import de.fenste.ms.address.application.dtos.CountryDto
import de.fenste.ms.address.application.dtos.PostCodeDto
import de.fenste.ms.address.application.dtos.StateDto
import de.fenste.ms.address.application.services.CityService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.PagedModel
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Controller
@Suppress("TooManyFunctions")
class CityController(
    @Autowired private val cityService: CityService,
) : CityApi, CityGraphql {

    override fun restGetCities(
        page: Int?,
        size: Int?,
        sort: String?,
    ): PagedModel<CityDto> = graphqlGetCities(
        page = page,
        size = size,
        sort = sort,
    )
        .let { list ->
            val e = cityService.count()
            val p = page ?: 0L
            val s = size ?: e
            val t = (e + s - 1) / s
            PagedModel.of(
                list,
                PagedModel.PageMetadata(list.count().toLong(), p.toLong(), e.toLong(), t.toLong()),
                CityApi.generateCityPageLinks(size, page, t, sort),
            )
        }

    override fun graphqlGetCities(
        page: Int?,
        size: Int?,
        sort: String?,
    ): List<CityDto> = cityService
        .list(
            page = page,
            size = size,
            sort = sort,
        )

    override fun restGetCity(
        id: UUID,
    ): EntityModel<CityDto> = graphqlGetCity(
        id = id,
    )
        ?.let { c -> EntityModel.of(c) }
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "The city ($id) does not exist.")

    override fun graphqlGetCity(
        id: UUID,
    ): CityDto? = cityService
        .find(
            id = id,
        )

    override fun restCreateCity(
        city: CityInputDto,
    ): EntityModel<CityDto> = graphqlCreateCity(
        city = city,
    )
        .let { c -> EntityModel.of(c) }

    override fun graphqlCreateCity(
        city: CityInputDto,
    ): CityDto = cityService
        .create(
            city = city,
        )

    override fun restUpdateCity(
        id: UUID,
        city: CityInputDto,
    ): EntityModel<CityDto> = graphqlUpdateCity(
        id = id,
        city = city,
    )
        .let { c -> EntityModel.of(c) }

    override fun graphqlUpdateCity(
        id: UUID,
        city: CityInputDto,
    ): CityDto = cityService
        .update(
            id = id,
            city = city,
        )

    override fun restDeleteCity(
        id: UUID,
    ): Boolean = graphqlDeleteCity(
        id = id,
    )

    override fun graphqlDeleteCity(
        id: UUID,
    ): Boolean = cityService
        .delete(
            id = id,
        )

    override fun restGetCityCountry(
        id: UUID,
    ): EntityModel<CountryDto> = graphqlGetCity(
        id = id,
    )
        ?.let { c -> EntityModel.of(c.country) }
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "The city ($id) does not exist.")

    override fun restGetCityStates(
        id: UUID,
    ): EntityModel<StateDto> = graphqlGetCity(
        id = id,
    )
        ?.let { c ->
            c.state?.let { s -> EntityModel.of(s) }
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "The city ($id) does not ly within a state")
        }
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "The city ($id) does not exist.")

    override fun restGetCityPostCodes(
        id: UUID,
        page: Int?,
        size: Int?,
        sort: String?,
    ): PagedModel<PostCodeDto> = graphqlGetCity(
        id = id,
    )
        ?.let { c ->
            val list = c.postCodes
            val count = list.count()
            PagedModel.of(
                list,
                PagedModel.PageMetadata(count.toLong(), 0, count.toLong(), 1),
                CityApi.generatePostCodesPageLinks(id),
            )
        }
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "The city ($id) does not exist.")
}
