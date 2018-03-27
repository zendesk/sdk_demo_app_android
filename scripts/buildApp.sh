#!/usr/bin/env bash

mkdir -p ${ANDROID_HOME}licenses
echo -e "\nd56f5187479451eabf01fb78af6dfcb131a6481e" > ${ANDROID_HOME}licenses/android-sdk-license

./gradlew clean assemble lint
