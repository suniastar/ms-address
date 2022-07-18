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

import org.springframework.data.jpa.domain.AbstractPersistable
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table

@Entity
@Table(name = "cities")
open class City(

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "country_id", unique = false, nullable = false)
    open var country: Country = Country(),

    @ManyToOne(optional = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "state_id", unique = false, nullable = true)
    open var state: State? = null,

    @Column(name = "name", unique = false, nullable = false, columnDefinition = "varchar(255)")
    open var name: String = "",

    ) : AbstractPersistable<UUID>() {

    override fun equals(other: Any?): Boolean = when {
        other === null -> false
        other === this -> true
        other is City -> super.equals(other) &&
                country.id == other.country.id &&
                state?.id == other.state?.id &&
                name == other.name
        else -> false
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
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
