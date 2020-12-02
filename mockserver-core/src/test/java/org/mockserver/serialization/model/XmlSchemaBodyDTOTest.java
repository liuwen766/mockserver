package org.mockserver.serialization.model;

import org.junit.Test;
import org.mockserver.model.Body;
import org.mockserver.model.XPathBody;
import org.mockserver.model.XmlSchemaBody;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockserver.model.XmlSchemaBody.xmlSchema;


/**
 * @author jamesdbloom
 */
public class XmlSchemaBodyDTOTest {

    @Test
    public void shouldReturnValuesSetInConstructor() {
        // when
        XmlSchemaBodyDTO xmlSchemaBodyDTO = new XmlSchemaBodyDTO(new XmlSchemaBody("some_body"));

        // then
        assertThat(xmlSchemaBodyDTO.getXml(), is("some_body"));
        assertThat(xmlSchemaBodyDTO.getType(), is(Body.Type.XML_SCHEMA));
    }

    @Test
    public void shouldBuildCorrectObject() {
        // when
        XmlSchemaBody xmlSchemaBody = new XmlSchemaBodyDTO(new XmlSchemaBody("some_body")).buildObject();

        // then
        assertThat(xmlSchemaBody.getValue(), is("some_body"));
        assertThat(xmlSchemaBody.getType(), is(Body.Type.XML_SCHEMA));
    }

    @Test
    public void shouldBuildCorrectObjectWithOptional() {
        // when
        XmlSchemaBody xmlSchemaBody = new XmlSchemaBodyDTO((XmlSchemaBody) new XmlSchemaBody("some_body").withOptional(true)).buildObject();

        // then
        assertThat(xmlSchemaBody.getValue(), is("some_body"));
        assertThat(xmlSchemaBody.getType(), is(Body.Type.XML_SCHEMA));
        assertThat(xmlSchemaBody.getOptional(), is(true));
    }

    @Test
    public void shouldReturnCorrectObjectFromStaticBuilder() {
        assertThat(xmlSchema("some_body"), is(new XmlSchemaBody("some_body")));
    }
}
