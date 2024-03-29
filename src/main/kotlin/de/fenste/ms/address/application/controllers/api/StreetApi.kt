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

import de.fenste.ms.address.application.dtos.AddressDto
import de.fenste.ms.address.application.dtos.PostCodeDto
import de.fenste.ms.address.application.dtos.StreetDto
import de.fenste.ms.address.application.dtos.StreetInputDto
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

@RequestMapping("/api/street")
interface StreetApi {

    companion object LINKER {
        private val BASE_URI = BasicLinkBuilder.linkToCurrentMapping()

        fun generateStreetPageLinks(size: Int?, page: Int?, totalPages: Int?, sort: String?): Set<Link> = PageHelper
            .generatePageLinks(
                "$BASE_URI/api/street",
                size,
                page,
                totalPages,
                sort,
            ) { l ->
                Affordances.of(l)
                    .afford(HttpMethod.TRACE)
                    .andAfford(HttpMethod.POST)
                    .withName("create")
                    .withInput(StreetInputDto::class.java)
                    .withOutput(StreetDto::class.java)
                    .toLink()
            }

        fun generateEntityLinks(id: UUID): Set<Link> = setOf(
            Affordances.of(Link.of("$BASE_URI/api/street/$id").withSelfRel())
                .afford(HttpMethod.TRACE)
                .andAfford(HttpMethod.PUT)
                .withName("update")
                .withInput(StreetInputDto::class.java)
                .withOutput(StreetDto::class.java)
                .andAfford(HttpMethod.DELETE)
                .withName("delete")
                .withOutput(Boolean::class.java)
                .toLink(),
            Link.of("$BASE_URI/api/street/$id/postcode").withRel("postcode"),
            Link.of("$BASE_URI/api/street/$id/addresses{?page,size,sort}").withRel("addresses"),
        )

        fun generateAddressPageLinks(id: UUID): Set<Link> = PageHelper
            .generatePageLinks(
                "$BASE_URI/api/street/$id/addresses",
                null,
                null,
                null,
                null,
            )
    }

    @ResponseBody
    @GetMapping
    fun restGetStreets(
        @RequestParam page: Int? = null,
        @RequestParam size: Int? = null,
        @RequestParam sort: String? = null,
    ): PagedModel<StreetDto>

    @ResponseBody
    @GetMapping("/{id}")
    fun restGetStreet(
        @PathVariable id: UUID,
    ): EntityModel<StreetDto>

    @ResponseBody
    @PostMapping
    fun restCreateStreet(
        @RequestBody street: StreetInputDto,
    ): EntityModel<StreetDto>

    @ResponseBody
    @PutMapping("/{id}")
    fun restUpdateStreet(
        @PathVariable id: UUID,
        @RequestBody street: StreetInputDto,
    ): EntityModel<StreetDto>

    @ResponseBody
    @DeleteMapping("/{id}")
    fun restDeleteStreet(
        @PathVariable id: UUID,
    ): Boolean

    @ResponseBody
    @GetMapping("/{id}/postcode")
    fun restGetStreetPostCode(
        @PathVariable id: UUID,
    ): EntityModel<PostCodeDto>

    @ResponseBody
    @GetMapping("/{id}/addresses")
    fun restGetStreetAddresses(
        @PathVariable id: UUID,
        @RequestParam page: Int? = null,
        @RequestParam size: Int? = null,
        @RequestParam sort: String? = null,
    ): PagedModel<AddressDto>
}
