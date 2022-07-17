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
import javax.persistence.Table

@Entity
@Table(name = "countries")
open class Country(

    @Column(name = "alpha2", unique = true, nullable = false, columnDefinition = "char(2)")
    open var alpha2: String = "",

    @Column(name = "alpha3", unique = true, nullable = false, columnDefinition = "char(3)")
    open var alpha3: String = "",

    @Column(name = "name", unique = true, nullable = false, columnDefinition = "varchar(255)")
    open var name: String = "",

    @Column(name = "localizedName", unique = true, nullable = false, columnDefinition = "varchar(255)")
    open var localizedName: String = "",

    ) : AbstractPersistable<UUID>() {

    override fun equals(other: Any?): Boolean = when {
        other === null -> false
        other === this -> true
        other is Country -> super.equals(other) &&
            alpha2 == other.alpha2 &&
            alpha3 == other.alpha3 &&
            name == other.name &&
            localizedName == other.localizedName
        else -> false
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
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
