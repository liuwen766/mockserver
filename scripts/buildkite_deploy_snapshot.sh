#!/usr/bin/env bash

set -e

export MAVEN_OPTS="$MAVEN_OPTS -Xmx3072m"
export JAVA_OPTS="$JAVA_OPTS -Xmx3072m"
echo
java -version
echo
./mvnw -version
echo

if test "$BUILDKITE_BRANCH" = "master"; then
    echo "BRANCH: MASTER"
    ./mvnw -s /etc/maven/settings.xml clean deploy $1 -Djava.security.egd=file:/dev/./urandom
else
    echo "BRANCH: $CURRENT_BRANCH"
    ./mvnw -s /etc/maven/settings.xml clean install $1 -Djava.security.egd=file:/dev/./urandom
fi
