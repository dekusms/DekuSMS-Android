FROM ubuntu:jammy-20230624@sha256:b060fffe8e1561c9c3e6dea6db487b900100fc26830b9ea2ec966c151ab4c020

# Consolidate dependencies cleanly into one layer
RUN apt-get update && apt-get install -y \
    git \
    openjdk-17-jdk \
    unzip \
    wget \
    tar \
    && rm -rf /var/lib/apt/lists/*

# Pre-install the JetBrains JDK 21 toolchain locally
RUN mkdir -p /opt/hostedtoolchains/JetBrains/21.0.0/x64 \
    && wget -O /tmp/jbr.tar.gz https://cache-redirector.jetbrains.com/intellij-jbr/jbr_jcef-21.0.6-linux-x64-b895.97.tar.gz \
    && tar -xzf /tmp/jbr.tar.gz -C /opt/hostedtoolchains/JetBrains/21.0.0/x64 --strip-components=1 \
    && rm /tmp/jbr.tar.gz

ENV GRADLE_USER_HOME=/project/.gradle
WORKDIR /android

ENV ANDROID_COMMAND_LINE_TOOLS_FILENAME commandlinetools-linux-10406996_latest.zip
ENV ANDROID_API_LEVELS                  android-36
ENV ANDROID_BUILD_TOOLS_VERSION         35.0.0

ENV ANDROID_HOME /usr/local/android-sdk-linux
ENV PATH         ${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/cmdline-tools/bin

# Explicitly download command-line tools to /usr/local/ using explicit output flags
RUN wget -q "https://dl.google.com/android/repository/${ANDROID_COMMAND_LINE_TOOLS_FILENAME}" -O /usr/local/${ANDROID_COMMAND_LINE_TOOLS_FILENAME} \
    && unzip /usr/local/${ANDROID_COMMAND_LINE_TOOLS_FILENAME} -d /usr/local/android-sdk-linux \
    && rm /usr/local/${ANDROID_COMMAND_LINE_TOOLS_FILENAME}

RUN yes | sdkmanager --update --sdk_root="${ANDROID_HOME}"
RUN yes | sdkmanager --sdk_root="${ANDROID_HOME}" "platforms;${ANDROID_API_LEVELS}" "build-tools;${ANDROID_BUILD_TOOLS_VERSION}" "extras;google;m2repository" "extras;android;m2repository" "extras;google;google_play_services" "ndk;28.0.13004108" "cmake;3.22.1"

RUN yes | sdkmanager --licenses --sdk_root="${ANDROID_HOME}"

RUN rm -rf ${ANDROID_HOME}/tools

RUN git config --global --add safe.directory /project
