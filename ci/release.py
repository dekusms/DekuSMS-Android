#!/usr/bin/python3
import re
import requests
import json
import sys, os
import logging
import httplib2
import argparse
import threading


class RelGooglePlaystore:
    def create_edit_for_draft_release(
        self, 
        version_code, 
        version_name, 
        description, 
        bundle_file,
        status='draft',
        track='internal', 
        timeout_seconds=600,
        credentials_file_path,
        package_name,
        changesNotSentForReview = True
    ):
        """
        """
        from googleapiclient.discovery import build
        from google.oauth2 import service_account 
        from googleapiclient.http import MediaFileUpload  # Import MediaFileUpload

        credentials_file_path = None
        package_name = None

        http = httplib2.Http(timeout=timeout_seconds)

        credentials = service_account.Credentials.from_service_account_file(credentials_file_path)
        credentials.http = http

        service = build(
            serviceName='androidpublisher', 
            version='v3', 
            credentials=credentials,
            num_retries=5
        )

        edit_request = service.edits().insert(packageName=package_name)
        edit_response = edit_request.execute()
        edit_id = edit_response['id']

        # Create a media upload request
        media_upload = MediaFileUpload(
            bundle_file,                           
            mimetype="application/octet-stream", 
            resumable=True
        )

        bundle_response = service.edits().bundles().upload(
            packageName=package_name, 
            editId=edit_id, 
            media_body=media_upload
        ).execute()

        bundle_version_code = bundle_response['versionCode']

        release_body = [
            {
                'name':version_name,
                'status':status,
                'versionCodes':[version_code]
            }
        ]

        track_request = service.edits().tracks().update(
            packageName=package_name,
            editId=edit_id,
            track=track,
            body={'releases': release_body}
        )

        response = track_request.execute()
        logging.info("[Playstore] %s release %s created with version code %d", status, version_name, version_code)

        # Commit the changes to finalize the edit
        commit_request = service.edits().commit(
            packageName=package_name,
            editId=edit_id
        )
        commit_request.execute()

        logging.info("[Playstore] Changes committed and edit finalized.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="An argument parser for Python")

    parser.add_argument("--version_code", type=int, required=True, help="The version code of the app")
    parser.add_argument("--version_name", type=str, required=True, help="The version name of the app")
    parser.add_argument("--description", type=str, required=True, help="The description of the app")
    parser.add_argument("--track", type=str, required=True, help="The track of the app")
    parser.add_argument("--app_bundle_file", type=str, required=True, help="The app bundle file")
    parser.add_argument("--status", type=str, required=True, help="The app release status")
    parser.add_argument("--creds_filepath", type=str, required=True, help="Path to playstore creds")
    parser.add_argument("--package_name", type=str, required=True, help="name the package")


    args = parser.parse_args()
    rel_playstore.create_edit_for_draft_release(
        args.version_code, 
        args.version_name, 
        args.description, 
        args.app_bundle_file, 
        args.status, 
        args.track, 
        args.creds_filepath,
        args.package_name,
        True,
    )
