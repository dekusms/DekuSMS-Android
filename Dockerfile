# --- Stage 1: Base Environment ---
FROM ubuntu:22.04 AS base
RUN apt-get update && apt-get install -y openjdk-17-jdk wget unzip && rm -rf /var/lib/apt/lists/*

ENV ANDROID_HOME="/opt/android-sdk"
ENV PATH="${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools"

# Install Android SDK Tools
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -O /tmp/tools.zip && \
    unzip /tmp/tools.zip -d ${ANDROID_HOME}/cmdline-tools && \
    mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest && \
    rm /tmp/tools.zip

# Pre-install specific versions for reproducibility
RUN yes | sdkmanager --licenses && \
    sdkmanager "platforms;android-33" "build-tools;33.0.0" "platform-tools"

WORKDIR /android

# --- Stage 2: Cache Gradle ---
COPY gradlew .
COPY gradle gradle
COPY build.gradle . 
COPY settings.gradle .
COPY app/build.gradle app/
RUN ./gradlew --no-daemon dependencies

# --- Stage 3: Build APK ---
FROM base AS apk-builder
COPY . .
# Fixed timestamp for reproducible ZIP/APK entries
ENV SOURCE_DATE_EPOCH=1709834400 
# Run the build during the 'docker build' phase so it's saved in the image
RUN ./gradlew assembleRelease --no-daemon
