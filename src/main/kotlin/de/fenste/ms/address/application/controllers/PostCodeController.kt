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

import de.fenste.ms.address.application.controllers.api.PostCodeApi
import de.fenste.ms.address.application.controllers.graphql.PostCodeGraphql
import de.fenste.ms.address.application.dtos.CityDto
import de.fenste.ms.address.application.dtos.PostCodeDto
import de.fenste.ms.address.application.dtos.PostCodeInputDto
import de.fenste.ms.address.application.dtos.StreetDto
import de.fenste.ms.address.application.services.PostCodeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.PagedModel
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Controller
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@Controller
@Suppress("TooManyFunctions")
class PostCodeController(
    @Autowired private val postCodeService: PostCodeService,
) : PostCodeApi, PostCodeGraphql {

    override fun restGetPostCodes(
        page: Int?,
        size: Int?,
        sort: String?,
    ): PagedModel<PostCodeDto> = graphqlGetPostCodes(
        page = page,
        size = size,
        sort = sort,
    )
        .let { list ->
            val e = postCodeService.count()
            val p = page ?: 0L
            val s = size ?: e
            val t = (e + s - 1) / s
            PagedModel.of(
                list,
                PagedModel.PageMetadata(list.count().toLong(), p.toLong(), e.toLong(), t.toLong()),
                PostCodeApi.generatePostCodePageLinks(size, page, t, sort),
            )
        }

    override fun graphqlGetPostCodes(
        page: Int?,
        size: Int?,
        sort: String?,
    ): List<PostCodeDto> = postCodeService
        .list(
            page = page,
            size = size,
            sort = sort,
        )

    override fun restGetPostCode(
        id: UUID,
    ): EntityModel<PostCodeDto> = graphqGetlPostCode(
        id = id,
    )
        ?.let { p -> EntityModel.of(p) }
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "The post code ($id) does not exist.")

    override fun graphqGetlPostCode(
        id: UUID,
    ): PostCodeDto? = postCodeService
        .find(
            id = id,
        )

    override fun restCreatePostCode(
        postCode: PostCodeInputDto,
    ): EntityModel<PostCodeDto> = graphqlCreatePostCode(
        postCode = postCode,
    )
        .let { p -> EntityModel.of(p) }

    override fun graphqlCreatePostCode(
        postCode: PostCodeInputDto,
    ): PostCodeDto = postCodeService
        .create(
            postCode = postCode,
        )

    override fun restUpdatePostCode(
        id: UUID,
        postCode: PostCodeInputDto,
    ): EntityModel<PostCodeDto> = graphqlUpdatePostCode(
        id = id,
        postCode = postCode,
    )
        .let { p -> EntityModel.of(p) }

    override fun graphqlUpdatePostCode(
        id: UUID,
        postCode: PostCodeInputDto,
    ): PostCodeDto = postCodeService
        .update(
            id = id,
            postCode = postCode,
        )

    override fun restDeletePostCode(
        id: UUID,
    ): Boolean = graphqlDeletePostCode(
        id = id,
    )

    override fun graphqlDeletePostCode(
        id: UUID,
    ): Boolean = postCodeService
        .delete(
            id = id,
        )

    override fun restGetPostCodeCity(
        id: UUID,
    ): EntityModel<CityDto> = graphqGetlPostCode(
        id = id,
    )
        ?.let { p -> EntityModel.of(p.city) }
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "The post code ($id) does not exist.")

    override fun restGetPostCodeStreets(
        id: UUID,
        page: Int?,
        size: Int?,
        sort: String?,
    ): PagedModel<StreetDto> = graphqGetlPostCode(
        id = id,
    )
        ?.let { p ->
            val list = p.streets
            val count = list.count()
            PagedModel.of(
                list,
                PagedModel.PageMetadata(count.toLong(), 0, count.toLong(), 1),
                PostCodeApi.generateStreetsPageLinks(id),
            )
        }
        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "The post code ($id) does not exist.")
}
