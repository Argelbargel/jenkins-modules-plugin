#!/usr/bin/env bash

echo "Building branch $TRAVIS_BRANCH (pull-request: $TRAVIS_PULL_REQUEST)..."

GRADLE_TASKS="test"
if [ "$TRAVIS_TAG" != "" ]; then
    GRADLE_TASKS="$GRADLE_TASKS publish"
fi

./gradlew ${GRADLE_TASKS} -Prelease=${TRAVIS_TAG} --no-daemon

if [ "$TRAVIS_TAG" != "" ]; then
    ./travis/publish.sh
fi

