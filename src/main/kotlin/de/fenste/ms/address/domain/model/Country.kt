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

import de.fenste.ms.address.infrastructure.tables.CountryTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class Country(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object EntityClass : UUIDEntityClass<Country>(CountryTable)

    var alpha2 by CountryTable.alpha2

    var alpha3 by CountryTable.alpha3

    var name by CountryTable.name

    var localizedName by CountryTable.localizedName

    override fun equals(other: Any?): Boolean = when {
        other === null -> false
        other === this -> true
        other is Country -> id == other.id &&
            alpha2 == other.alpha2 &&
            alpha3 == other.alpha3 &&
            name == other.name &&
            localizedName == other.localizedName
        else -> false
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + alpha2.hashCode()
        result = 31 * result + alpha3.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + localizedName.hashCode()
        return result
    }

    override fun toString(): String = "Country(" +
        "id='$id', " +
        "alpha2='$alpha2', " +
        "alpha3='$alpha3', " +
        "name='$name', " +
        "localizedName='$localizedName')"
}
