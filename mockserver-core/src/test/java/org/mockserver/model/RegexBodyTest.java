package org.mockserver.model;

import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockserver.model.RegexBody.regex;

/**
 * @author jamesdbloom
 */
public class RegexBodyTest {

    @Test
    public void shouldReturnValuesSetInConstructor() {
        // when
        RegexBody regexBody = new RegexBody("some_body");

        // then
        assertThat(regexBody.getValue(), is("some_body"));
        assertThat(regexBody.getType(), is(Body.Type.REGEX));
        assertThat(regexBody.getContentType(), nullValue());
        assertThat(regexBody.getCharset(StandardCharsets.UTF_8), is(StandardCharsets.UTF_8));
    }

    @Test
    public void shouldReturnValuesFromStaticBuilder() {
        // when
        RegexBody regexBody = regex("some_body");

        // then
        assertThat(regexBody.getValue(), is("some_body"));
        assertThat(regexBody.getType(), is(Body.Type.REGEX));
        assertThat(regexBody.getContentType(), nullValue());
        assertThat(regexBody.getCharset(StandardCharsets.UTF_8), is(StandardCharsets.UTF_8));
    }

}
