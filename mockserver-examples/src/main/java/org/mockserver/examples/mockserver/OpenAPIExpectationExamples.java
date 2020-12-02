package org.mockserver.examples.mockserver;

import org.mockserver.client.MockServerClient;
import org.mockserver.file.FileReader;

import java.util.HashMap;
import java.util.Map;

import static org.mockserver.mock.OpenAPIExpectation.openAPIExpectation;

/**
 * @author jamesdbloom
 */
public class OpenAPIExpectationExamples {

    public void createOpenAPIExpectationLoadedByHttpUrl() {
        new MockServerClient("localhost", 1080)
            .upsert(
                openAPIExpectation(
                    "https://raw.githubusercontent.com/mock-server/mockserver/master/mockserver-integration-testing/src/main/resources/org/mockserver/mock/openapi_petstore_example.json"
                )
            );
    }

    public void createOpenAPIExpectationFilteringOperations() {
        Map<String, String> operationsAndResponses = new HashMap<>();
        operationsAndResponses.put("showPetById", "200");
        operationsAndResponses.put("createPets", "500");
        new MockServerClient("localhost", 1080)
            .upsert(
                openAPIExpectation(
                    "https://raw.githubusercontent.com/mock-server/mockserver/master/mockserver-integration-testing/src/main/resources/org/mockserver/mock/openapi_petstore_example.json",
                    operationsAndResponses
                )
            );
    }

    public void createOpenAPIExpectationLoadedByFileUrl() {
        new MockServerClient("localhost", 1080)
            .upsert(
                openAPIExpectation(
                    "file:/Users/jamesbloom/git/mockserver/mockserver/mockserver-core/target/test-classes/org/mockserver/mock/openapi_petstore_example.json"
                )
            );
    }

    public void createOpenAPIExpectationLoadedByClasspathLocation() {
        new MockServerClient("localhost", 1080)
            .upsert(
                openAPIExpectation("org/mockserver/mock/openapi_petstore_example.json")
            );
    }

    public void createOpenAPIExpectationLoadedByJsonLiteral() {
        new MockServerClient("localhost", 1080)
            .upsert(
                openAPIExpectation(FileReader.readFileFromClassPathOrPath("/Users/jamesbloom/git/mockserver/mockserver/mockserver-core/target/test-classes/org/mockserver/mock/openapi_petstore_example.json"))
            );
    }

    public void createOpenAPIExpectationLoadedByYamlLiteral() {
        new MockServerClient("localhost", 1080)
            .upsert(
                openAPIExpectation("---\n" +
                    "openapi: 3.0.0\n" +
                    "info:\n" +
                    "  version: 1.0.0\n" +
                    "  title: Swagger Petstore\n" +
                    "  license:\n" +
                    "    name: MIT\n" +
                    "servers:\n" +
                    "  - url: http://petstore.swagger.io/v1\n" +
                    "paths:\n" +
                    "  /pets:\n" +
                    "    get:\n" +
                    "      summary: List all pets\n" +
                    "      operationId: listPets\n" +
                    "      tags:\n" +
                    "        - pets\n" +
                    "      parameters:\n" +
                    "        - name: limit\n" +
                    "          in: query\n" +
                    "          description: How many items to return at one time (max 100)\n" +
                    "          required: false\n" +
                    "          schema:\n" +
                    "            type: integer\n" +
                    "            format: int32\n" +
                    "      responses:\n" +
                    "        '200':\n" +
                    "          description: A paged array of pets\n" +
                    "          headers:\n" +
                    "            x-next:\n" +
                    "              description: A link to the next page of responses\n" +
                    "              schema:\n" +
                    "                type: string\n" +
                    "              examples:\n" +
                    "                two:\n" +
                    "                  value: \"/pets?query=752cd724e0d7&page=2\"\n" +
                    "                end:\n" +
                    "                  value: \"\"\n" +
                    "          content:\n" +
                    "            application/json:\n" +
                    "              schema:\n" +
                    "                $ref: '#/components/schemas/Pets'\n" +
                    "        '500':\n" +
                    "          description: unexpected error\n" +
                    "          headers:\n" +
                    "            x-code:\n" +
                    "              description: The error code\n" +
                    "              schema:\n" +
                    "                type: integer\n" +
                    "                format: int32\n" +
                    "                example: 90\n" +
                    "          content:\n" +
                    "            application/json:\n" +
                    "              schema:\n" +
                    "                $ref: '#/components/schemas/Error'\n" +
                    "        default:\n" +
                    "          description: unexpected error\n" +
                    "          content:\n" +
                    "            application/json:\n" +
                    "              schema:\n" +
                    "                $ref: '#/components/schemas/Error'\n" +
                    "    post:\n" +
                    "      summary: Create a pet\n" +
                    "      operationId: createPets\n" +
                    "      tags:\n" +
                    "        - pets\n" +
                    "      requestBody:\n" +
                    "        description: a pet\n" +
                    "        required: true\n" +
                    "        content:\n" +
                    "          application/json:\n" +
                    "            schema:\n" +
                    "              $ref: '#/components/schemas/Pet'\n" +
                    "          '*/*':\n" +
                    "            schema:\n" +
                    "              $ref: '#/components/schemas/Pet'\n" +
                    "      responses:\n" +
                    "        '201':\n" +
                    "          description: Null response\n" +
                    "        '400':\n" +
                    "          description: unexpected error\n" +
                    "          content:\n" +
                    "            application/json:\n" +
                    "              schema:\n" +
                    "                $ref: '#/components/schemas/Error'\n" +
                    "        '500':\n" +
                    "          description: unexpected error\n" +
                    "          content:\n" +
                    "            application/json:\n" +
                    "              schema:\n" +
                    "                $ref: '#/components/schemas/Error'\n" +
                    "        default:\n" +
                    "          description: unexpected error\n" +
                    "          content:\n" +
                    "            application/json:\n" +
                    "              schema:\n" +
                    "                $ref: '#/components/schemas/Error'\n" +
                    "  /pets/{petId}:\n" +
                    "    get:\n" +
                    "      summary: Info for a specific pet\n" +
                    "      operationId: showPetById\n" +
                    "      tags:\n" +
                    "        - pets\n" +
                    "      parameters:\n" +
                    "        - name: petId\n" +
                    "          in: path\n" +
                    "          required: true\n" +
                    "          description: The id of the pet to retrieve\n" +
                    "          schema:\n" +
                    "            type: string\n" +
                    "        - in: header\n" +
                    "          name: X-Request-ID\n" +
                    "          schema:\n" +
                    "            type: string\n" +
                    "            format: uuid\n" +
                    "          required: true\n" +
                    "      responses:\n" +
                    "        '200':\n" +
                    "          description: Expected response to a valid request\n" +
                    "          content:\n" +
                    "            application/json:\n" +
                    "              schema:\n" +
                    "                $ref: '#/components/schemas/Pet'\n" +
                    "              examples:\n" +
                    "                Crumble:\n" +
                    "                  value:\n" +
                    "                    id: 2\n" +
                    "                    name: Crumble\n" +
                    "                    tag: dog\n" +
                    "                Boots:\n" +
                    "                  value:\n" +
                    "                    id: 3\n" +
                    "                    name: Boots\n" +
                    "                    tag: cat\n" +
                    "        '500':\n" +
                    "          description: unexpected error\n" +
                    "          content:\n" +
                    "            application/json:\n" +
                    "              schema:\n" +
                    "                $ref: '#/components/schemas/Error'\n" +
                    "        default:\n" +
                    "          description: unexpected error\n" +
                    "          content:\n" +
                    "            application/json:\n" +
                    "              schema:\n" +
                    "                $ref: '#/components/schemas/Error'\n" +
                    "components:\n" +
                    "  schemas:\n" +
                    "    Pet:\n" +
                    "      type: object\n" +
                    "      required:\n" +
                    "        - id\n" +
                    "        - name\n" +
                    "      properties:\n" +
                    "        id:\n" +
                    "          type: integer\n" +
                    "          format: int64\n" +
                    "        name:\n" +
                    "          type: string\n" +
                    "        tag:\n" +
                    "          type: string\n" +
                    "      example:\n" +
                    "        id: 1\n" +
                    "        name: Scruffles\n" +
                    "        tag: dog\n" +
                    "    Pets:\n" +
                    "      type: array\n" +
                    "      items:\n" +
                    "        $ref: '#/components/schemas/Pet'\n" +
                    "    Error:\n" +
                    "      type: object\n" +
                    "      required:\n" +
                    "        - code\n" +
                    "        - message\n" +
                    "      properties:\n" +
                    "        code:\n" +
                    "          type: integer\n" +
                    "          format: int32\n" +
                    "        message:\n" +
                    "          type: string\n")
            );
    }

}
