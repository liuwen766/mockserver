package org.mockserver.templates.engine.velocity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.velocity.script.VelocityScriptEngineFactory;
import org.mockserver.log.model.LogEntry;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.model.HttpRequest;
import org.mockserver.serialization.ObjectMapperFactory;
import org.mockserver.serialization.model.DTO;
import org.mockserver.templates.engine.TemplateEngine;
import org.mockserver.templates.engine.model.HttpRequestTemplateObject;
import org.mockserver.templates.engine.serializer.HttpTemplateOutputDeserializer;
import org.slf4j.event.Level;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;
import java.io.StringWriter;
import java.io.Writer;

import static org.mockserver.formatting.StringFormatter.formatLogMessage;
import static org.mockserver.log.model.LogEntry.LogMessageType.TEMPLATE_GENERATED;
import static org.mockserver.log.model.LogEntryMessages.TEMPLATE_GENERATED_MESSAGE_FORMAT;

/**
 * @author jamesdbloom
 */
public class VelocityTemplateEngine implements TemplateEngine {

    private static final ScriptEngineManager manager = new ScriptEngineManager();
    private static final ScriptEngine engine;
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.createObjectMapper();
    private final MockServerLogger logFormatter;
    private HttpTemplateOutputDeserializer httpTemplateOutputDeserializer;

    static {
        manager.registerEngineName("velocity", new VelocityScriptEngineFactory());
        engine = manager.getEngineByName("velocity");
    }

    public VelocityTemplateEngine(MockServerLogger logFormatter) {
        this.logFormatter = logFormatter;
        this.httpTemplateOutputDeserializer = new HttpTemplateOutputDeserializer(logFormatter);
    }

    @Override
    public <T> T executeTemplate(String template, HttpRequest request, Class<? extends DTO<T>> dtoClass) {
        T result;
        try {
            Writer writer = new StringWriter();
            ScriptContext context = new SimpleScriptContext();
            context.setWriter(writer);
            context.setAttribute("request", new HttpRequestTemplateObject(request), ScriptContext.ENGINE_SCOPE);
            engine.eval(template, context);
            JsonNode generatedObject = null;
            try {
                generatedObject = OBJECT_MAPPER.readTree(writer.toString());
            } catch (Throwable throwable) {
                if (MockServerLogger.isEnabled(Level.TRACE)) {
                    logFormatter.logEvent(
                        new LogEntry()
                            .setLogLevel(Level.TRACE)
                            .setHttpRequest(request)
                            .setMessageFormat("exception deserialising generated content:{}into json node for request:{}")
                            .setArguments(writer.toString(), request)
                    );
                }
            }
            if (MockServerLogger.isEnabled(Level.INFO)) {
                logFormatter.logEvent(
                    new LogEntry()
                        .setType(TEMPLATE_GENERATED)
                        .setLogLevel(Level.INFO)
                        .setHttpRequest(request)
                        .setMessageFormat(TEMPLATE_GENERATED_MESSAGE_FORMAT)
                        .setArguments(generatedObject != null ? generatedObject : writer.toString(), template, request)
                );
            }
            result = httpTemplateOutputDeserializer.deserializer(request, writer.toString(), dtoClass);
        } catch (Exception e) {
            throw new RuntimeException(formatLogMessage("Exception transforming template:{}for request:{}", template, request), e);
        }
        return result;
    }
}
