package org.mockserver.serialization.model;

import org.mockserver.model.Header;

/**
 * @author jamesdbloom
 */
public class HeaderDTO extends KeyToMultiValueDTO implements DTO<Header> {

    public HeaderDTO(Header header) {
        super(header);
    }

    protected HeaderDTO() {
    }

    public Header buildObject() {
        return new Header(getName(), getValues());
    }
}
