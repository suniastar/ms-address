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

import de.fenste.ms.address.application.dtos.PostCodeDto
import de.fenste.ms.address.application.dtos.PostCodeInputDto
import de.fenste.ms.address.application.util.PageHelper
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.PagedModel
import org.springframework.hateoas.server.mvc.BasicLinkBuilder
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

@RequestMapping("/api/postcode")
interface PostCodeApi {

    companion object LINKER {
        private val BASE_URI = BasicLinkBuilder.linkToCurrentMapping()

        fun generatePageLinks(size: Long, page: Long, total: Long, sort: String?): Set<Link> =
            PageHelper.generatePageLinks("$BASE_URI/api/postcode", size, page, total, sort)

        fun generateEntityLinks(id: UUID): Set<Link> = setOf(
            Link.of("${BASE_URI}/api/postcode/$id").withSelfRel(),
        )
    }

    @ResponseBody
    @GetMapping
    fun restGetPostCodes(
        @RequestParam page: Int? = null,
        @RequestParam size: Int? = null,
        @RequestParam sort: String? = null,
    ): PagedModel<PostCodeDto>

    @ResponseBody
    @GetMapping("/{id}")
    fun restGetPostCode(
        @PathVariable id: UUID,
    ): EntityModel<PostCodeDto>?

    @ResponseBody
    @PostMapping
    fun restCreatePostCode(
        @RequestBody postCode: PostCodeInputDto,
    ): EntityModel<PostCodeDto>

    @ResponseBody
    @PutMapping("/{id}")
    fun restUpdatePostCode(
        @PathVariable id: UUID,
        @RequestBody postCode: PostCodeInputDto,
    ): EntityModel<PostCodeDto>

    @ResponseBody
    @DeleteMapping("/{id}")
    fun restDeletePostCode(
        @PathVariable id: UUID,
    ): Boolean

    @ResponseBody
    @GetMapping("/{id}/city")
    fun restGetPostCodeCity(
        @PathVariable id: UUID,
    )

    @ResponseBody
    @GetMapping("/{id}/streets")
    fun restGetPostCodeStreets(
        @PathVariable id: UUID,
        @RequestParam page: Int? = null,
        @RequestParam size: Int? = null,
        @RequestParam sort: String? = null,
    )
}
