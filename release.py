#!/usr/bin/env python3
import re
import requests
import json
import sys, os
import logging
import httplib2

from googleapiclient.discovery import build
from google.oauth2 import service_account 


class RelGooglePlaystore:
    def __init__(self):
        self.credentials_file_path= None
        self.package_name= None
        with open('release.properties', 'r') as fd:
            lines = fd.readlines()

        for line in lines:
            if line.startswith('google_playstore_creds_filepath'):
                self.credentials_file_path = line.split("=")[1].strip() 
            elif line.startswith('app_package_name'):
                self.package_name= line.split("=")[1].strip() 
        # Create an HTTP object with a timeout

        http = httplib2.Http(timeout=600)

        credentials = service_account.Credentials.from_service_account_file(self.credentials_file_path)
        credentials.http = http

        self.service = build(serviceName='androidpublisher', version='v3', 
                        credentials=credentials,
                        num_retries=5)

    def flight(self):
        edit_request = self.service.edits().insert(packageName=self.package_name)
        edit_response = edit_request.execute()
        edit_id = edit_response['id']


    def create_edit_for_draft_release(self, 
                                      version_code, 
                                      version_name, 
                                      description, 
                                      bundle_file,
                                      status='draft',
                                      track='internal', 
                                      timeout_seconds=600, 
                                      changesNotSentForReview = True):
        """
        """
        from googleapiclient.http import MediaFileUpload  # Import MediaFileUpload

        credentials_file_path = None

        # return service.edits().insert(editId=release_id, body=edit_body).execute()
        edit_request = self.service.edits().insert(packageName=self.package_name)
        edit_response = edit_request.execute()
        edit_id = edit_response['id']

        # Create a media upload request
        media_upload = MediaFileUpload(bundle_file, 
                                       mimetype="application/octet-stream", resumable=True)

        bundle_response = self.service.edits().bundles().upload(
                packageName=self.package_name, 
                editId=edit_id, 
                media_body=media_upload
            ).execute()

        bundle_version_code = bundle_response['versionCode']

        # Specify the version code for the draft release
        version_code = bundle_version_code  # Use the version code of the uploaded bundle

        # version_code = 26
        release_body = [{
                'name':version_name,
                'status':status,
                'versionCodes':[version_code]
                }]

        track_request = self.service.edits().tracks().update(
                packageName=self.package_name,
                editId=edit_id,
                track=track,
                body={'releases': release_body}
                )

        response = track_request.execute()
        logging.info("[Playstore] %s release %s created with version code %d", status, version_name, version_code)

        # Commit the changes to finalize the edit
        commit_request = self.service.edits().commit(
            packageName=self.package_name,
            editId=edit_id
        )
        commit_request.execute()

        logging.info("[Playstore] Changes committed and edit finalized.")


class RelGithub:
    def __init__(self):
        self.github_token = None

        with open('release.properties', 'r') as fd:
            lines = fd.readlines()

        for line in lines:
            if line.startswith('github_token'):
                self.github_token = line.split("=")[1].strip() 
                break

        self.headers = {"Authorization": "Bearer {}".format(self.github_token), 
                   "X-GitHub-Api-Version": "2022-11-28",
                   "Accept": "application/vnd.github+json"}


    def flight(self, url):
        response = requests.get(url, headers=self.headers)
        response.raise_for_status()

    def create_edit_for_draft_release(self, 
                                      version_code, 
                                      version_name, 
                                      description, 
                                      target_branch, 
                                      status, 
                                      url,
                                      apk_file):
        # Create a new release on GitHub.

        status = True if status == 'draft' else False

        data = {
            "tag_name": str(version_code),
            "name": version_name,
            "body": description,
            "target_commitish": target_branch,
            "draft": status,
            "prerelease":False,
            "generate_release_notes":False
        }
        logging.info(data)

        response = requests.post(url, json=data, headers=self.headers)
        response.raise_for_status()

        logging.info("[GitHub] Create new release: %d", response.status_code)
        response = json.loads(response.text)
        upload_url = response['upload_url']

        # Upload assets to a new release on GitHub.
        headers = {'Content-Type': 'application/octet-stream', 
                   "Authorization": "Bearer {}".format(self.github_token), 
                   "X-GitHub-Api-Version": "2022-11-28",
                   "Accept": "application/vnd.github+json"}

        upload_url = re.sub(r"\{\?name,label}", "", upload_url)

        params = {
            'name': os.path.basename(apk_file),
            'label': version_name
        }

        with open(apk_file, 'rb') as f:
            data = f.read()

        response = requests.post(upload_url, headers=headers, data=data, params=params)
        response.raise_for_status()

        logging.info("[GitHub] Create upload release: %d", response.status_code)
        # return json.loads(response.text)


def test_flight(url):
    print("-- Beginning flight test")
    try:
        # url = "https://api.github.com/repos/dekusms/DekuSMS-Android/releases"
        print("Github url:", url)
        rel_github = RelGithub()
        rel_github.flight(url)
        print("++ Github passed!")

    except Exception as error:
        logging.exception(error)
        exit("-- Github failed")
    else:
        try:
            rel_playstore = RelGooglePlaystore()
            rel_playstore.flight()
            print("++ Playstore passed!")

        except Exception as error:
            logging.exception(error)
            exit("-- Playstore failed")


if __name__ == "__main__":
    import argparse
    import threading

    parser = argparse.ArgumentParser(description="An argument parser for Python")

    parser.add_argument("--version_code", type=int, required=False, help="The version code of the app")
    parser.add_argument("--version_name", type=str, required=False, help="The version name of the app")
    parser.add_argument("--description", type=str, required=False, help="The description of the app")
    parser.add_argument("--branch", type=str, required=False, help="The branch of the app")
    parser.add_argument("--track", type=str, required=False, help="The track of the app")
    parser.add_argument("--app_bundle_file", type=str, required=False, help="The app bundle file")
    parser.add_argument("--app_apk_file", type=str, required=False, help="The app APK file")
    parser.add_argument("--status", type=str, required=False, help="The app release status")
    parser.add_argument("--github_url", type=str, required=False, help="The github repo URL")
    parser.add_argument("--log_level", type=str, default='INFO', required=False, help="The level of the log")
    parser.add_argument("--platform", type=str, default="test-flight", required=False, help="Platform to be released on: \
            playstore, github")

    args = parser.parse_args()

    rel_playstore = RelGooglePlaystore()
    thread_playstore = threading.Thread(target=rel_playstore.create_edit_for_draft_release, args=(
        args.version_code, args.version_name, args.description, args.app_bundle_file, args.status, args.track, True,))

    rel_github = RelGithub()
    thread_github = threading.Thread(target=rel_github.create_edit_for_draft_release, args=(
        args.version_code, args.version_name, args.description, args.branch, 
        args.status, args.github_url, args.app_apk_file,))

    logging.basicConfig(level=args.log_level)

    if args.platform == "test-flight":
        test_flight(args.github_url)

    elif args.platform == "all":
        thread_playstore.start()
        thread_playstore.join()

        thread_github.start()
        thread_github.join()

    elif args.platform == "playstore":
        thread_playstore.start()
        thread_playstore.join()

    elif args.platform == "github":
        thread_github.start()
        thread_github.join()

