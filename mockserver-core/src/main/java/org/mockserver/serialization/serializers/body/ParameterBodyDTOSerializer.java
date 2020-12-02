package org.mockserver.serialization.serializers.body;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.mockserver.serialization.model.ParameterBodyDTO;

import java.io.IOException;

/**
 * @author jamesdbloom
 */
public class ParameterBodyDTOSerializer extends StdSerializer<ParameterBodyDTO> {

    public ParameterBodyDTOSerializer() {
        super(ParameterBodyDTO.class);
    }

    @Override
    public void serialize(ParameterBodyDTO parameterBodyDTO, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeStartObject();
        if (parameterBodyDTO.getNot() != null && parameterBodyDTO.getNot()) {
            jgen.writeBooleanField("not", parameterBodyDTO.getNot());
        }
        if (parameterBodyDTO.getOptional() != null && parameterBodyDTO.getOptional()) {
            jgen.writeBooleanField("optional", parameterBodyDTO.getOptional());
        }
        jgen.writeStringField("type", parameterBodyDTO.getType().name());
        if (!parameterBodyDTO.getParameters().isEmpty()) {
            jgen.writeObjectField("parameters", parameterBodyDTO.getParameters());
        }
        jgen.writeEndObject();
    }
}
