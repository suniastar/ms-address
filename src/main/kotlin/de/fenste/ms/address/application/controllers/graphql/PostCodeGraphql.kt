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

package de.fenste.ms.address.application.controllers.graphql

import de.fenste.ms.address.application.dtos.PostCodeDto
import de.fenste.ms.address.application.dtos.PostCodeInputDto
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.SchemaMapping
import java.util.UUID

interface PostCodeGraphql {

    @SchemaMapping(field = "postCodes", typeName = "Query")
    fun graphqlGetPostCodes(
        @Argument page: Int? = null,
        @Argument size: Int? = null,
        @Argument sort: String? = null,
    ): List<PostCodeDto>

    @SchemaMapping(field = "postCode", typeName = "Query")
    fun graphqGetlPostCode(
        @Argument id: UUID,
    ): PostCodeDto?

    @SchemaMapping(field = "createPostCode", typeName = "Mutation")
    fun graphqlCreatePostCode(
        @Argument postCode: PostCodeInputDto,
    ): PostCodeDto

    @SchemaMapping(field = "updatePostCode", typeName = "Mutation")
    fun graphqlUpdatePostCode(
        @Argument id: UUID,
        @Argument postCode: PostCodeInputDto,
    ): PostCodeDto

    @SchemaMapping(field = "deletePostCode", typeName = "Mutation")
    fun graphqlDeletePostCode(
        @Argument id: UUID,
    ): Boolean
}
