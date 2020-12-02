package org.mockserver.serialization.java;

import com.google.common.base.Strings;
import org.apache.commons.text.StringEscapeUtils;
import org.mockserver.model.HttpTemplate;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.mockserver.character.Character.NEW_LINE;
import static org.mockserver.serialization.java.ExpectationToJavaSerializer.INDENT_SIZE;

/**
 * @author jamesdbloom
 */
public class HttpTemplateToJavaSerializer implements ToJavaSerializer<HttpTemplate> {

    @Override
    public String serialize(int numberOfSpacesToIndent, HttpTemplate httpTemplate) {
        StringBuffer output = new StringBuffer();
        if (httpTemplate != null) {
            appendNewLineAndIndent(numberOfSpacesToIndent * INDENT_SIZE, output).append("template(HttpTemplate.TemplateType.").append(httpTemplate.getTemplateType().name()).append(")");
            if (isNotBlank(httpTemplate.getTemplate())) {
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(".withTemplate(\"").append(StringEscapeUtils.escapeJava(httpTemplate.getTemplate())).append("\")");
            }
            if (httpTemplate.getDelay() != null) {
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(".withDelay(").append(new DelayToJavaSerializer().serialize(0, httpTemplate.getDelay())).append(")");
            }
        }

        return output.toString();
    }

    private StringBuffer appendNewLineAndIndent(int numberOfSpacesToIndent, StringBuffer output) {
        return output.append(NEW_LINE).append(Strings.padStart("", numberOfSpacesToIndent, ' '));
    }
}
