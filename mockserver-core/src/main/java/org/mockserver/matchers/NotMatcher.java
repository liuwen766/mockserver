package org.mockserver.matchers;

import org.mockserver.model.ObjectWithReflectiveEqualsHashCodeToString;

/**
 * @author jamesdbloom
 */
public abstract class NotMatcher<MatchedType> extends ObjectWithReflectiveEqualsHashCodeToString implements Matcher<MatchedType> {

    boolean not = false;

    public static <MatcherType extends NotMatcher<?>> MatcherType notMatcher(MatcherType matcher) {
        matcher.not = true;
        return matcher;
    }

}
