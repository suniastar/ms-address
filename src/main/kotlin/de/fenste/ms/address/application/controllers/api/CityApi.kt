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

package de.fenste.ms.address.application.controllers.api

import de.fenste.ms.address.application.dtos.CityDto
import de.fenste.ms.address.application.dtos.CityInputDto
import de.fenste.ms.address.application.dtos.CountryDto
import de.fenste.ms.address.application.dtos.PostCodeDto
import de.fenste.ms.address.application.dtos.StateDto
import de.fenste.ms.address.application.util.PageHelper
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.PagedModel
import org.springframework.hateoas.mediatype.Affordances
import org.springframework.hateoas.server.mvc.BasicLinkBuilder
import org.springframework.http.HttpMethod
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import java.util.UUID

@RequestMapping("/api/city")
interface CityApi {

    companion object LINKER {
        private val BASE_URI = BasicLinkBuilder.linkToCurrentMapping()

        fun generatePageLinks(size: Int?, page: Int?, totalPages: Int?, sort: String?): Set<Link> =
            PageHelper.generatePageLinks(
                "$BASE_URI/api/city",
                size,
                page,
                totalPages,
                sort,
            ) { l ->
                Affordances.of(l)
                    .afford(HttpMethod.TRACE)
                    .andAfford(HttpMethod.POST)
                    .withName("create")
                    .withInput(CityInputDto::class.java)
                    .withOutput(CityDto::class.java)
                    .toLink()
            }

        fun generateEntityLinks(id: UUID): Set<Link> = setOf(
            Affordances.of(Link.of("$BASE_URI/api/city/$id").withSelfRel())
                .afford(HttpMethod.TRACE)
                .andAfford(HttpMethod.PUT)
                .withName("update")
                .withInput(CityInputDto::class.java)
                .withOutput(CityDto::class.java)
                .andAfford(HttpMethod.DELETE)
                .withName("delete")
                .withOutput(Boolean::class.java)
                .toLink(),
            Link.of("$BASE_URI/api/city/$id/country").withRel("country"),
            Link.of("$BASE_URI/api/city/$id/state").withRel("state"),
            Link.of("$BASE_URI/api/city/$id/postcodes{?page,size,sort}").withRel("postcodes"),
        )
    }

    @ResponseBody
    @GetMapping
    fun restGetCities(
        @RequestParam page: Int? = null,
        @RequestParam size: Int? = null,
        @RequestParam sort: String? = null,
    ): PagedModel<CityDto>

    @ResponseBody
    @GetMapping("/{id}")
    fun restGetCity(
        @PathVariable id: UUID,
    ): EntityModel<CityDto>

    @ResponseBody
    @PostMapping
    fun restCreateCity(
        @RequestBody city: CityInputDto,
    ): EntityModel<CityDto>

    @ResponseBody
    @PutMapping("/{id}")
    fun restUpdateCity(
        @PathVariable id: UUID,
        @RequestBody city: CityInputDto,
    ): EntityModel<CityDto>

    @ResponseBody
    @DeleteMapping("/{id}")
    fun restDeleteCity(
        @PathVariable id: UUID,
    ): Boolean

    @ResponseBody
    @GetMapping("/{id}/country")
    fun restGetCityCountry(
        @PathVariable id: UUID,
    ): EntityModel<CountryDto>

    @ResponseBody
    @GetMapping("/{id}/state")
    fun restGetCityStates(
        @PathVariable id: UUID,
    ): PagedModel<StateDto>

    @ResponseBody
    @GetMapping("/{id}/postcodes")
    fun restGetCityPostCodes(
        @PathVariable id: UUID,
        @RequestParam page: Int? = null,
        @RequestParam size: Int? = null,
        @RequestParam sort: String? = null,
    ): PagedModel<PostCodeDto>
}
