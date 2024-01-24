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
import de.fenste.ms.address.application.dtos.CountryDto
import de.fenste.ms.address.application.dtos.StateDto
import de.fenste.ms.address.application.dtos.StateInputDto
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

@RequestMapping("/api/state")
interface StateApi {

    companion object LINKER {
        private val BASE_URI = BasicLinkBuilder.linkToCurrentMapping()

        fun generateStatePageLinks(size: Int?, page: Int?, totalPages: Int?, sort: String?): Set<Link> = PageHelper
            .generatePageLinks(
                "$BASE_URI/api/state",
                size,
                page,
                totalPages,
                sort,
            ) { l ->
                Affordances.of(l)
                    .afford(HttpMethod.TRACE)
                    .andAfford(HttpMethod.POST)
                    .withName("create")
                    .withInput(StateInputDto::class.java)
                    .withOutput(StateDto::class.java)
                    .toLink()
            }

        fun generateEntityLinks(id: UUID): Set<Link> = setOf(
            Affordances.of(Link.of("$BASE_URI/api/state/$id").withSelfRel())
                .afford(HttpMethod.TRACE)
                .andAfford(HttpMethod.PUT)
                .withName("update")
                .withInput(StateInputDto::class.java)
                .withOutput(StateDto::class.java)
                .andAfford(HttpMethod.DELETE)
                .withName("delete")
                .withOutput(Boolean::class.java)
                .toLink(),
            Link.of("$BASE_URI/api/state/$id/country").withRel("country"),
            Link.of("$BASE_URI/api/state/$id/cities{?page,size,sort}").withRel("cities"),
        )

        fun generateCityPageLinks(id: UUID): Set<Link> = PageHelper
            .generatePageLinks(
                "$BASE_URI/api/state/$id/cities",
                null,
                null,
                null,
                null,
            )
    }

    @ResponseBody
    @GetMapping("/{id}")
    fun restGetState(
        @PathVariable id: UUID,
    ): EntityModel<StateDto>

    @ResponseBody
    @GetMapping
    fun restGetStates(
        @RequestParam page: Int? = null,
        @RequestParam size: Int? = null,
        @RequestParam sort: String? = null,
    ): PagedModel<StateDto>

    @ResponseBody
    @GetMapping("/{id}/country")
    fun restGetStateCountry(
        @PathVariable id: UUID,
    ): EntityModel<CountryDto>

    @ResponseBody
    @GetMapping("/{id}/cities")
    fun restGetStateCities(
        @PathVariable id: UUID,
        @RequestParam page: Int? = null,
        @RequestParam size: Int? = null,
        @RequestParam sort: String? = null,
    ): PagedModel<CityDto>

    @ResponseBody
    @PostMapping
    fun restCreateState(
        @RequestBody state: StateInputDto,
    ): EntityModel<StateDto>

    @ResponseBody
    @PutMapping("/{id}")
    fun restUpdateState(
        @PathVariable id: UUID,
        @RequestBody state: StateInputDto,
    ): EntityModel<StateDto>

    @ResponseBody
    @DeleteMapping("/{id}")
    fun restDeleteState(
        @PathVariable id: UUID,
    ): Boolean
}
