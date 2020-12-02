package org.mockserver.serialization.serializers.body;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.mockserver.serialization.model.StringBodyDTO;

import java.io.IOException;

/**
 * @author jamesdbloom
 */
public class StringBodyDTOSerializer extends StdSerializer<StringBodyDTO> {

    public StringBodyDTOSerializer() {
        super(StringBodyDTO.class);
    }

    @Override
    public void serialize(StringBodyDTO stringBodyDTO, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        boolean notFieldSetAndNotDefault = stringBodyDTO.getNot() != null && stringBodyDTO.getNot();
        boolean optionalFieldSetAndNotDefault = stringBodyDTO.getOptional() != null && stringBodyDTO.getOptional();
        boolean subStringFieldNotDefault = stringBodyDTO.isSubString();
        boolean contentTypeFieldSet = stringBodyDTO.getContentType() != null;
        if (notFieldSetAndNotDefault || optionalFieldSetAndNotDefault || contentTypeFieldSet || subStringFieldNotDefault) {
            jgen.writeStartObject();
            if (notFieldSetAndNotDefault) {
                jgen.writeBooleanField("not", true);
            }
            if (optionalFieldSetAndNotDefault) {
                jgen.writeBooleanField("optional", true);
            }
            jgen.writeStringField("type", stringBodyDTO.getType().name());
            jgen.writeStringField("string", stringBodyDTO.getString());
            if (stringBodyDTO.getRawBytes() != null) {
                jgen.writeObjectField("rawBytes", stringBodyDTO.getRawBytes());
            }
            if (subStringFieldNotDefault) {
                jgen.writeBooleanField("subString", true);
            }
            if (contentTypeFieldSet) {
                jgen.writeStringField("contentType", stringBodyDTO.getContentType());
            }
            jgen.writeEndObject();
        } else {
            jgen.writeString(stringBodyDTO.getString());
        }
    }
}
