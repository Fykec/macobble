#!/usr/bin/env python3
"""Verifies that the Compose resources are fully localized.

1. Every key in values/strings.xml has a matching entry in values-zh/strings.xml.
2. No user-facing string literal is left inline in shared UI code.

Exits non-zero when something is missing, so it can gate CI.
"""

from __future__ import annotations

import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
MODULES = ("util", "pebble", "experimental", "composeApp")

# Deliberately not localized:
#   - developer-facing screens (MCP setup, debug output) sit next to English field names;
#   - WatchNotifications builds text the watch renders, in the watch's own language.
EXCLUDED_PATHS = (
    "/ui/screens/settings/mcp/",
    "HttpServerEditDialog.kt",
    "AddIntegration.kt",
    "DebugFirmwareSideload.kt",
    "DebugLogOverlay.kt",
    "DebugLogs.kt",
    "RawBleScan.kt",
    "WatchNotifications.kt",
)

UI_STRING_PATTERN = re.compile(
    r'(?:Text\(\s*"'
    r"|(?:text|title|label|contentDescription|placeholder|supportingText|message)\s*=\s*\")"
)
STRING_LITERAL = re.compile(r'"((?:[^"\\]|\\.)*)"')


def is_prose(literal: str) -> bool:
    """True when the literal looks like human text rather than a format or symbol."""
    if len(re.findall(r"[A-Za-z]", literal)) < 2:
        return False
    if literal.startswith("%") and " " not in literal:
        return False
    return True


def preview_ranges(source: str) -> list[tuple[int, int]]:
    """Ranges of @Preview functions; their sample text is not shipped UI."""
    ranges: list[tuple[int, int]] = []
    for annotation in re.finditer(r"@(?:[\w.]+\.)?Preview\b", source):
        brace = source.find("{", annotation.end())
        if brace == -1:
            continue
        depth = 0
        for index in range(brace, len(source)):
            char = source[index]
            if char == "{":
                depth += 1
            elif char == "}":
                depth -= 1
                if depth == 0:
                    ranges.append((brace, index))
                    break
    return ranges


def resource_keys(path: Path) -> tuple[set[str], set[str]]:
    if not path.exists():
        return set(), set()
    root = ET.parse(path).getroot()
    strings = {el.get("name") for el in root.iter("string") if el.get("name")}
    plurals = {el.get("name") for el in root.iter("plural") if el.get("name")}
    return strings, plurals


def check_key_parity() -> bool:
    print("== key parity ==")
    ok = True
    for module in MODULES:
        base = ROOT / module / "src/commonMain/composeResources/values/strings.xml"
        translated = (
            ROOT / module / "src/commonMain/composeResources/values-zh/strings.xml"
        )
        if not base.exists():
            continue

        en_strings, en_plurals = resource_keys(base)
        if not en_strings and not en_plurals:
            print(f"  {module}: no keys")
            continue

        if not translated.exists():
            print(f"  {module}:")
            print(f"    ! missing {translated.relative_to(ROOT)}")
            ok = False
            continue

        zh_strings, zh_plurals = resource_keys(translated)
        problems = []
        missing = sorted(en_strings - zh_strings)
        extra = sorted(zh_strings - en_strings)
        missing_plurals = sorted(en_plurals - zh_plurals)
        if missing:
            problems.append(f"untranslated ({len(missing)}): {', '.join(missing[:20])}")
        if extra:
            problems.append(f"missing upstream ({len(extra)}): {', '.join(extra[:20])}")
        if missing_plurals:
            problems.append(f"untranslated plurals: {', '.join(missing_plurals)}")

        if problems:
            print(f"  {module}:")
            for problem in problems:
                print(f"    ! {problem}")
            ok = False
        else:
            print(f"  {module}: {len(en_strings)} keys, all translated")
    return ok


def check_inline_strings() -> bool:
    print()
    print("== inline UI strings ==")
    ok = True
    for module in MODULES:
        source = ROOT / module / "src/commonMain"
        if not source.is_dir():
            continue

        per_file: dict[str, int] = {}
        for path in sorted(source.rglob("*.kt")):
            relative = "/" + str(path.relative_to(ROOT))
            if any(pattern in relative for pattern in EXCLUDED_PATHS):
                continue
            text = path.read_text(encoding="utf-8")
            if not UI_STRING_PATTERN.search(text):
                continue
            skip_ranges = preview_ranges(text)
            count = 0
            offset = 0
            for line in text.splitlines(keepends=True):
                if UI_STRING_PATTERN.search(line):
                    position = offset + line.index('"') if '"' in line else offset
                    in_preview = any(start <= position <= end for start, end in skip_ranges)
                    literal_match = STRING_LITERAL.search(line)
                    if not in_preview and literal_match and is_prose(literal_match.group(1)):
                        count += 1
                offset += len(line)
            if count:
                per_file[str(path.relative_to(ROOT))] = count

        if per_file:
            total = sum(per_file.values())
            print(f"  {module}: {total} left in {len(per_file)} files")
            for name, count in sorted(per_file.items(), key=lambda item: -item[1])[:10]:
                print(f"    {count:4d}  {name}")
            ok = False
    return ok


def main() -> int:
    keys_ok = check_key_parity()
    inline_ok = check_inline_strings()
    print()
    if keys_ok and inline_ok:
        print("localization OK")
        return 0
    return 1


if __name__ == "__main__":
    sys.exit(main())
