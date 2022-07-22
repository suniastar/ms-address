/*
 * Copyright (c) 2022 Frederik Enste <frederik@fenste.de>.
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

package de.fenste.ms.address.application.dtos

import de.fenste.ms.address.domain.model.Street
import org.springframework.graphql.data.method.annotation.SchemaMapping

@SchemaMapping(typeName = "Street")
data class StreetDto(

    @get:SchemaMapping(field = "id", typeName = "String")
    val id: String,

    @get:SchemaMapping(field = "name", typeName = "String")
    val name: String,

    @get:SchemaMapping(field = "postCode", typeName = "PostCode")
    val postCode: PostCodeDto,
) {
    constructor(street: Street) : this(
        id = street.id.value.toString(),
        name = street.name,
        postCode = PostCodeDto(street.postCode),
    )
}
