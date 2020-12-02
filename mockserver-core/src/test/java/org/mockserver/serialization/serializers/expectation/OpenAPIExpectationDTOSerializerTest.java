package org.mockserver.serialization.serializers.expectation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.mockserver.file.FileReader;
import org.mockserver.serialization.ObjectMapperFactory;
import org.mockserver.serialization.model.OpenAPIExpectationDTO;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockserver.character.Character.NEW_LINE;
import static org.mockserver.mock.OpenAPIExpectation.openAPIExpectation;

public class OpenAPIExpectationDTOSerializerTest {

    private final ObjectWriter objectMapper = ObjectMapperFactory.createObjectMapper(true);

    @Test
    public void shouldReturnJsonWithNoFieldsSet() throws JsonProcessingException {
        assertThat(objectMapper.writeValueAsString(new OpenAPIExpectationDTO(openAPIExpectation())), is("{ }"));
    }

    @Test
    public void shouldReturnJsonWithOpenAPIClasspathLocationAndOperationId() throws JsonProcessingException {
        assertThat(objectMapper.writeValueAsString(new OpenAPIExpectationDTO(
            openAPIExpectation()
                .withSpecUrlOrPayload("org/mockserver/mock/openapi_simple_example.json")
                .withOperationsAndResponses(ImmutableMap.of(
                    "listPets", "200",
                    "createPets", "201"
                ))
        )), is("" +
            "{" + NEW_LINE +
            "  \"specUrlOrPayload\" : \"org/mockserver/mock/openapi_simple_example.json\"," + NEW_LINE +
            "  \"operationsAndResponses\" : {" + NEW_LINE +
            "    \"listPets\" : \"200\"," + NEW_LINE +
            "    \"createPets\" : \"201\"" + NEW_LINE +
            "  }" + NEW_LINE +
            "}"
        ));
    }

    @Test
    public void shouldReturnJsonWithOpenAPIClasspathLocation() throws JsonProcessingException {
        assertThat(objectMapper.writeValueAsString(new OpenAPIExpectationDTO(
            openAPIExpectation()
                .withSpecUrlOrPayload("org/mockserver/mock/openapi_simple_example.json")
        )), is("" +
            "{" + NEW_LINE +
            "  \"specUrlOrPayload\" : \"org/mockserver/mock/openapi_simple_example.json\"" + NEW_LINE +
            "}"
        ));
    }

    @Test
    public void shouldReturnJsonWithOpenAPIUrlAndOperationId() throws JsonProcessingException {
        assertThat(objectMapper.writeValueAsString(new OpenAPIExpectationDTO(
            openAPIExpectation()
                .withSpecUrlOrPayload(FileReader.getURL("org/mockserver/mock/openapi_simple_example.json").toString())
                .withOperationsAndResponses(ImmutableMap.of(
                    "listPets", "200",
                    "createPets", "201"
                ))
        )), is("" +
            "{" + NEW_LINE +
            "  \"specUrlOrPayload\" : \"" + FileReader.getURL("org/mockserver/mock/openapi_simple_example.json").toString() + "\"," + NEW_LINE +
            "  \"operationsAndResponses\" : {" + NEW_LINE +
            "    \"listPets\" : \"200\"," + NEW_LINE +
            "    \"createPets\" : \"201\"" + NEW_LINE +
            "  }" + NEW_LINE +
            "}"
        ));
    }

    @Test
    public void shouldReturnJsonWithOpenAPIUrl() throws JsonProcessingException {
        assertThat(objectMapper.writeValueAsString(new OpenAPIExpectationDTO(
            openAPIExpectation()
                .withSpecUrlOrPayload(FileReader.getURL("org/mockserver/mock/openapi_simple_example.json").toString())
        )), is("" +
            "{" + NEW_LINE +
            "  \"specUrlOrPayload\" : \"" + FileReader.getURL("org/mockserver/mock/openapi_simple_example.json").toString() + "\"" + NEW_LINE +
            "}"
        ));
    }

    @Test
    public void shouldReturnJsonWithOpenAPISpecAndOperationId() throws JsonProcessingException {
        assertThat(objectMapper.writeValueAsString(new OpenAPIExpectationDTO(
            openAPIExpectation()
                .withSpecUrlOrPayload(FileReader.readFileFromClassPathOrPath("org/mockserver/mock/openapi_simple_example.json"))
                .withOperationsAndResponses(ImmutableMap.of(
                    "listPets", "200",
                    "createPets", "201"
                ))
        )), is("" +
            "{" + NEW_LINE +
            "  \"specUrlOrPayload\" : " + ObjectMapperFactory.createObjectMapper().readTree(FileReader.readFileFromClassPathOrPath("org/mockserver/mock/openapi_simple_example.json")).toPrettyString().replaceAll("\\R", "\n  ") + "," + NEW_LINE +
            "  \"operationsAndResponses\" : {" + NEW_LINE +
            "    \"listPets\" : \"200\"," + NEW_LINE +
            "    \"createPets\" : \"201\"" + NEW_LINE +
            "  }" + NEW_LINE +
            "}"
        ));
    }

    @Test
    public void shouldReturnJsonWithOpenAPISpec() throws JsonProcessingException {
        assertThat(objectMapper.writeValueAsString(new OpenAPIExpectationDTO(
            openAPIExpectation()
                .withSpecUrlOrPayload(FileReader.readFileFromClassPathOrPath("org/mockserver/mock/openapi_simple_example.json"))
        )), is("" +
            "{" + NEW_LINE +
            "  \"specUrlOrPayload\" : " + ObjectMapperFactory.createObjectMapper().readTree(FileReader.readFileFromClassPathOrPath("org/mockserver/mock/openapi_simple_example.json")).toPrettyString().replaceAll("\\R", "\n  ") + NEW_LINE +
            "}"
        ));
    }

}