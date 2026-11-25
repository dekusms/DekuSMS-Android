#!/bin/bash

cargo clean
cargo ndk \
	-t armeabi-v7a -t arm64-v8a -t x86 -t x86_64 \
	-o ../app/src/main/jniLibs/ \
	build --release
