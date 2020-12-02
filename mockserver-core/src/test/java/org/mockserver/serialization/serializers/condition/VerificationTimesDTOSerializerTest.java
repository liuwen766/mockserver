package org.mockserver.serialization.serializers.condition;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;
import org.mockserver.serialization.ObjectMapperFactory;
import org.mockserver.serialization.model.VerificationTimesDTO;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockserver.character.Character.NEW_LINE;
import static org.mockserver.verify.VerificationTimes.*;

/**
 * @author jamesdbloom
 */
public class VerificationTimesDTOSerializerTest {

    @Test
    public void shouldSerializeBetween() throws JsonProcessingException {
        assertThat(ObjectMapperFactory
                .createObjectMapper(true)
                .writeValueAsString(
                    new VerificationTimesDTO(
                        between(1, 2)
                    )
                ),
            is("{" + NEW_LINE +
                "  \"atLeast\" : 1," + NEW_LINE +
                "  \"atMost\" : 2" + NEW_LINE +
                "}"));
    }

    @Test
    public void shouldSerializeOnce() throws JsonProcessingException {
        assertThat(ObjectMapperFactory
                .createObjectMapper(true)
                .writeValueAsString(
                    new VerificationTimesDTO(
                        once()
                    )
                ),
            is("{" + NEW_LINE +
                "  \"atLeast\" : 1," + NEW_LINE +
                "  \"atMost\" : 1" + NEW_LINE +
                "}"));
    }

    @Test
    public void shouldSerializeExact() throws JsonProcessingException {
        assertThat(ObjectMapperFactory
                .createObjectMapper(true)
                .writeValueAsString(
                    new VerificationTimesDTO(
                        exactly(2)
                    )
                ),
            is("{" + NEW_LINE +
                "  \"atLeast\" : 2," + NEW_LINE +
                "  \"atMost\" : 2" + NEW_LINE +
                "}"));
    }

    @Test
    public void shouldSerializeAtLeast() throws JsonProcessingException {
        assertThat(ObjectMapperFactory
                .createObjectMapper(true)
                .writeValueAsString(
                    new VerificationTimesDTO(
                        atLeast(2)
                    )
                ),
            is("{" + NEW_LINE +
                "  \"atLeast\" : 2" + NEW_LINE +
                "}"));
    }

    @Test
    public void shouldSerializeAtMost() throws JsonProcessingException {
        assertThat(ObjectMapperFactory
                .createObjectMapper(true)
                .writeValueAsString(
                    new VerificationTimesDTO(
                        atMost(2)
                    )
                ),
            is("{" + NEW_LINE +
                "  \"atMost\" : 2" + NEW_LINE +
                "}"));
    }
}