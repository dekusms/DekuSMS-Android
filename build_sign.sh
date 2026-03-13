#!/bin/bash
set -euo pipefail

MIN_SDK=24
github_url="https://api.github.com/repos/dekusms/DekuSMS-Android/releases"

python3 -m venv venv
venv/bin/pip install -r requirements.txt

# use venv's python explicitly for all python commands
venv/bin/python bump_version.py "$(git symbolic-ref HEAD)"

tagVersion=$(sed -n '5p' version.properties | cut -d "=" -f 2)
label=$(sed -n '4p' version.properties | cut -d "=" -f 2)
branch=$(git symbolic-ref HEAD | cut -d "/" -f 3)
track=$(python3 track.py "$branch")

git add .
git commit -m "release: making release"
git tag -f "${tagVersion}"

# ./gradlew clean assembleRelease
./gradlew clean assembleRelease --no-build-cache --no-configuration-cache --no-daemon
apksigner sign --ks app/keys/app-release-key.jks \
  --ks-pass pass:"$1" \
  --in app/build/outputs/apk/release/app-release-unsigned.apk \
  --out apk-outputs/"$label".apk

# ./gradlew clean assembleRelease
./gradlew clean assembleRelease --no-build-cache --no-configuration-cache --no-daemon
apksigner sign --ks app/keys/app-release-key.jks \
  --ks-pass pass:"$1" \
  --in app/build/outputs/apk/release/app-release-unsigned.apk \
  --out app/build/outputs/apk/release/"$label".apk

# This will now stop the script immediately if diffoscope fails
diffoscope apk-outputs/"$label".apk app/build/outputs/apk/release/"$label".apk

./gradlew assemble bundleRelease
apksigner sign --ks app/keys/app-release-key.jks \
  --ks-pass pass:"$1" \
  --in app/build/outputs/bundle/release/app-release.aab \
  --out app/build/outputs/bundle/release/app-bundle.aab \
  --min-sdk-version "$MIN_SDK"

git push origin "$branch"
git push --tag

venv/bin/python release.py \
	--version_code "${tagVersion}" \
        --version_name "${label}" \
        --description "<b>Release</b>: ${label}<br><b>Build No</b>: ${tagVersion}<br><b>shasum</b>: $(shasum apk-outputs/${label}.apk)" \
        --branch "${branch}" \
        --track "${track}" \
        --app_bundle_file app/build/outputs/bundle/release/app-bundle.aab \
        --app_apk_file app/build/outputs/apk/release/"${label}".apk \
        --status "completed" \
        --platforms "all" \
        --github_url "${github_url}"

rm apk-outputs/"$label".apk
