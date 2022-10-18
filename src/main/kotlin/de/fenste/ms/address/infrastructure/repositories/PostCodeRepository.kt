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

package de.fenste.ms.address.infrastructure.repositories

import de.fenste.ms.address.domain.model.City
import de.fenste.ms.address.domain.model.PostCode
import de.fenste.ms.address.infrastructure.tables.CityTable
import de.fenste.ms.address.infrastructure.tables.PostCodeTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.SizedIterable
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
class PostCodeRepository {

    private fun idOf(
        code: String,
        cityId: UUID,
    ): EntityID<UUID>? = PostCodeTable
        .slice(PostCodeTable.id)
        .select { (PostCodeTable.code eq code) and (PostCodeTable.city eq cityId) }
        .limit(1)
        .notForUpdate()
        .firstOrNull()
        ?.let { p -> p[PostCodeTable.id] }

    fun list(
        limit: Int? = null,
        offset: Long = 0L,
        vararg order: Pair<Expression<*>, SortOrder> = arrayOf(PostCodeTable.id to SortOrder.ASC),
    ): SizedIterable<PostCode> = when {
        limit != null ->
            PostCode
                .all()
                .orderBy(*order)
                .limit(limit, offset)
                .notForUpdate()

        else ->
            PostCode
                .all()
                .orderBy(*order)
                .notForUpdate()
    }

    fun find(
        id: UUID,
    ): PostCode? = PostCode
        .find { PostCodeTable.id eq id }
        .limit(1)
        .notForUpdate()
        .firstOrNull()

    fun create(
        code: String,
        cityId: UUID,
    ): PostCode {
        require(
            idOf(
                code = code,
                cityId = cityId,
            ) == null,
        ) { "A post code with this code and parent city does already exist." }

        val city = City
            .find { CityTable.id eq cityId }
            .limit(1)
            .notForUpdate()
            .firstOrNull()

        requireNotNull(city) { "The city ($cityId) does not exist" }

        return PostCode.new {
            this.code = code
            this.city = city
        }
    }

    fun update(
        id: UUID,
        code: String? = null,
        cityId: UUID? = null,
    ): PostCode {
        val postCode = PostCode
            .find { PostCodeTable.id eq id }
            .limit(1)
            .notForUpdate()
            .firstOrNull()

        requireNotNull(postCode) { "The post code ($id) does not exist." }

        val uId = idOf(
            code = code ?: postCode.code,
            cityId = cityId ?: postCode.city.id.value,
        )
        require(
            uId == null || uId == postCode.id,
        ) { "A post code with this code and parent city does already exist." }

        code?.let { postCode.code = code }
        cityId?.let {
            val city = City
                .find { CityTable.id eq cityId }
                .limit(1)
                .forUpdate()
                .firstOrNull()

            requireNotNull(city) { "The city ($cityId) does not exist." }

            postCode.city = city
        }

        return postCode
    }

    fun delete(
        id: UUID,
    ) {
        val postCode = PostCode
            .find { PostCodeTable.id eq id }
            .limit(1)
            .forUpdate()
            .firstOrNull()

        requireNotNull(postCode) { "The post code ($id) does not exist." }

        postCode.delete()
    }
}
