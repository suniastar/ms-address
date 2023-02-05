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

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.hateoas.Link
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test
import kotlin.test.assertEquals

@SpringBootTest
@ActiveProfiles("sample")
class PageHelperTest {

    @Test
    fun `test generatePageLinks with null`() {
        val expected = setOf(
            Link.of("###{?page,size,sort}").withSelfRel(),
        )
        val actual = PageHelper.generatePageLinks(
            "###",
            null,
            null,
            null,
            null,
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `test generatePageLinks with data`() {
        val expected = setOf(
            Link.of("###{?page,size,sort}").withSelfRel(),
            Link.of("###?page=0&size=10&sort=name,asc").withRel("first"),
            Link.of("###?page=4&size=10&sort=name,asc").withRel("prev"),
            Link.of("###?page=6&size=10&sort=name,asc").withRel("next"),
            Link.of("###?page=9&size=10&sort=name,asc").withRel("last"),
        )
        val actual = PageHelper.generatePageLinks(
            "###",
            10,
            5,
            10,
            "name,asc",
        )

        assertEquals(expected, actual)
    }
}
