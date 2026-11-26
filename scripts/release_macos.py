#!/usr/bin/env python3
"""Builds the macOS app, tags it, and publishes a GitHub release with a DMG.

One command per release, so the version, the bundle, the tag and the asset never drift apart:

    scripts/release_macos.py 0.0.9            # build, commit the version bump, tag, push, release
    scripts/release_macos.py 0.0.9 --dry-run  # everything up to (and including) the DMG, then stop

What it does, in order:

1. Sets CFBundleShortVersionString/CFBundleVersion in macosApp's Info.plist.
2. Runs `:macosApp:bundleApp`, which links the Kotlin/Native binary and assembles Pebble.app.
3. Ad-hoc signs the bundle, packs Pebble.app + an /Applications link into a compressed DMG, and
   verifies the image.
4. Renders the release notes (macosApp/release-notes.md, with {{VERSION}}, {{DMG_NAME}} and
   {{SHA256}} substituted).
5. Commits the version bump, creates the annotated tag `v<version>`, pushes both.
6. Creates the GitHub release and uploads the DMG, then checks the asset landed.

Requires: Java 17+ for Gradle, Xcode command line tools, and `gh` logged in for the upload
(`--no-upload` skips that part). Nothing here notarizes the app, so the DMG is for internal testers;
the notes tell them how to get past Gatekeeper.
"""

from __future__ import annotations

import argparse
import hashlib
import os
import plistlib
import re
import shutil
import subprocess
import sys
import tempfile
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parent.parent
INFO_PLIST = REPO_ROOT / "macosApp/src/macosMain/resources/Info.plist"
NOTES_TEMPLATE = REPO_ROOT / "macosApp/release-notes.md"
APP_BUNDLE = REPO_ROOT / "macosApp/build/macosApp/Pebble.app"
DEFAULT_BRANCH = "master"


class ReleaseError(RuntimeError):
    pass


def run(cmd: list[str], *, capture: bool = False, check: bool = True, env: dict | None = None) -> str:
    printable = " ".join(cmd)
    print(f"  $ {printable}", flush=True)
    result = subprocess.run(
        cmd,
        cwd=REPO_ROOT,
        text=True,
        capture_output=capture,
        env={**os.environ, **(env or {})},
    )
    if check and result.returncode != 0:
        detail = (result.stderr or result.stdout or "").strip()
        raise ReleaseError(f"{printable} failed" + (f":\n{detail}" if detail else ""))
    return (result.stdout or "") if capture else ""


def git(*args: str, capture: bool = False) -> str:
    return run(["git", *args], capture=capture)


# ---------------------------------------------------------------- preflight


def parse_version(raw: str) -> tuple[str, int]:
    """`0.0.9` -> ("0.0.9", 9). CFBundleVersion must be a single increasing integer."""
    value = raw[1:] if raw.startswith("v") else raw
    if not re.fullmatch(r"\d+\.\d+\.\d+", value):
        raise ReleaseError(f"version must look like 0.0.9, got {raw!r}")
    major, minor, patch = (int(part) for part in value.split("."))
    return value, major * 10000 + minor * 100 + patch


def preflight(version: str, *, upload: bool) -> None:
    print("Checking prerequisites", flush=True)
    if git("status", "--porcelain", capture=True).strip():
        raise ReleaseError("working tree is dirty; commit or stash first")
    branch = git("rev-parse", "--abbrev-ref", "HEAD", capture=True).strip()
    if branch != DEFAULT_BRANCH:
        print(f"  ! on branch {branch}, releases are normally cut from {DEFAULT_BRANCH}")
    if git("tag", "--list", f"v{version}", capture=True).strip():
        raise ReleaseError(f"tag v{version} already exists")

    if shutil.which("xcodebuild") is None:
        raise ReleaseError("Xcode command line tools are missing (xcode-select --install)")
    if shutil.which("gh") is None and upload:
        raise ReleaseError("`gh` is missing; install it or pass --no-upload")
    if upload:
        try:
            run(["gh", "auth", "status"], capture=True)
        except ReleaseError as error:
            raise ReleaseError("`gh auth login` first, or pass --no-upload") from error
    if "JAVA_HOME" not in os.environ:
        print("  ! JAVA_HOME is not set; Gradle needs Java 17+")


def gradlew(*args: str, env: dict | None = None) -> None:
    run([str(REPO_ROOT / "gradlew"), *args], env=env)


# ---------------------------------------------------------------- version


def write_bundle_version(version: str, build: int) -> bool:
    """Rewrites just the two version keys, so the diff stays one hunk. Returns True if changed."""
    text = INFO_PLIST.read_text()
    updated = re.sub(
        r"(<key>CFBundleShortVersionString</key>\s*<string>)[^<]*(</string>)",
        rf"\g<1>{version}\g<2>",
        text,
    )
    updated = re.sub(
        r"(<key>CFBundleVersion</key>\s*<string>)[^<]*(</string>)",
        rf"\g<1>{build}\g<2>",
        updated,
    )
    if updated == text:
        print(f"  Info.plist already at {version} ({build})")
        return False
    INFO_PLIST.write_text(updated)
    print(f"  Info.plist -> {version} ({build})")
    return True


def verify_bundle_version(version: str) -> None:
    with APP_BUNDLE.joinpath("Contents/Info.plist").open("rb") as fh:
        plist = plistlib.load(fh)
    found = plist.get("CFBundleShortVersionString")
    if found != version:
        raise ReleaseError(f"{APP_BUNDLE} reports version {found!r}, expected {version!r}")


# ---------------------------------------------------------------- dmg


def make_dmg(version: str, workdir: Path) -> tuple[Path, str]:
    if not APP_BUNDLE.is_dir():
        raise ReleaseError(f"{APP_BUNDLE} is missing; the bundle step did not produce it")
    stage = workdir / "stage"
    stage.mkdir()
    shutil.copytree(APP_BUNDLE, stage / "Pebble.app", symlinks=True)
    (stage / "Applications").symlink_to("/Applications")

    # bundleApp already ad-hoc signs, but copying can invalidate the sealed resources.
    run(
        [
            "codesign", "--force", "--sign", "-", "--timestamp=none",
            "--preserve-metadata=identifier,entitlements,flags",
            str(stage / "Pebble.app"),
        ]
    )

    dmg_name = f"Pebble-{version}-macos-arm64.dmg"
    dmg = workdir / dmg_name
    run(
        [
            "hdiutil", "create", "-volname", f"Pebble {version}", "-srcfolder", str(stage),
            "-ov", "-format", "UDZO", "-fs", "HFS+", str(dmg),
        ],
        capture=True,
    )
    run(["hdiutil", "verify", str(dmg)], capture=True)
    digest = hashlib.sha256(dmg.read_bytes()).hexdigest()
    print(f"  {dmg_name}: {dmg.stat().st_size / 1e6:.1f} MB, sha256 {digest}")
    return dmg, digest


# ---------------------------------------------------------------- notes


def render_notes(version: str, dmg_name: str, digest: str, workdir: Path) -> Path:
    if not NOTES_TEMPLATE.is_file():
        raise ReleaseError(f"{NOTES_TEMPLATE} is missing")
    text = NOTES_TEMPLATE.read_text()
    for placeholder, value in (("{{VERSION}}", version), ("{{DMG_NAME}}", dmg_name), ("{{SHA256}}", digest)):
        text = text.replace(placeholder, value)
    leftover = re.findall(r"\{\{\w+\}\}", text)
    if leftover:
        raise ReleaseError(f"release notes still contain {', '.join(sorted(set(leftover)))}")
    notes = workdir / "release-notes.md"
    notes.write_text(text)
    return notes


# ---------------------------------------------------------------- publish


def release_repo() -> str:
    url = git("remote", "get-url", "origin", capture=True).strip()
    match = re.search(r"github\.com[:/](?P<repo>[^/]+/[^/]+?)(?:\.git)?$", url)
    if not match:
        raise ReleaseError(f"could not work out the GitHub repo from origin ({url})")
    return match.group("repo")


def publish(version: str, title: str, dmg: Path, notes: Path, repo: str) -> str:
    git("add", str(INFO_PLIST.relative_to(REPO_ROOT)))
    git("commit", "-m", f"Release macOS {version}")
    git("tag", "-a", f"v{version}", "-m", title)
    git("push", "origin", f"HEAD:{DEFAULT_BRANCH}")
    git("push", "origin", f"v{version}")

    url = run(
        [
            "gh", "release", "create", f"v{version}", str(dmg),
            "--repo", repo, "--title", title, "--notes-file", str(notes),
        ],
        capture=True,
    ).strip().splitlines()[-1]

    assets = run(["gh", "release", "view", f"v{version}", "--repo", repo, "--json", "assets"], capture=True)
    if dmg.name not in assets:
        raise ReleaseError(f"release {url} does not list {dmg.name}")
    print(f"  verified asset {dmg.name} on {url}")
    return url


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Build, tag and publish a macOS DMG release.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog=__doc__,
    )
    parser.add_argument("version", help="release version, e.g. 0.0.9 (a leading v is accepted)")
    parser.add_argument("--notes", help=f"release notes template (default {NOTES_TEMPLATE.relative_to(REPO_ROOT)})")
    parser.add_argument("--repo", help="owner/name to publish to (default: derived from origin)")
    parser.add_argument("--no-upload", action="store_true", help="stop after pushing the tag")
    parser.add_argument("--dry-run", action="store_true", help="build and pack, but do not commit, tag or publish")
    parser.add_argument("--skip-build", action="store_true", help="reuse the existing Pebble.app")
    args = parser.parse_args()

    template = Path(args.notes).resolve() if args.notes else NOTES_TEMPLATE

    version, build = parse_version(args.version)
    title = f"Pebble for macOS {version} (arm64)"
    upload = not args.no_upload and not args.dry_run

    preflight(version, upload=upload)
    print(f"Releasing {title}", flush=True)
    write_bundle_version(version, build)

    if not args.skip_build:
        print("Building the app bundle", flush=True)
        gradlew(":macosApp:bundleApp", "--console=plain")
    verify_bundle_version(version)

    workdir = Path(tempfile.mkdtemp(prefix="pebble-release-"))
    try:
        print("Packing the DMG", flush=True)
        dmg, digest = make_dmg(version, workdir)
        notes = render_notes(version, dmg.name, digest, workdir)

        if args.dry_run:
            # Keep the artifacts somewhere the caller can inspect; the scratch dir is about to go.
            keep = REPO_ROOT / "macosApp/build/release"
            keep.mkdir(parents=True, exist_ok=True)
            kept_dmg = Path(shutil.copy2(dmg, keep / dmg.name))
            kept_notes = Path(shutil.copy2(notes, keep / notes.name))
            print("\nDry run: stopping before commit/tag/push/release. Artifacts:")
            print(f"  DMG:   {kept_dmg}")
            print(f"  notes: {kept_notes}")
            print(f"  would tag v{version} and publish to {args.repo or release_repo()}")
            return 0

        if not upload:
            git("add", str(INFO_PLIST.relative_to(REPO_ROOT)))
            git("commit", "-m", f"Release macOS {version}")
            git("tag", "-a", f"v{version}", "-m", title)
            git("push", "origin", f"HEAD:{DEFAULT_BRANCH}")
            git("push", "origin", f"v{version}")
            print(f"\nPushed tag v{version}. DMG left at {dmg} (upload skipped).")
            return 0

        print("Publishing", flush=True)
        url = publish(version, title, dmg, notes, args.repo or release_repo())
        print(f"\nReleased {url}")
        print(f"  {dmg.name}  sha256 {digest}")
        return 0
    finally:
        shutil.rmtree(workdir, ignore_errors=True)


if __name__ == "__main__":
    try:
        sys.exit(main())
    except ReleaseError as error:
        print(f"\nerror: {error}", file=sys.stderr)
        sys.exit(1)
