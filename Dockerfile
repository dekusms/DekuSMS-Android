FROM ubuntu:22.04 AS base
# Ensure we have essential tools for Gradle to run
RUN apt-get update && apt-get install -y \
    openjdk-17-jdk wget unzip git \
    && rm -rf /var/lib/apt/lists/*

ENV ANDROID_HOME="/opt/android-sdk"
ENV PATH="${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools"

# Install SDK Tools properly
RUN mkdir -p ${ANDROID_HOME}/cmdline-tools && \
    wget -q https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -O /tmp/tools.zip && \
    unzip /tmp/tools.zip -d ${ANDROID_HOME}/cmdline-tools && \
    mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest && \
    rm /tmp/tools.zip

# Pre-accept licenses and install required components
# Use the versions your specific app needs
RUN yes | sdkmanager --licenses && \
    sdkmanager "platforms;android-33" "build-tools;33.0.0"

WORKDIR /android

# --- Cache Stage ---
COPY gradlew .
COPY gradle/ gradle/
COPY build.gradle . 
COPY settings.gradle .
COPY app/build.gradle app/

# FIX: Ensure gradlew is executable and run dependencies
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies

# --- Build Stage ---
FROM base AS apk-builder
COPY . .
ENV SOURCE_DATE_EPOCH=1709834400 
RUN chmod +x gradlew && ./gradlew assembleRelease --no-daemon
