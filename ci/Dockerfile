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

ENV ANDROID_COMMAND_LINE_TOOLS_FILENAME commandlinetools-linux-14742923_latest.zip
ENV ANDROID_API_LEVELS                  android-36
ENV ANDROID_BUILD_TOOLS_VERSION         35.0.0

ENV ANDROID_HOME /usr/local/android-sdk-linux
# UPDATED PATH: Added /cmdline-tools/latest/bin to correctly map to the new layout
ENV PATH         ${PATH}:${ANDROID_HOME}/tools:${ANDROID_HOME}/platform-tools:${ANDROID_HOME}/cmdline-tools/latest/bin

# 1. Create the structured directory layout that modern sdkmanager demands
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools

# 2. Explicitly download command-line tools zip
RUN wget -q "https://dl.google.com/android/repository/${ANDROID_COMMAND_LINE_TOOLS_FILENAME}" -O /tmp/${ANDROID_COMMAND_LINE_TOOLS_FILENAME} \
    && unzip /tmp/${ANDROID_COMMAND_LINE_TOOLS_FILENAME} -d ${ANDROID_HOME}/cmdline-tools \
    && mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
    && rm /tmp/${ANDROID_COMMAND_LINE_TOOLS_FILENAME}

# 3. Accept licensing agreements prior to triggering installations
RUN yes | sdkmanager --licenses --sdk_root="${ANDROID_HOME}"

# 4. Update core sdkmanager channel manifests
RUN yes | sdkmanager --update --sdk_root="${ANDROID_HOME}"

# 5. Install the packages cleanly (platforms;android-37 will now resolve successfully!)
RUN yes | sdkmanager --sdk_root="${ANDROID_HOME}" \
    "platforms;${ANDROID_API_LEVELS}" \
    "build-tools;${ANDROID_BUILD_TOOLS_VERSION}" \
    "extras;google;m2repository" \
    "extras;android;m2repository" \
    "extras;google;google_play_services" \
    "ndk;28.0.13004108" \
    "cmake;3.22.1"

RUN git config --global --add safe.directory /project
