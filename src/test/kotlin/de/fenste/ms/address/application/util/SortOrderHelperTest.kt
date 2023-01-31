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

package de.fenste.ms.address.application.util

import de.fenste.ms.address.infrastructure.tables.CountryTable
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.SortOrder
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith

@SpringBootTest
@ActiveProfiles("sample")
class SortOrderHelperTest {

    @Test
    fun `test parseSortOrder using CountryTable`() {
        val sort = "name,asc;localized_name,desc"
        val expected = arrayOf<Pair<Expression<*>, SortOrder>>(
            CountryTable.name to SortOrder.ASC,
            CountryTable.localizedName to SortOrder.DESC,
        )
        val actual = sort.parseSortOrder(CountryTable::valueOf)

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test parseSortOrder with implicit order using CountryTable`() {
        val sort = "name;alpha2"
        val expected = arrayOf<Pair<Expression<*>, SortOrder>>(
            CountryTable.name to SortOrder.ASC,
            CountryTable.alpha2 to SortOrder.ASC,
        )
        val actual = sort.parseSortOrder(CountryTable::valueOf)

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test parseSortOrder with explicit and implicit order using CountryTable`() {
        val sort = "alpha2,desc;name;alpha3,asc"
        val expected = arrayOf<Pair<Expression<*>, SortOrder>>(
            CountryTable.alpha2 to SortOrder.DESC,
            CountryTable.name to SortOrder.ASC,
            CountryTable.alpha3 to SortOrder.ASC,
        )
        val actual = sort.parseSortOrder(CountryTable::valueOf)

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test parseSortOrder with explicit and implicit order and whitespaces using CountryTable`() {
        val sort = "  alpha2 ,   desc  ;  name    ;    alpha3 ,asc"
        val expected = arrayOf<Pair<Expression<*>, SortOrder>>(
            CountryTable.alpha2 to SortOrder.DESC,
            CountryTable.name to SortOrder.ASC,
            CountryTable.alpha3 to SortOrder.ASC,
        )
        val actual = sort.parseSortOrder(CountryTable::valueOf)

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test parseSortOrder with empty string`() {
        val sort = ""
        val expected = emptyArray<Pair<Expression<*>, SortOrder>>()
        val actual = sort.parseSortOrder(CountryTable::valueOf)

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test parseSortOrder with null string`() {
        val sort: String? = null
        val expected = emptyArray<Pair<Expression<*>, SortOrder>>()
        val actual = sort.parseSortOrder(CountryTable::valueOf)

        assertContentEquals(expected, actual)
    }

    @Test
    fun `test parseSortOrder not existing SortOrder`() {
        val sort = "name,fail"
        assertFailsWith<IllegalArgumentException> {
            sort.parseSortOrder(CountryTable::valueOf)
        }
    }

    @Test
    fun `test parseSortOrder not existing Field`() {
        val sort = "fail,asc"
        assertFailsWith<IllegalArgumentException> {
            sort.parseSortOrder(CountryTable::valueOf)
        }
    }
}
