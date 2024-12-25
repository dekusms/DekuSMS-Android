# pass=$$(cat $(jks_pass))
pass=$(jks_pass)
branch_name=$$(git symbolic-ref HEAD)

branch=$$(git symbolic-ref HEAD | cut -d "/" -f 3)
# track = 'internal', 'alpha', 'beta', 'production'
track=$$(python3 track.py $(branch))

releaseVersion=$$(sed -n '1p' version.properties | cut -d "=" -f 2)
stagingVersion=$$(sed -n '2p' version.properties | cut -d "=" -f 2)
nightlyVersion=$$(sed -n '3p' version.properties | cut -d "=" -f 2)
label=$$(sed -n '4p' version.properties | cut -d "=" -f 2)
tagVersion=$$(sed -n '5p' version.properties | cut -d "=" -f 2)

# label=${releaseVersion}.${stagingVersion}.${nightlyVersion}

aab_output=${label}.aab
apk_output=${label}.apk

APP_1=${label}.apk
APP_2=${label}_1.apk

CONTAINER_NAME=deku_sms_container_${label}
CONTAINER_NAME_1=deku_sms_container_${label}_1
CONTAINER_NAME_BUNDLE=deku_sms_container_${label}_bundle
CONTAINER_NAME_COMMIT_CHECK=$(commit)_commit_check

minSdk=24

github_url=https://api.github.com/repos/dekusms/DekuSMS-Android/releases
docker_apk_image=deku_sms_apk_image
docker_apk_image_commit_check=docker_apk_image_commit_check
docker_app_image=deku_sms_app_image

diff_check: 
	@echo "Building apk output: ${APP_1}"
	@docker build -t ${docker_apk_image} --platform linux/amd64 --target apk-builder .
	@docker run --name ${CONTAINER_NAME} -e PASS=$(pass) ${docker_apk_image} && \
		docker cp ${CONTAINER_NAME}:/android/app/build/outputs/apk/release/app-release.apk apk-outputs/${APP_1}
	@sleep 3
	@echo "Building apk output: ${APP_2}"
	@docker run --name ${CONTAINER_NAME_1} -e PASS=$(pass) ${docker_apk_image} && \
		docker cp ${CONTAINER_NAME_1}:/android/app/build/outputs/apk/release/app-release.apk apk-outputs/${APP_2}
	@diffoscope apk-outputs/${APP_1} apk-outputs/${APP_2}
	@echo $? | exit

docker-build-aab: diff_check
	@sleep 5
	@docker build -t ${docker_app_image} --platform linux/amd64 --target bundle-builder .
	@docker run --name ${CONTAINER_NAME_BUNDLE} -e PASS=$(pass) -e MIN_SDK=$(minSdk) ${docker_app_image} && \
		docker cp ${CONTAINER_NAME_BUNDLE}:/android/app/build/outputs/bundle/release/app-bundle.aab apk-outputs/${aab_output}

build-apk:
	@echo "+ Building apk output: ${apk_output} - ${branch_name}"
	@./gradlew clean assembleRelease
	@apksigner sign --ks app/keys/app-release-key.jks \
		--ks-pass pass:$(pass) \
		--in app/build/outputs/apk/release/app-release-unsigned.apk \
		--out apk-outputs/${apk_output}
	@shasum apk-outputs/${apk_output}

bump_version: 
	@python3 -m venv venv; \
	( \
		. venv/bin/activate; \
		pip3 install -r requirements.txt; \
		python3 bump_version.py $(branch_name); \
		git add . ; \
		git commit -m "release: making release"; \
	) 

build-aab:
	@echo "+ Building aab output: ${aab_output} - ${branch_name}"
	@./gradlew clean bundleRelease
	@apksigner sign --ks app/keys/app-release-key.jks \
		--ks-pass pass:$(pass) \
		--in app/build/outputs/bundle/release/app-release.aab \
		--out apk-outputs/${aab_output} \
		--min-sdk-version ${minSdk}
	@shasum apk-outputs/${aab_output}

test-flight:
	# check if builds can be signed
	@if [ ! -f "app/keys/app-release-key.jks" ]; then \
		echo "+ [ERROR] app/keys/app-release-key.jks file not found for signing"; \
		exit 1; \
	else \
		python3 -m venv venv; \
		( \
			. venv/bin/activate; \
			pip3 install -r requirements.txt; \
			python3 release.py --github_url "${github_url}"; \
		) \
	fi

release-cd: test-flight clean requirements.txt bump_version docker-build-aab clean
	@echo "+ Target branch for relase: ${branch}"
	@git tag -f ${tagVersion}
	@git push origin ${branch_name}
	@git push --tag
	@python3 -m venv venv
	@( \
		. venv/bin/activate; \
		pip3 install -r requirements.txt; \
		python3 release.py \
			--version_code ${tagVersion} \
			--version_name ${label} \
			--description "<b>Release</b>: ${label}<br><b>Build No</b>: ${tagVersion}<br><b>shasum</b>: $$(shasum apk-outputs/$(apk_output))" \
			--branch ${branch} \
			--track ${track} \
			--app_bundle_file apk-outputs/${aab_output} \
			--app_apk_file apk-outputs/${apk_output} \
			--status "completed" \
			--platform "all" \
			--github_url "${github_url}" \
	)


clean:
	@containers=$$(docker ps -a --filter "ancestor=$(docker_apk_image)" --format "{{.ID}}"); \
		if [ -n "$$containers" ]; then \
		    docker stop $$containers; \
		    docker rm $$containers; \
		fi
	@containers=$$(docker ps -a --filter "ancestor=$(docker_app_image)" --format "{{.ID}}"); \
		if [ -n "$$containers" ]; then \
		    docker stop $$containers; \
		    docker rm $$containers; \
		fi
	@containers=$$(docker ps -a --filter "ancestor=$(docker_apk_image_commit_check)" --format "{{.ID}}"); \
		if [ -n "$$containers" ]; then \
		    docker stop $$containers; \
		    docker rm $$containers; \
		fi
	@echo "y" | docker builder prune -a
	@echo "y" | docker image prune -a
