package org.mockserver.serialization.serializers.body;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mockserver.model.JsonSchemaBody;
import org.mockserver.model.ParameterStyle;
import org.mockserver.serialization.ObjectMapperFactory;
import org.mockserver.serialization.model.JsonSchemaBodyDTO;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class JsonSchemaBodyDTOSerializerTest {

    @Test
    public void shouldSerializeJsonBodyDTO() throws JsonProcessingException {
        assertThat(ObjectMapperFactory.createObjectMapper().writeValueAsString(new JsonSchemaBodyDTO(new JsonSchemaBody("{\"type\": \"object\", \"properties\": {\"id\": {\"type\": \"integer\"}}, \"required\": [\"id\"]}"), false)),
            is("{\"type\":\"JSON_SCHEMA\",\"jsonSchema\":{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\"}},\"required\":[\"id\"]}}"));
    }

    @Test
    public void shouldSerializeJsonBodyDTOWithParameterStyle() throws JsonProcessingException {
        assertThat(ObjectMapperFactory.createObjectMapper().writeValueAsString(new JsonSchemaBodyDTO(
                new JsonSchemaBody("{\"type\": \"object\", \"properties\": {\"id\": {\"type\": \"integer\"}}, \"required\": [\"id\"]}")
                    .withParameterStyles(ImmutableMap.of("pipeDelimitedParameter", ParameterStyle.PIPE_DELIMITED)),
                false
            )),
            is("{\"type\":\"JSON_SCHEMA\",\"jsonSchema\":{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\"}},\"required\":[\"id\"]},\"parameterStyles\":{\"pipeDelimitedParameter\":\"PIPE_DELIMITED\"}}"));
    }

    @Test
    public void shouldSerializeJsonBodyDTOWithNot() throws JsonProcessingException {
        assertThat(ObjectMapperFactory.createObjectMapper().writeValueAsString(new JsonSchemaBodyDTO(new JsonSchemaBody("{\"type\": \"object\", \"properties\": {\"id\": {\"type\": \"integer\"}}, \"required\": [\"id\"]}"), true)),
            is("{\"not\":true,\"type\":\"JSON_SCHEMA\",\"jsonSchema\":{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\"}},\"required\":[\"id\"]}}"));
    }

    @Test
    public void shouldSerializeJsonBodyDTOWithOptional() throws JsonProcessingException {
        assertThat(ObjectMapperFactory.createObjectMapper().writeValueAsString(new JsonSchemaBodyDTO(new JsonSchemaBody("{\"type\": \"object\", \"properties\": {\"id\": {\"type\": \"integer\"}}, \"required\": [\"id\"]}")).withOptional(true)),
            is("{\"optional\":true,\"type\":\"JSON_SCHEMA\",\"jsonSchema\":{\"type\":\"object\",\"properties\":{\"id\":{\"type\":\"integer\"}},\"required\":[\"id\"]}}"));
    }

}