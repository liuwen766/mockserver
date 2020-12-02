package org.mockserver.openapi;

import com.fasterxml.jackson.databind.ObjectWriter;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.mockserver.log.model.LogEntry;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.mock.Expectation;
import org.mockserver.model.HttpResponse;
import org.mockserver.openapi.examples.ExampleBuilder;
import org.mockserver.openapi.examples.JsonNodeExampleSerializer;
import org.mockserver.openapi.examples.models.StringExample;
import org.mockserver.serialization.ObjectMapperFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.JsonBody.json;
import static org.mockserver.model.OpenAPIDefinition.openAPI;
import static org.mockserver.openapi.OpenAPIParser.buildOpenAPI;
import static org.slf4j.event.Level.ERROR;

public class OpenAPIConverter {

    private static final ObjectWriter OBJECT_WRITER = ObjectMapperFactory.createObjectMapper(new JsonNodeExampleSerializer()).writerWithDefaultPrettyPrinter();
    private final MockServerLogger mockServerLogger;

    public OpenAPIConverter(MockServerLogger mockServerLogger) {
        this.mockServerLogger = mockServerLogger;
    }

    public List<Expectation> buildExpectations(String specUrlOrPayload, Map<String, String> operationsAndResponses) {
        OpenAPI openAPI = buildOpenAPI(specUrlOrPayload);
        return openAPI
            .getPaths()
            .values()
            .stream()
            .flatMap(pathItem ->
                pathItem
                    .readOperations()
                    .stream()
            )
            .filter(operation -> operationsAndResponses == null || operationsAndResponses.containsKey(operation.getOperationId()))
            .map(operation -> new Expectation(openAPI(specUrlOrPayload, operation.getOperationId()))
                .thenRespond(buildHttpResponse(
                    openAPI,
                    operation.getResponses(),
                    operationsAndResponses != null ? operationsAndResponses.get(operation.getOperationId()) : null
                ))
            )
            .collect(Collectors.toList());
    }

    private HttpResponse buildHttpResponse(OpenAPI openAPI, ApiResponses apiResponses, String apiResponseKey) {
        HttpResponse response = response();
        Optional
            .ofNullable(apiResponses)
            .flatMap(notNullApiResponses -> notNullApiResponses.entrySet().stream().filter(entry -> isBlank(apiResponseKey) | entry.getKey().equals(apiResponseKey)).findFirst())
            .ifPresent(apiResponse -> {
                if (!apiResponse.getKey().equalsIgnoreCase("default")) {
                    response.withStatusCode(Integer.parseInt(apiResponse.getKey()));
                }
                Optional
                    .ofNullable(apiResponse.getValue().getHeaders())
                    .map(Map::entrySet)
                    .map(Set::stream)
                    .ifPresent(stream -> stream
                        .forEach(entry -> {
                            Header value = entry.getValue();
                            Example example = findExample(value);
                            if (example != null) {
                                response.withHeader(entry.getKey(), String.valueOf(example.getValue()));
                            } else if (value.getSchema() != null) {
                                org.mockserver.openapi.examples.models.Example generatedExample = ExampleBuilder.fromSchema(value.getSchema(), openAPI.getComponents().getSchemas());
                                if (generatedExample instanceof StringExample) {
                                    response.withHeader(entry.getKey(), ((StringExample) generatedExample).getValue());
                                } else {
                                    response.withHeader(entry.getKey(), serialise(generatedExample));
                                }
                            }
                        })
                    );
                Optional
                    .ofNullable(apiResponse.getValue().getContent())
                    .flatMap(content -> content
                        .entrySet()
                        .stream()
                        .findFirst()
                    )
                    .ifPresent(contentType -> {
                        response.withHeader("content-type", contentType.getKey());
                        Optional
                            .ofNullable(contentType.getValue())
                            .ifPresent(mediaType -> {
                                Example example = findExample(mediaType);
                                if (example != null) {
                                    if (isJsonContentType(contentType.getKey())) {
                                        response.withBody(json(serialise(example.getValue())));
                                    } else {
                                        response.withBody(String.valueOf(example.getValue()));
                                    }
                                } else if (mediaType.getSchema() != null) {
                                    org.mockserver.openapi.examples.models.Example generatedExample = ExampleBuilder.fromSchema(mediaType.getSchema(), openAPI.getComponents().getSchemas());
                                    if (generatedExample instanceof StringExample) {
                                        response.withBody(((StringExample) generatedExample).getValue());
                                    } else {
                                        String serialise = serialise(ExampleBuilder.fromSchema(mediaType.getSchema(), openAPI.getComponents().getSchemas()));
                                        if (isJsonContentType(contentType.getKey())) {
                                            response.withBody(json(serialise));
                                        } else {
                                            response.withBody(serialise);
                                        }
                                    }
                                }
                            });
                    });
            });
        return response;
    }

    public static boolean isJsonContentType(String contentType) {
        return org.mockserver.model.MediaType.parse(contentType).isJson();
    }

    private Example findExample(Header value) {
        Example example = null;
        if (value.getExample() instanceof Example) {
            example = (Example) value.getExample();
        } else if (value.getExamples() != null && !value.getExamples().isEmpty()) {
            example = value.getExamples().values().stream().findFirst().orElse(null);
        }
        return example;
    }

    private Example findExample(MediaType mediaType) {
        Example example = null;
        if (mediaType.getExample() instanceof Example) {
            example = (Example) mediaType.getExample();
        } else if (mediaType.getExamples() != null && !mediaType.getExamples().isEmpty()) {
            example = mediaType.getExamples().values().stream().findFirst().orElse(null);
        }
        return example;
    }

    private String serialise(Object example) {
        try {
            return OBJECT_WRITER.writeValueAsString(example);
        } catch (Throwable throwable) {
            mockServerLogger.logEvent(
                new LogEntry()
                    .setLogLevel(ERROR)
                    .setMessageFormat("exception while serialising " + example.getClass() + " {}")
                    .setArguments(example)
                    .setThrowable(throwable)
            );
            return "";
        }
    }
}
