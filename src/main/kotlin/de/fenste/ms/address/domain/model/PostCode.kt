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

package de.fenste.ms.address.domain.model

import de.fenste.ms.address.infrastructure.tables.PostCodeTable
import de.fenste.ms.address.infrastructure.tables.StreetTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class PostCode(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object EntityClass : UUIDEntityClass<PostCode>(PostCodeTable)

    var city by City referencedOn PostCodeTable.city

    var code by PostCodeTable.code

    val streets by Street referrersOn StreetTable.postCode

    override fun equals(other: Any?): Boolean = when {
        other === null -> false
        other === this -> true
        other is PostCode ->
            id == other.id &&
                city.id == other.city.id &&
                code == other.code
        else -> false
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + city.id.hashCode()
        result = 31 * result + code.hashCode()
        return result
    }

    override fun toString(): String = "PostCode(" +
        "id='$id', " +
        "city='${city.id}', " +
        "code='$code')"
}
