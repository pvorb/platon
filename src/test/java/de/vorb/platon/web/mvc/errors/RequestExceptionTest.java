/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.vorb.platon.web.mvc.errors;

import org.assertj.core.api.ThrowableAssert;
import org.junit.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class RequestExceptionTest {

    @Test
    public void acceptOnlyStatusCodesGreaterThan400() {

        assertThatIllegalArgumentException().isThrownBy(creatingResultException(100));
        assertThatIllegalArgumentException().isThrownBy(creatingResultException(200));
        assertThatIllegalArgumentException().isThrownBy(creatingResultException(301));
        assertThatIllegalArgumentException().isThrownBy(creatingResultException(308));

        assertThatCode(creatingResultException(400)).doesNotThrowAnyException();
        assertThatCode(creatingResultException(500)).doesNotThrowAnyException();
        assertThatCode(creatingResultException(511)).doesNotThrowAnyException();
        assertThatCode(creatingResultException(999)).doesNotThrowAnyException();
    }

    @Test
    public void returnsOriginalStatus() {
        assertThat(RequestException.withStatus(400).build().getStatus()).isEqualTo(400);
    }

    @Test
    public void badRequest() {
        assertThat(RequestException.badRequest().build().getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void unauthorized() {
        assertThat(RequestException.unauthorized().build().getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void forbidden() {
        assertThat(RequestException.forbidden().build().getHttpStatus()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void notFound() {
        assertThat(RequestException.notFound().build().getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void internalServerError() {
        assertThat(RequestException.internalServerError().build().getHttpStatus())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private static ThrowableAssert.ThrowingCallable creatingResultException(int status) {
        return () -> RequestException.withStatus(status).build();
    }
}