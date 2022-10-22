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

package de.fenste.ms.address.config

import graphql.language.StringValue
import graphql.schema.Coercing
import graphql.schema.CoercingSerializeException
import graphql.schema.GraphQLScalarType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.execution.RuntimeWiringConfigurer
import java.util.UUID

@Configuration
class CustomScalarConfig {

    @Bean
    fun uuidScalar(): RuntimeWiringConfigurer = GraphQLScalarType
        .newScalar()
        .name("UUID")
        .description("A field whose value is a generic Universally Unique Identifier")
        .coercing(
            object : Coercing<UUID, String> {
                override fun serialize(dataFetcherResult: Any): String =
                    if (dataFetcherResult is UUID) {
                        dataFetcherResult.toString()
                    } else {
                        throw CoercingSerializeException("Expected an object of type UUID.")
                    }

                override fun parseValue(input: Any): UUID =
                    if (input is String) {
                        try {
                            UUID.fromString(input)
                        } catch (e: IllegalArgumentException) {
                            throw CoercingSerializeException("\"$input\" is not a valid UUID.", e)
                        }
                    } else {
                        throw CoercingSerializeException("Expected a string.")
                    }

                override fun parseLiteral(input: Any): UUID =
                    if (input is StringValue) {
                        try {
                            UUID.fromString(input.value)
                        } catch (e: IllegalArgumentException) {
                            throw CoercingSerializeException("\"$input\" is not a valid UUID.", e)
                        }
                    } else {
                        throw CoercingSerializeException("Expected a string.")
                    }
            },
        )
        .build()
        .let { type -> RuntimeWiringConfigurer { builder -> builder.scalar(type) } }
}
