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

package de.fenste.ms.address.config

import de.fenste.ms.address.domain.exception.DuplicateException
import de.fenste.ms.address.domain.exception.InvalidArgumentException
import de.fenste.ms.address.domain.exception.NotFoundException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class ExceptionHandlerConfig : ResponseEntityExceptionHandler() {

    @ExceptionHandler(DuplicateException::class)
    fun handleDuplicateException(e: RuntimeException, request: WebRequest): ResponseEntity<Any>? {
        return handleExceptionInternal(
            e,
            e.message,
            HttpHeaders(),
            HttpStatus.CONFLICT,
            request,
        )
    }

    @ExceptionHandler(InvalidArgumentException::class)
    fun handleInvalidArgumentException(e: RuntimeException, request: WebRequest): ResponseEntity<Any>? {
        return handleExceptionInternal(
            e,
            e.message,
            HttpHeaders(),
            HttpStatus.BAD_REQUEST,
            request,
        )
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(e: RuntimeException, request: WebRequest): ResponseEntity<Any>? {
        return handleExceptionInternal(
            e,
            e.message,
            HttpHeaders(),
            HttpStatus.NOT_FOUND,
            request,
        )
    }
}
