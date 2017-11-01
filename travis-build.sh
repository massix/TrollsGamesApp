#!/usr/bin/env bash

if [[ ${TRAVIS_BRANCH} == "develop" ]]; then
    ./gradlew check assembleStaging
    cp ./app/build/outputs/apk/app-staging.apk TrollsGames.apk
elif [[ ${TRAVIS_BRANCH} == "master" ]]; then
    ./gradlew check assembleRelease
    cp ./app/build/outputs/apk/app-release.apk TrollsGames-${TRAVIS_TAG}.apk
fi