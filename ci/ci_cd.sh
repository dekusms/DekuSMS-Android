#!/bin/bash

rm -rf Deku-SMS-Android
git clone --recurse-submodules -j8 git@github.com:deku-messaging/Deku-SMS-Android.git
cd Deku-SMS-Android && \
	git checkout staging && \
	cp -v ../../release.properties . && \
	mkdir -p app/keys/ && \
	cp -v ../../app/keys/app-release-key.jks app/keys/ && \
	cp -v ../../ks.passwd . && \
	make clean && \
	python3 -m venv venv && \
	( \
	. venv/bin/activate && \
	pip install -r requirements.txt && \
	make release-cd status="draft")
