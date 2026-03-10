#!/usr/bin/env python3
import os
import sys

def bump_version(tagVersion, filename, flavour):
    """
    Bumps the version number in the specified file.

    Args:
    filename: The name of the file to update.

    Returns:
    The new version number.
    """

    with open(filename, "r") as f:
        lines = f.readlines()

    releaseVersion = None
    stagingVersion = None
    nightlyVersion = None

    for line in lines:
        if line.startswith("releaseVersion="): 
            releaseVersion = line.split("=")[1].strip() 

        if line.startswith("stagingVersion="): 
            stagingVersion = line.split("=")[1].strip() 

        if line.startswith("nightlyVersion="): 
            nightlyVersion = line.split("=")[1].strip() 

        """
        if line.startswith("tagVersion="): 
            tagVersion = line.split("=")[1].strip() 
        """

    if releaseVersion is None:
        raise ValueError("Could not find releaseVersion in file")

    if stagingVersion is None:
        raise ValueError("Could not find stagingVersion in file")

    if nightlyVersion is None:
        raise ValueError("Could not find nightlyVersion in file")

    if tagVersion is None:
        raise ValueError("Could not find tagVersion in file")

    if flavour == "refs/heads/master":
        releaseVersion = int(releaseVersion) + 1
        stagingVersion = 0
        nightlyVersion = 0

    elif flavour == "refs/heads/staging":
        stagingVersion = int(stagingVersion) + 1
        nightlyVersion = 0

    else:
        nightlyVersion = int(nightlyVersion) + 1

    tagVersion = int(tagVersion) + 1

    return f"""releaseVersion={str(releaseVersion)}
stagingVersion={str(stagingVersion)}
nightlyVersion={str(nightlyVersion)}
versionName={str(releaseVersion)}.{str(stagingVersion)}.{str(nightlyVersion)}
tagVersion={str(tagVersion)}"""


if __name__ == "__main__":
  filename = "version.properties"

  tag = sys.argv[1]
  flavour = sys.argv[2]
  version = bump_version(tag, filename, flavour)
  print(version)
