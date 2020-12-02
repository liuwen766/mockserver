package org.mockserver.serialization.java;

import org.junit.Test;
import org.mockserver.model.Cookie;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockserver.character.Character.NEW_LINE;
import static org.mockserver.model.NottableOptionalString.optional;
import static org.mockserver.model.NottableString.not;

public class CookieToJavaSerializerTest {

    @Test
    public void shouldSerializeCookie() {
        assertEquals(NEW_LINE +
                        "        new Cookie(\"requestCookieNameOne\", \"requestCookieValueOne\")",
                new CookieToJavaSerializer().serialize(1, new Cookie("requestCookieNameOne", "requestCookieValueOne"))
        );
    }

    @Test
    public void shouldSerializeMultipleCookies() {
        assertEquals(NEW_LINE +
                        "        new Cookie(\"requestCookieNameOne\", \"requestCookieValueOne\")," +
                        NEW_LINE +
                        "        new Cookie(\"requestCookieNameTwo\", \"requestCookieValueTwo\")",
                new CookieToJavaSerializer().serializeAsJava(1, new Cookie("requestCookieNameOne", "requestCookieValueOne"), new Cookie("requestCookieNameTwo", "requestCookieValueTwo"))
        );
    }

    @Test
    public void shouldSerializeListOfCookies() {
        assertEquals(NEW_LINE +
                        "        new Cookie(\"requestCookieNameOne\", \"requestCookieValueOne\")," +
                        NEW_LINE +
                        "        new Cookie(\"requestCookieNameTwo\", \"requestCookieValueTwo\")",
                new CookieToJavaSerializer().serializeAsJava(1, Arrays.asList(
                        new Cookie("requestCookieNameOne", "requestCookieValueOne"),
                        new Cookie("requestCookieNameTwo", "requestCookieValueTwo")
                ))
        );
    }

    @Test
    public void shouldSerializeListOfNottedAndOptionalCookies() {
        assertEquals(NEW_LINE +
                        "        new Cookie(not(\"requestCookieNameOne\"), \"requestCookieValueOne\")," +
                        NEW_LINE +
                        "        new Cookie(optional(\"requestCookieNameTwo\"), \"requestCookieValueTwo\")",
                new CookieToJavaSerializer().serializeAsJava(1, Arrays.asList(
                        new Cookie(not("requestCookieNameOne"), "requestCookieValueOne"),
                        new Cookie(optional("requestCookieNameTwo"), "requestCookieValueTwo")
                ))
        );
    }

}