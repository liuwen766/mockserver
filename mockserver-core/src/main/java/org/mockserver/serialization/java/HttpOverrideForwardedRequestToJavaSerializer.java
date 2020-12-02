package org.mockserver.serialization.java;

import com.google.common.base.Strings;
import org.mockserver.model.HttpOverrideForwardedRequest;

import static org.mockserver.character.Character.NEW_LINE;
import static org.mockserver.serialization.java.ExpectationToJavaSerializer.INDENT_SIZE;

/**
 * @author jamesdbloom
 */
public class HttpOverrideForwardedRequestToJavaSerializer implements ToJavaSerializer<HttpOverrideForwardedRequest> {

    @Override
    public String serialize(int numberOfSpacesToIndent, HttpOverrideForwardedRequest httpForward) {
        StringBuffer output = new StringBuffer();
        if (httpForward != null) {
            appendNewLineAndIndent(numberOfSpacesToIndent * INDENT_SIZE, output).append("forwardOverriddenRequest()");
            if (httpForward.getHttpRequest() != null) {
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(".withRequest(");
                output.append(new HttpRequestToJavaSerializer().serialize(numberOfSpacesToIndent + 2, httpForward.getHttpRequest()));
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(")");
            }
            if (httpForward.getHttpResponse() != null) {
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(".withResponse(");
                output.append(new HttpResponseToJavaSerializer().serialize(numberOfSpacesToIndent + 2, httpForward.getHttpResponse()));
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(")");
            }
            if (httpForward.getDelay() != null) {
                appendNewLineAndIndent((numberOfSpacesToIndent + 1) * INDENT_SIZE, output).append(".withDelay(");
                output.append(new DelayToJavaSerializer().serialize(0, httpForward.getDelay()));
                output.append(")");
            }
        }
        return output.toString();
    }

    private StringBuffer appendNewLineAndIndent(int numberOfSpacesToIndent, StringBuffer output) {
        return output.append(NEW_LINE).append(Strings.padStart("", numberOfSpacesToIndent, ' '));
    }
}
