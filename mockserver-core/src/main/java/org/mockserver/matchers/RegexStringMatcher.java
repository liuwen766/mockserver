package org.mockserver.matchers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.mockserver.log.model.LogEntry;
import org.mockserver.logging.MockServerLogger;
import org.mockserver.model.NottableSchemaString;
import org.mockserver.model.NottableString;

import java.util.regex.PatternSyntaxException;

import static org.mockserver.model.NottableString.string;
import static org.slf4j.event.Level.DEBUG;

/**
 * @author jamesdbloom
 */
public class RegexStringMatcher extends BodyMatcher<NottableString> {

    private static final String[] EXCLUDED_FIELDS = {"mockServerLogger"};
    private final MockServerLogger mockServerLogger;
    private final NottableString matcher;
    private final boolean controlPlaneMatcher;

    public RegexStringMatcher(MockServerLogger mockServerLogger, boolean controlPlaneMatcher) {
        this.mockServerLogger = mockServerLogger;
        this.controlPlaneMatcher = controlPlaneMatcher;
        this.matcher = null;
    }

    RegexStringMatcher(MockServerLogger mockServerLogger, NottableString matcher, boolean controlPlaneMatcher) {
        this.mockServerLogger = mockServerLogger;
        this.controlPlaneMatcher = controlPlaneMatcher;
        this.matcher = matcher;
    }

    public boolean matches(String matched) {
        return matches(null, string(matched));
    }

    public boolean matches(final MatchDifference context, NottableString matched) {
        boolean result = false;

        if (matcher == null || matches(matcher, matched, true)) {
            result = true;
        }

        if (!result && context != null) {
            context.addDifference(mockServerLogger, "string or regex match failed expected:{}found:{}", this.matcher, matched);
        }

        return not != result;
    }

    public boolean matches(NottableString matcher, NottableString matched, boolean ignoreCase) {
        if (matcher instanceof NottableSchemaString && matched instanceof NottableSchemaString) {
            return controlPlaneMatcher && matchesByNottedStrings(matcher, matched, ignoreCase);
        } else if (matcher instanceof NottableSchemaString) {
            return matchesBySchemas((NottableSchemaString) matcher, matched);
        } else if (matched instanceof NottableSchemaString) {
            return controlPlaneMatcher && matchesBySchemas((NottableSchemaString) matched, matcher);
        } else {
            return matchesByNottedStrings(matcher, matched, ignoreCase);
        }
    }

    private boolean matchesByNottedStrings(NottableString matcher, NottableString matched, boolean ignoreCase) {
        if (matcher.isNot() && matched.isNot()) {
            // mutual notted control plane match
            return matchesByStrings(matcher, matched, ignoreCase);
        } else {
            // data plane & control plan match
            return (matcher.isNot() || matched.isNot()) ^ matchesByStrings(matcher, matched, ignoreCase);
        }
    }

    private boolean matchesBySchemas(NottableSchemaString schema, NottableString string) {
        return string.isNot() != schema.matches(string.getValue());
    }

    private boolean matchesByStrings(NottableString matcher, NottableString matched, boolean ignoreCase) {
        final String matcherValue = matcher.getValue();
        if (StringUtils.isBlank(matcherValue)) {
            return true;
        } else {
            final String matchedValue = matched.getValue();
            if (matchedValue != null) {
                // match as exact string
                if (matchedValue.equals(matcherValue)) {
                    return true;
                }

                // match as regex - matcher -> matched (data plane or control plane)
                try {
                    if (matcher.matches(matchedValue)) {
                        return true;
                    }
                } catch (PatternSyntaxException pse) {
                    if (MockServerLogger.isEnabled(DEBUG)) {
                        mockServerLogger.logEvent(
                            new LogEntry()
                                .setLogLevel(DEBUG)
                                .setMessageFormat("error while matching regex [" + matcher + "] for string [" + matched + "] " + pse.getMessage())
                                .setThrowable(pse)
                        );
                    }
                }
                // match as regex - matched -> matcher (control plane only)
                try {
                    if (controlPlaneMatcher && matched.matches(matcherValue)) {
                        return true;
                    } else if (MockServerLogger.isEnabled(DEBUG) && matched.matches(matcherValue)) {
                        mockServerLogger.logEvent(
                            new LogEntry()
                                .setLogLevel(DEBUG)
                                .setMessageFormat("matcher{}would match{}if matcher was used for control plane")
                                .setArguments(matcher, matched)
                        );
                    }
                } catch (PatternSyntaxException pse) {
                    if (controlPlaneMatcher) {
                        if (MockServerLogger.isEnabled(DEBUG)) {
                            mockServerLogger.logEvent(
                                new LogEntry()
                                    .setLogLevel(DEBUG)
                                    .setMessageFormat("error while matching regex [" + matched + "] for string [" + matcher + "] " + pse.getMessage())
                                    .setThrowable(pse)
                            );
                        }
                    }
                }
                // case-insensitive comparison is mainly to improve matching in web containers like Tomcat that convert header names to lower case
                if (ignoreCase) {
                    // match as exact string lower-case
                    if (matchedValue.equalsIgnoreCase(matcherValue)) {
                        return true;
                    }
                    // match as regex - matcher -> matched (data plane or control plane)
                    try {
                        if (matcher.matchesIgnoreCase(matchedValue)) {
                            return true;
                        }
                    } catch (PatternSyntaxException pse) {
                        if (MockServerLogger.isEnabled(DEBUG)) {
                            mockServerLogger.logEvent(
                                new LogEntry()
                                    .setLogLevel(DEBUG)
                                    .setMessageFormat("error while matching regex [" + matcherValue.toLowerCase() + "] for string [" + matchedValue.toLowerCase() + "] " + pse.getMessage())
                                    .setThrowable(pse)
                            );
                        }
                    }
                    // match as regex - matched -> matcher (control plane only)
                    try {
                        if (controlPlaneMatcher && matched.matchesIgnoreCase(matcherValue)) {
                            return true;
                        }
                    } catch (PatternSyntaxException pse) {
                        if (MockServerLogger.isEnabled(DEBUG)) {
                            mockServerLogger.logEvent(
                                new LogEntry()
                                    .setLogLevel(DEBUG)
                                    .setMessageFormat("error while matching regex [" + matchedValue.toLowerCase() + "] for string [" + matcherValue.toLowerCase() + "] " + pse.getMessage())
                                    .setThrowable(pse)
                            );
                        }
                    }
                }
            }
        }

        return false;
    }

    public boolean isBlank() {
        return matcher == null || StringUtils.isBlank(matcher.getValue());
    }

    @Override
    @JsonIgnore
    protected String[] fieldsExcludedFromEqualsAndHashCode() {
        return EXCLUDED_FIELDS;
    }
}
