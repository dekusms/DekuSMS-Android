#!/usr/bin/env python3
import sys

def bump_version(tagVersion, filename, flavour):
    with open(filename, "rb") as f:  # open as binary to see raw bytes
        raw = f.read()

    print(f"DEBUG: raw file bytes: {repr(raw)}", file=sys.stderr)

    lines = raw.decode("utf-8-sig").splitlines()  # utf-8-sig strips BOM if present

    print(f"DEBUG: lines: {lines}", file=sys.stderr)

    releaseVersion = None
    stagingVersion = None
    nightlyVersion = None

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

    print(f"DEBUG: releaseVersion={releaseVersion}, stagingVersion={stagingVersion}, nightlyVersion={nightlyVersion}", file=sys.stderr)

    if releaseVersion is None:
        raise ValueError("Could not find releaseVersion in file")
    if stagingVersion is None:
        raise ValueError("Could not find stagingVersion in file")
    if nightlyVersion is None:
        raise ValueError("Could not find nightlyVersion in file")
    if tagVersion is None:
        raise ValueError("Could not find tagVersion in args")

    releaseVersion = int(releaseVersion)
    stagingVersion = int(stagingVersion)
    nightlyVersion = int(nightlyVersion)
    tagVersion = int(tagVersion)

    if flavour in ("refs/heads/master", "master"):
        releaseVersion += 1
        stagingVersion = 0
        nightlyVersion = 0
    elif flavour in ("refs/heads/staging", "staging"):
        stagingVersion += 1
        nightlyVersion = 0
    else:
        nightlyVersion += 1

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
    tag = sys.argv[1]
    flavour = sys.argv[2]
    print(f"DEBUG: tag={repr(tag)}, flavour={repr(flavour)}, filename={repr(filename)}", file=sys.stderr)
    version = bump_version(tag, filename, flavour)
    print(version)
