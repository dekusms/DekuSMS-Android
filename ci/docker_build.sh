#!/bin/bash

set -eou pipefail

image_name=deku_rep_build_release

docker build -t deku_rep_build_release .

docker run --rm -v "$(pwd)":/project -w /project --user "$(id -u):$(id -g)" $image_name \
	./gradlew assembleRelease \
	--no-daemon \
	--max-workers=2 \
	--console=plain \
	-Dorg.gradle.jvmargs="-Xmx1024m -Xms256m -XX:MaxMetaspaceSize=384m -Dfile.encoding=UTF-8" \
	-Dkotlin.daemon.jvm.options="-Xmx512m,-Xss1m" \
	-Dkotlin.compiler.execution.strategy=in-process
sha1=$( sha256sum app/build/outputs/apk/release/app-release-unsigned.apk )
rm app/build/outputs/apk/release/app-release-unsigned.apk

docker run --rm -v "$(pwd)":/project -w /project --user "$(id -u):$(id -g)" $image_name \
	./gradlew assembleRelease \
	--no-daemon \
	--max-workers=2 \
	--console=plain \
	-Dorg.gradle.jvmargs="-Xmx1024m -Xms256m -XX:MaxMetaspaceSize=384m -Dfile.encoding=UTF-8" \
	-Dkotlin.daemon.jvm.options="-Xmx512m,-Xss1m" \
	-Dkotlin.compiler.execution.strategy=in-process

sha2=$( sha256sum app/build/outputs/apk/release/app-release-unsigned.apk )
echo "$sha1"
echo "$sha1"

if [ "$sha1" eq "$sha2" ]; then
	echo "All good! Reproducible"
else
	echo "Not reproducible..."
fi
