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

import de.fenste.ms.address.application.controllers.api.CountryApi
import de.fenste.ms.address.application.controllers.graphql.CountryGraphql
import de.fenste.ms.address.application.dtos.CityDto
import de.fenste.ms.address.application.dtos.CountryDto
import de.fenste.ms.address.application.dtos.CountryInputDto
import de.fenste.ms.address.application.dtos.StateDto
import de.fenste.ms.address.application.services.CountryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.PagedModel
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Controller
@Suppress("TooManyFunctions")
class CountryController(
    @Autowired private val countryService: CountryService,
) : CountryApi, CountryGraphql {

    override fun restGetCountries(
        page: Int?,
        size: Int?,
        sort: String?,
    ): PagedModel<CountryDto> = graphqlGetCountries(
        page = page,
        size = size,
        sort = sort,
    )
        .let { list ->
            val e = countryService.count()
            val p = page ?: 0
            val s = size ?: e
            val t = (e + s - 1) / s
            PagedModel.of(
                list,
                PagedModel.PageMetadata(list.count().toLong(), p.toLong(), e.toLong(), t.toLong()),
                CountryApi.generateCountryPageLinks(size, page, t, sort),
            )
        }

    override fun graphqlGetCountries(
        page: Int?,
        size: Int?,
        sort: String?,
    ): List<CountryDto> = countryService
        .list(
            page = page,
            size = size,
            sort = sort,
        )

    override fun restGetCountry(
        id: UUID,
    ): EntityModel<CountryDto> = graphqlGetCountry(
        id = id,
    )
        ?.let { c -> EntityModel.of(c) }
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "The country ($id) does not exist.")

    override fun graphqlGetCountry(
        id: UUID?,
        alpha2: String?,
        alpha3: String?,
    ): CountryDto? = countryService
        .find(
            id = id,
            alpha2 = alpha2,
            alpha3 = alpha3,
        )

    override fun restCreateCountry(
        country: CountryInputDto,
    ): EntityModel<CountryDto> = graphqlCreateCountry(
        country = country,
    )
        .let { c -> EntityModel.of(c) }

    override fun graphqlCreateCountry(
        country: CountryInputDto,
    ): CountryDto = countryService
        .create(
            country = country,
        )

    override fun restUpdateCountry(
        id: UUID,
        country: CountryInputDto,
    ): EntityModel<CountryDto> = graphqlUpdateCountry(
        id = id,
        country = country,
    )
        .let { c -> EntityModel.of(c) }

    override fun graphqlUpdateCountry(
        id: UUID,
        country: CountryInputDto,
    ): CountryDto = countryService
        .update(
            id = id,
            country = country,
        )

    override fun restDeleteCountry(
        id: UUID,
    ): Boolean = graphqlDeleteCountry(
        id = id,
    )

    override fun graphqlDeleteCountry(
        id: UUID,
    ): Boolean = countryService
        .delete(
            id = id,
        )

    override fun restGetCountryStates(
        id: UUID,
        page: Int?,
        size: Int?,
        sort: String?,
    ): PagedModel<StateDto> = graphqlGetCountry(
        id = id,
    )
        ?.let { c ->
            val list = c.states.toList()
            val count = list.count()
            PagedModel.of(
                list,
                PagedModel.PageMetadata(count.toLong(), 0, count.toLong(), 1),
                CountryApi.generateStatePageLinks(id),
            )
        }
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "The country ($id) does not exist.")

    override fun restGetCountryCities(
        id: UUID,
        page: Int?,
        size: Int?,
        sort: String?,
    ): PagedModel<CityDto> = graphqlGetCountry(
        id = id,
    )
        ?.let { c ->
            val list = c.cities.toList()
            val count = list.count()
            PagedModel.of(
                list,
                PagedModel.PageMetadata(count.toLong(), 0, count.toLong(), 1),
                CountryApi.generateCityPageLinks(id),
            )
        }
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "The country ($id) does not exist.")
}
