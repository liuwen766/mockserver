package org.mockserver.serialization.model;

import org.mockserver.model.Body;
import org.mockserver.model.XPathBody;

/**
 * @author jamesdbloom
 */
public class XPathBodyDTO extends BodyDTO {

    private final String xpath;

    public XPathBodyDTO(XPathBody xPathBody) {
        this(xPathBody, null);
    }

    public XPathBodyDTO(XPathBody xPathBody, Boolean not) {
        super(Body.Type.XPATH, not);
        this.xpath = xPathBody.getValue();
        withOptional(xPathBody.getOptional());
    }

    public String getXPath() {
        return xpath;
    }

    public XPathBody buildObject() {
        return (XPathBody) new XPathBody(getXPath()).withOptional(getOptional());
    }
}
