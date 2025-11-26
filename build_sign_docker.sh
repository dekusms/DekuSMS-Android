#!/bin/bash
set -euo pipefail

tagVersion=$(sed -n '5p' version.properties | cut -d "=" -f 2)
label=$(sed -n '4p' version.properties | cut -d "=" -f 2)
branch=$(git symbolic-ref HEAD | cut -d "/" -f 3)
track=$(python3 track.py "$branch")

docker_apk_image=swob_app_apk_image
docker_apk_image_commit_check=docker_apk_image_commit_check
docker_app_image=swob_app_app_image

echo "[+] label: $label"

APP1="$label".apk
APP2="$label".1.apk

echo "[+] App 1 := $APP1"
echo "[+] App 2 := $APP2"

CONTAINER_NAME=swob_app_container_$label
CONTAINER_NAME_1=swob_app_container_"$label"_1
# CONTAINER_NAME_BUNDLE=swob_app_container_"$label"_bundle
# CONTAINER_NAME_COMMIT_CHECK=$(commit)_commit_check

minSdk=24

venv/bin/python bump_version.py "$(git symbolic-ref HEAD)"

git add .
git commit -m "release: making release"
git tag -f "$tagVersion"

echo "[+] Cleaning up..."
containers=$(docker ps -a --filter "ancestor=$docker_apk_image" --format "{{.ID}}"); \
	if [ -n "$$containers" ]; then \
	    docker stop $$containers; \
	    docker rm $$containers; \
	fi
containers=$(docker ps -a --filter "ancestor=$docker_app_image" --format "{{.ID}}"); \
	if [ -n "$$containers" ]; then \
	    docker stop $$containers; \
	    docker rm $$containers; \
	fi
containers=$(docker ps -a --filter "ancestor=$docker_apk_image_commit_check" --format "{{.ID}}"); \
	if [ -n "$$containers" ]; then \
	    docker stop $$containers; \
	    docker rm $$containers; \
	fi
echo "[+] Done cleaning up..."


echo "[+] Building apk output: $APP1"
DOCKER_BUILDKIT=1 docker build --platform linux/amd64 -t $docker_apk_image --target apk-builder .
docker run --memory="8g" --cpus="7" --name $CONTAINER_NAME -e PASS=$1 $docker_apk_image && \
	docker cp $CONTAINER_NAME:/android/app/build/outputs/apk/release/app-release.apk apk-outputs/$APP1

echo "[+] Building apk output: $APP2"
docker run --memory="8g" --cpus="7" --name $CONTAINER_NAME_1 -e PASS=$1 $docker_apk_image && \
	docker cp $CONTAINER_NAME_1:/android/app/build/outputs/apk/release/app-release.apk apk-outputs/$APP2

diffoscope apk-outputs/$APP1 apk-outputs/$APP2
echo "[+] All good!! Publish..."

