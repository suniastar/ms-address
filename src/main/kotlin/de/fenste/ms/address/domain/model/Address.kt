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
@Table(name = "addresses")
open class Address(

    @Column(name = "house_number", unique = false, nullable = false, columnDefinition = "varchar(255)")
    open var houseNumber: String = "",

    @Column(name = "extra", unique = false, nullable = true, columnDefinition = "varchar(255)")
    open var extra: String? = null,

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "street_id", unique = false, nullable = false)
    open var street: Street = Street(),

    ) : AbstractPersistable<UUID>() {

    override fun equals(other: Any?): Boolean = when {
        other === null -> false
        other === this -> true
        other is Address -> super.equals(other) &&
            houseNumber == other.houseNumber &&
            extra == other.extra &&
            street.id == other.street.id
        else -> false
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + houseNumber.hashCode()
        result = 31 * result + extra.hashCode()
        result = 31 * result + street.id.hashCode()
        return result
    }

    override fun toString(): String = "Address(" +
        "id='$id', " +
        "houseNumber='$houseNumber', " +
        "extra='$extra', " +
        "street='${street.id}')"
}
