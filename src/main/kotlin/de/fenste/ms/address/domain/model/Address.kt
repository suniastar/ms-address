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

package de.fenste.ms.address.domain.model

import de.fenste.ms.address.infrastructure.tables.AddressTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class Address(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object EntityClass : UUIDEntityClass<Address>(AddressTable)

    var street by Street referencedOn AddressTable.street

    var houseNumber by AddressTable.houseNumber

    var extra by AddressTable.extra

    override fun equals(other: Any?): Boolean = when {
        other === null -> false
        other === this -> true
        other is Address ->
            id == other.id &&
                street.id == other.street.id &&
                houseNumber == other.houseNumber &&
                extra == other.extra
        else -> false
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + street.id.hashCode()
        result = 31 * result + houseNumber.hashCode()
        result = 31 * result + extra.hashCode()
        return result
    }

    override fun toString(): String = "Address(" +
        "id='$id', " +
        "street='${street.id}', " +
        "houseNumber='$houseNumber', " +
        "extra='$extra')"
}
