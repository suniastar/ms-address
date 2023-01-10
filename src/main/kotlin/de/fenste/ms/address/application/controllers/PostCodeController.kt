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

import de.fenste.ms.address.application.dtos.PostCodeDto
import de.fenste.ms.address.application.dtos.PostCodeInputDto
import de.fenste.ms.address.application.services.PostCodeService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
@Suppress("unused")
class PostCodeController(
    @Autowired private val postCodeService: PostCodeService,
) {
    @SchemaMapping(field = "postCodes", typeName = "Query")
    fun postCodes(
        @Argument limit: Int? = null,
        @Argument offset: Int? = null,
    ): List<PostCodeDto>? = postCodeService.list(
        limit = limit,
        offset = offset?.toLong(),
    )

    @SchemaMapping(field = "postCode", typeName = "Query")
    fun postCode(
        @Argument id: UUID,
    ): PostCodeDto? = postCodeService.find(
        id = id,
    )

    @SchemaMapping(field = "createPostCode", typeName = "Mutation")
    fun createPostCode(
        @Argument postCode: PostCodeInputDto,
    ): PostCodeDto = postCodeService.create(
        postCode = postCode,
    )

    @SchemaMapping(field = "updatePostCode", typeName = "Mutation")
    fun updatePostCode(
        @Argument id: UUID,
        @Argument postCode: PostCodeInputDto,
    ): PostCodeDto = postCodeService.update(
        id = id,
        postCode = postCode,
    )

    @SchemaMapping(field = "deletePostCode", typeName = "Mutation")
    fun deletePostCode(
        @Argument id: UUID,
    ): Boolean = postCodeService.delete(
        id = id,
    )
}
