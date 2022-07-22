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

import de.fenste.ms.address.infrastructure.tables.CityTable
import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import java.util.UUID

class City(id: EntityID<UUID>) : UUIDEntity(id) {
    companion object EntityClass : UUIDEntityClass<City>(CityTable)

    var country by Country referencedOn CityTable.country

    var state by State optionalReferencedOn CityTable.state

    var name by CityTable.name

    override fun equals(other: Any?): Boolean = when {
        other === null -> false
        other === this -> true
        other is City ->
            id == other.id &&
                country.id == other.country.id &&
                state?.id == other.state?.id &&
                name == other.name
        else -> false
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + country.id.hashCode()
        result = 31 * result + state?.id.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    override fun toString(): String = "City(" +
        "id='$id', " +
        "country='${country.id}', " +
        "state='${state?.id}', " +
        "name='$name')"
}
