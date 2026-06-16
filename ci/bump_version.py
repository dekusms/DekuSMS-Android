#!/usr/bin/env python3
import sys

def bump_version(filename, flavour):
    with open(filename, "rb") as f:  # open as binary to see raw bytes
        raw = f.read()

    print(f"DEBUG: raw file bytes: {repr(raw)}", file=sys.stderr)

    lines = raw.decode("utf-8-sig").splitlines()  # utf-8-sig strips BOM if present

    print(f"DEBUG: lines: {lines}", file=sys.stderr)

    releaseVersion = None
    stagingVersion = None
    nightlyVersion = None
    tagVersion = None  # Now parsed directly from the file

    for line in lines:
        line = line.strip()
        print(f"DEBUG: processing line: {repr(line)}", file=sys.stderr)
        if not line or "=" not in line:
            continue
        key, _, value = line.partition("=")
        key = key.strip()
        value = value.strip()
        print(f"DEBUG: key={repr(key)} value={repr(value)}", file=sys.stderr)
        if key == "releaseVersion":
            releaseVersion = value
        elif key == "stagingVersion":
            stagingVersion = value
        elif key == "nightlyVersion":
            nightlyVersion = value
        elif key == "tagVersion":
            tagVersion = value

    print(f"DEBUG: releaseVersion={releaseVersion}, stagingVersion={stagingVersion}, nightlyVersion={nightlyVersion}, tagVersion={tagVersion}", file=sys.stderr)

    if releaseVersion is None:
        raise ValueError("Could not find releaseVersion in file")
    if stagingVersion is None:
        raise ValueError("Could not find stagingVersion in file")
    if nightlyVersion is None:
        raise ValueError("Could not find nightlyVersion in file")
    if tagVersion is None:
        raise ValueError("Could not find tagVersion in file")

    releaseVersion = int(releaseVersion)
    stagingVersion = int(stagingVersion)
    nightlyVersion = int(nightlyVersion)
    tagVersion = int(tagVersion)

    # Determine X.Y.Z increments by branch tier
    if flavour in ("refs/heads/master", "master"):
        releaseVersion += 1
        stagingVersion = 0
        nightlyVersion = 0
    elif flavour in ("refs/heads/staging", "staging"):
        stagingVersion += 1
        nightlyVersion = 0
    else:
        nightlyVersion += 1

    # Monotonic increment for Google Play versionCode tracking
    tagVersion += 1

    return (
        f"releaseVersion={releaseVersion}\n"
        f"stagingVersion={stagingVersion}\n"
        f"nightlyVersion={nightlyVersion}\n"
        f"versionName={releaseVersion}.{stagingVersion}.{nightlyVersion}\n"
        f"tagVersion={tagVersion}"
    )

if __name__ == "__main__":
    filename = "version.properties"
    
    # We only need the branch/flavour parameter now!
    if len(sys.argv) < 2:
        print("ERROR: Missing branch/flavour argument.", file=sys.stderr)
        sys.exit(1)
        
    flavour = sys.argv[1]
    print(f"DEBUG: flavour={repr(flavour)}, filename={repr(filename)}", file=sys.stderr)
    
    version = bump_version(filename, flavour)
    print(version)
