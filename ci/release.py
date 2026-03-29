#!/usr/bin/python3
import sys
import logging
import httplib2
import argparse

logging.basicConfig(level=logging.INFO)


class RelGooglePlaystore:
    def create_edit_for_draft_release(
        self,
        version_code,
        version_name,
        description,
        bundle_file,
        credentials_file_path,
        package_name,
        status='draft',
        track='internal',
        timeout_seconds=600,
        changesNotSentForReview=True
    ):
        from googleapiclient.discovery import build
        from google.oauth2 import service_account
        from googleapiclient.http import MediaFileUpload

        http = httplib2.Http(timeout=timeout_seconds)
        credentials = service_account.Credentials.from_service_account_file(
            credentials_file_path,
            scopes=['https://www.googleapis.com/auth/androidpublisher']
        )
        credentials = credentials.with_quota_project(None)

        service = build(
            serviceName='androidpublisher',
            version='v3',
            credentials=credentials,
            num_retries=5
        )

        # Create a new edit
        edit_request = service.edits().insert(packageName=package_name)
        edit_response = edit_request.execute()
        edit_id = edit_response['id']
        logging.info("[Playstore] Edit created with id: %s", edit_id)

        # Upload the AAB
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
        logging.info("[Playstore] AAB uploaded with version code: %d", bundle_version_code)

        # Update the track with the release
        release_body = [
            {
                'name': version_name,
                'status': status,
                'versionCodes': [bundle_version_code]
            }
        ]
        track_request = service.edits().tracks().update(
            packageName=package_name,
            editId=edit_id,
            track=track,
            body={'releases': release_body}
        )
        track_response = track_request.execute()
        logging.info("[Playstore] %s release '%s' created on track '%s'", status, version_name, track)

        # Commit the edit
        commit_request = service.edits().commit(
            packageName=package_name,
            editId=edit_id,
            changesNotSentForReview=changesNotSentForReview
        )
        commit_request.execute()
        logging.info("[Playstore] Edit committed and finalized.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Release an AAB to the Google Play Store")
    parser.add_argument("--version_code", type=int, required=True, help="The version code of the app")
    parser.add_argument("--version_name", type=str, required=True, help="The version name of the app")
    parser.add_argument("--description", type=str, required=True, help="The description of the release")
    parser.add_argument("--track", type=str, required=True, help="The track to release to (e.g. internal)")
    parser.add_argument("--app_bundle_file", type=str, required=True, help="Path to the AAB file")
    parser.add_argument("--status", type=str, required=True, help="Release status (e.g. draft)")
    parser.add_argument("--creds_filepath", type=str, required=True, help="Path to the service account JSON credentials")
    parser.add_argument("--package_name", type=str, required=True, help="The app package name")
    args = parser.parse_args()

    rel_playstore = RelGooglePlaystore()
    rel_playstore.create_edit_for_draft_release(
        version_code=args.version_code,
        version_name=args.version_name,
        description=args.description,
        bundle_file=args.app_bundle_file,
        credentials_file_path=args.creds_filepath,
        package_name=args.package_name,
        status=args.status,
        track=args.track,
    )
