package org.mockserver.validator.jsonschema;

import org.junit.Test;
import org.mockserver.logging.MockServerLogger;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockserver.character.Character.NEW_LINE;
import static org.mockserver.validator.jsonschema.JsonSchemaHttpRequestValidator.jsonSchemaHttpRequestValidator;
import static org.mockserver.validator.jsonschema.JsonSchemaValidator.OPEN_API_SPECIFICATION_URL;

/**
 * @author jamesdbloom
 */
public class JsonSchemaVerificationValidatorIntegrationTest {

    private final JsonSchemaValidator jsonSchemaValidator = jsonSchemaHttpRequestValidator(new MockServerLogger());

    @Test
    public void shouldValidateValidCompleteRequestWithStringBody() {
        // when
        assertThat(jsonSchemaValidator.isValid("{" + NEW_LINE +
            "    \"method\" : \"someMethod\"," + NEW_LINE +
            "    \"path\" : \"somePath\"," + NEW_LINE +
            "    \"queryStringParameters\" : [ {" + NEW_LINE +
            "      \"name\" : \"queryStringParameterNameOne\"," + NEW_LINE +
            "      \"values\" : [ \"queryStringParameterValueOne_One\", \"queryStringParameterValueOne_Two\" ]" + NEW_LINE +
            "    }, {" + NEW_LINE +
            "      \"name\" : \"queryStringParameterNameTwo\"," + NEW_LINE +
            "      \"values\" : [ \"queryStringParameterValueTwo_One\" ]" + NEW_LINE +
            "    } ]," + NEW_LINE +
            "    \"body\" : {" + NEW_LINE +
            "      \"type\" : \"STRING\"," + NEW_LINE +
            "      \"string\" : \"someBody\"" + NEW_LINE +
            "    }," + NEW_LINE +
            "    \"cookies\" : [ {" + NEW_LINE +
            "      \"name\" : \"someCookieName\"," + NEW_LINE +
            "      \"value\" : \"someCookieValue\"" + NEW_LINE +
            "    } ]," + NEW_LINE +
            "    \"headers\" : [ {" + NEW_LINE +
            "      \"name\" : \"someHeaderName\"," + NEW_LINE +
            "      \"values\" : [ \"someHeaderValue\" ]" + NEW_LINE +
            "    } ]" + NEW_LINE +
            "  }"), is(""));
    }

    @Test
    public void shouldValidateValidShortHandJsonObjectBodyType() {
        // when
        assertThat(jsonSchemaValidator.isValid("{" + NEW_LINE +
                "    \"body\" : {\"foo\":\"bar\"}" + NEW_LINE +
                "  }"),
            is(""));
    }

    @Test
    public void shouldValidateValidShortHandJsonArrayBodyType() {
        // when
        assertThat(jsonSchemaValidator.isValid("{" + NEW_LINE +
                "    \"body\" : [{\"foo\":\"bar\"},{\"bar\":\"foo\"}]" + NEW_LINE +
                "  }"),
            is(""));
    }

    @Test
    public void shouldValidateInvalidBodyType() {
        // when
        assertThat(jsonSchemaValidator.isValid("{" + NEW_LINE +
                "    \"body\" : 1" + NEW_LINE +
                "  }"),
            is(
                "14 errors:" + NEW_LINE +
                    " - field: \"/body\" for schema: \"body\" has error: \" a plain string, JSON object or one of the following example bodies must be specified " + NEW_LINE +
                    "   {" + NEW_LINE +
                    "     \"not\": false," + NEW_LINE +
                    "     \"type\": \"BINARY\"," + NEW_LINE +
                    "     \"base64Bytes\": \"\"," + NEW_LINE +
                    "     \"contentType\": \"\"" + NEW_LINE +
                    "   }, " + NEW_LINE +
                    "   {" + NEW_LINE +
                    "     \"not\": false," + NEW_LINE +
                    "     \"type\": \"JSON\"," + NEW_LINE +
                    "     \"json\": \"\"," + NEW_LINE +
                    "     \"contentType\": \"\"," + NEW_LINE +
                    "     \"matchType\": \"ONLY_MATCHING_FIELDS\"" + NEW_LINE +
                    "   }," + NEW_LINE +
                    "   {" + NEW_LINE +
                    "     \"not\": false," + NEW_LINE +
                    "     \"type\": \"JSON_SCHEMA\"," + NEW_LINE +
                    "     \"jsonSchema\": \"\"" + NEW_LINE +
                    "   }," + NEW_LINE +
                    "   {" + NEW_LINE +
                    "     \"not\": false," + NEW_LINE +
                    "     \"type\": \"JSON_PATH\"," + NEW_LINE +
                    "     \"jsonPath\": \"\"" + NEW_LINE +
                    "   }," + NEW_LINE +
                    "   {" + NEW_LINE +
                    "     \"not\": false," + NEW_LINE +
                    "     \"type\": \"PARAMETERS\"," + NEW_LINE +
                    "     \"parameters\": {\"name\": \"value\"}" + NEW_LINE +
                    "   }," + NEW_LINE +
                    "   {" + NEW_LINE +
                    "     \"not\": false," + NEW_LINE +
                    "     \"type\": \"REGEX\"," + NEW_LINE +
                    "     \"regex\": \"\"" + NEW_LINE +
                    "   }," + NEW_LINE +
                    "   {" + NEW_LINE +
                    "     \"not\": false," + NEW_LINE +
                    "     \"type\": \"STRING\"," + NEW_LINE +
                    "     \"string\": \"\"" + NEW_LINE +
                    "   }," + NEW_LINE +
                    "   {" + NEW_LINE +
                    "     \"not\": false," + NEW_LINE +
                    "     \"type\": \"XML\"," + NEW_LINE +
                    "     \"xml\": \"\"," + NEW_LINE +
                    "     \"contentType\": \"\"" + NEW_LINE +
                    "   }," + NEW_LINE +
                    "   {" + NEW_LINE +
                    "     \"not\": false," + NEW_LINE +
                    "     \"type\": \"XML_SCHEMA\"," + NEW_LINE +
                    "     \"xmlSchema\": \"\"" + NEW_LINE +
                    "   }," + NEW_LINE +
                    "   {" + NEW_LINE +
                    "     \"not\": false," + NEW_LINE +
                    "     \"type\": \"XPATH\"," + NEW_LINE +
                    "     \"xpath\": \"\"" + NEW_LINE +
                    "   }" + NEW_LINE +
                    NEW_LINE +
                    " - field: \"/body\" for schema: \"body/anyOf/0\" has error: \"instance type (integer) does not match any allowed primitive type (allowed: [\"object\"])\"" + NEW_LINE +
                    " - field: \"/body\" for schema: \"body/anyOf/1\" has error: \"instance type (integer) does not match any allowed primitive type (allowed: [\"object\"])\"" + NEW_LINE +
                    " - field: \"/body\" for schema: \"body/anyOf/10\" has error: \"instance type (integer) does not match any allowed primitive type (allowed: [\"object\"])\"" + NEW_LINE +
                    " - field: \"/body\" for schema: \"body/anyOf/11\" has error: \"instance type (integer) does not match any allowed primitive type (allowed: [\"object\"])\"" + NEW_LINE +
                    " - field: \"/body\" for schema: \"body/anyOf/12\" has error: \"instance type (integer) does not match any allowed primitive type (allowed: [\"object\"])\"" + NEW_LINE +
                    " - field: \"/body\" for schema: \"body/anyOf/2\" has error: \"instance type (integer) does not match any allowed primitive type (allowed: [\"object\"])\"" + NEW_LINE +
                    " - field: \"/body\" for schema: \"body/anyOf/3\" has error: \"instance type (integer) does not match any allowed primitive type (allowed: [\"array\"])\"" + NEW_LINE +
                    " - field: \"/body\" for schema: \"body/anyOf/4\" has error: \"instance type (integer) does not match any allowed primitive type (allowed: [\"object\"])\"" + NEW_LINE +
                    " - field: \"/body\" for schema: \"body/anyOf/5\" has error: \"instance type (integer) does not match any allowed primitive type (allowed: [\"object\"])\"" + NEW_LINE +
                    " - field: \"/body\" for schema: \"body/anyOf/6\" has error: \"instance type (integer) does not match any allowed primitive type (allowed: [\"object\"])\"" + NEW_LINE +
                    " - field: \"/body\" for schema: \"body/anyOf/7\" has error: \"instance type (integer) does not match any allowed primitive type (allowed: [\"object\"])\"" + NEW_LINE +
                    " - field: \"/body\" for schema: \"body/anyOf/8\" has error: \"instance type (integer) does not match any allowed primitive type (allowed: [\"object\"])\"" + NEW_LINE +
                    " - field: \"/body\" for schema: \"body/anyOf/9\" has error: \"instance type (integer) does not match any allowed primitive type (allowed: [\"string\"])\"" + NEW_LINE +
                    NEW_LINE +
                    OPEN_API_SPECIFICATION_URL
            ));
    }

    @Test
    public void shouldValidateInvalidExtraField() {
        // when
        assertThat(jsonSchemaValidator.isValid("{" + NEW_LINE +
                "    \"invalidField\" : {" + NEW_LINE +
                "      \"type\" : \"STRING\"," + NEW_LINE +
                "      \"value\" : \"someBody\"" + NEW_LINE +
                "    }" + NEW_LINE +
                "  }"),
            is(
                "1 error:" + NEW_LINE +
                    " - object instance has properties which are not allowed by the schema: [\"invalidField\"]" + NEW_LINE +
                    NEW_LINE +
                    OPEN_API_SPECIFICATION_URL
            ));
    }

    @Test
    public void shouldValidateMultipleInvalidFieldTypes() {
        // when
        assertThat(jsonSchemaValidator.isValid("{" + NEW_LINE +
                "    \"method\" : 100," + NEW_LINE +
                "    \"path\" : false" + NEW_LINE +
                "  }"),
            is(
                "4 errors:" + NEW_LINE +
                    " - field: \"/method\" for schema: \"stringOrJsonSchema/oneOf/0\" has error: \"instance type (integer) does not match any allowed primitive type (allowed: [\"string\"])\"" + NEW_LINE +
                    " - field: \"/method\" for schema: \"stringOrJsonSchema/oneOf/1\" has error: \"instance type (integer) does not match any allowed primitive type (allowed: [\"object\"])\"" + NEW_LINE +
                    " - field: \"/path\" for schema: \"stringOrJsonSchema/oneOf/0\" has error: \"instance type (boolean) does not match any allowed primitive type (allowed: [\"string\"])\"" + NEW_LINE +
                    " - field: \"/path\" for schema: \"stringOrJsonSchema/oneOf/1\" has error: \"instance type (boolean) does not match any allowed primitive type (allowed: [\"object\"])\"" + NEW_LINE +
                    "" + NEW_LINE +
                    OPEN_API_SPECIFICATION_URL
            ));
    }

    @Test
    public void shouldValidateInvalidListItemType() {
        // when
        assertThat(jsonSchemaValidator.isValid("{" + NEW_LINE +
                "    \"headers\" : [ \"invalidValueOne\", \"invalidValueTwo\" ]" + NEW_LINE +
                "  }"),
            is("4 errors:" + NEW_LINE +
                " - field: \"/headers\" for schema: \"keyToMultiValue\" has error: \" only one of the following example formats is allowed: " + NEW_LINE +
                NEW_LINE +
                "   {" + NEW_LINE +
                "       \"exampleRegexHeader\": [" + NEW_LINE +
                "           \"^some +regex$\"" + NEW_LINE +
                "       ], " + NEW_LINE +
                "       \"exampleNottedAndSimpleStringHeader\": [" + NEW_LINE +
                "           \"!notThisValue\", " + NEW_LINE +
                "           \"simpleStringMatch\"" + NEW_LINE +
                "       ]" + NEW_LINE +
                "   }" + NEW_LINE +
                NEW_LINE +
                "or:" + NEW_LINE +
                NEW_LINE +
                "   {" + NEW_LINE +
                "       \"exampleSchemaHeader\": [" + NEW_LINE +
                "           {" + NEW_LINE +
                "               \"type\": \"number\"" + NEW_LINE +
                "           }" + NEW_LINE +
                "       ], " + NEW_LINE +
                "       \"exampleMultiSchemaHeader\": [" + NEW_LINE +
                "           {" + NEW_LINE +
                "               \"type\": \"string\", " + NEW_LINE +
                "               \"pattern\": \"^some +regex$\"" + NEW_LINE +
                "           }, " + NEW_LINE +
                "           {" + NEW_LINE +
                "               \"type\": \"string\", " + NEW_LINE +
                "               \"format\": \"ipv4\"" + NEW_LINE +
                "           }" + NEW_LINE +
                "       ]" + NEW_LINE +
                "   }" + NEW_LINE +
                NEW_LINE +
                " - field: \"/headers\" for schema: \"keyToMultiValue/oneOf/1\" has error: \"instance type (array) does not match any allowed primitive type (allowed: [\"object\"])\"" + NEW_LINE +
                " - field: \"/headers/0\" for schema: \"keyToMultiValue/oneOf/0/items\" has error: \"instance type (string) does not match any allowed primitive type (allowed: [\"object\"])\"" + NEW_LINE +
                " - field: \"/headers/1\" for schema: \"keyToMultiValue/oneOf/0/items\" has error: \"instance type (string) does not match any allowed primitive type (allowed: [\"object\"])\"" + NEW_LINE +
                NEW_LINE +
                OPEN_API_SPECIFICATION_URL));
    }

}
