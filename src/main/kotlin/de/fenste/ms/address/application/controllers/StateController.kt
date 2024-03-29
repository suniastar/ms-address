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

import de.fenste.ms.address.application.controllers.api.StateApi
import de.fenste.ms.address.application.controllers.graphql.StateGraphql
import de.fenste.ms.address.application.dtos.CityDto
import de.fenste.ms.address.application.dtos.CountryDto
import de.fenste.ms.address.application.dtos.StateDto
import de.fenste.ms.address.application.dtos.StateInputDto
import de.fenste.ms.address.application.services.StateService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.PagedModel
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Controller
@Suppress("TooManyFunctions")
class StateController(
    @Autowired private val stateService: StateService,
) : StateApi, StateGraphql {

    override fun restGetStates(
        page: Int?,
        size: Int?,
        sort: String?,
    ): PagedModel<StateDto> = graphqlGetStates(
        page = page,
        size = size,
        sort = sort,
    )
        .let { list ->
            val e = stateService.count()
            val p = page ?: 0L
            val s = size ?: e
            val t = (e + s - 1) / s
            PagedModel.of(
                list,
                PagedModel.PageMetadata(list.count().toLong(), p.toLong(), e.toLong(), t.toLong()),
                StateApi.generateStatePageLinks(size, page, t, sort),
            )
        }

    override fun graphqlGetStates(
        page: Int?,
        size: Int?,
        sort: String?,
    ): List<StateDto> = stateService
        .list(
            page = page,
            size = size,
            sort = sort,
        )

    override fun restGetState(
        id: UUID,
    ): EntityModel<StateDto> = graphqlGetState(
        id = id,
    )
        ?.let { s -> EntityModel.of(s) }
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "The state ($id) does not exist.")

    override fun graphqlGetState(
        id: UUID,
    ): StateDto? = stateService
        .find(
            id = id,
        )

    override fun restCreateState(
        state: StateInputDto,
    ): EntityModel<StateDto> = graphqlCreateState(
        state = state,
    )
        .let { s -> EntityModel.of(s) }

    override fun graphqlCreateState(
        state: StateInputDto,
    ): StateDto = stateService
        .create(
            state = state,
        )

    override fun restUpdateState(
        id: UUID,
        state: StateInputDto,
    ): EntityModel<StateDto> = graphqlUpdateState(
        id = id,
        state = state,
    )
        .let { s -> EntityModel.of(s) }

    override fun graphqlUpdateState(
        id: UUID,
        state: StateInputDto,
    ): StateDto = stateService
        .update(
            id = id,
            state = state,
        )

    override fun restDeleteState(
        id: UUID,
    ): Boolean = graphqlDeleteState(
        id = id,
    )

    override fun graphqlDeleteState(
        id: UUID,
    ): Boolean = stateService
        .delete(
            id = id,
        )

    override fun restGetStateCountry(
        id: UUID,
    ): EntityModel<CountryDto> = graphqlGetState(
        id = id,
    )
        ?.let { s -> EntityModel.of(s.country) }
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "The state ($id) does not exist.")

    override fun restGetStateCities(
        id: UUID,
        page: Int?,
        size: Int?,
        sort: String?,
    ): PagedModel<CityDto> = graphqlGetState(
        id = id,
    )
        ?.let { s ->
            val list = s.cities
            val count = list.count()
            PagedModel.of(
                list,
                PagedModel.PageMetadata(count.toLong(), 0, count.toLong(), 1),
                StateApi.generateCityPageLinks(id),
            )
        }
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "The state ($id) does not exist.")
}
