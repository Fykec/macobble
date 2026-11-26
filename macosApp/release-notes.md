## Pebble for macOS {{VERSION}} (arm64)

Early macOS build of the Pebble app: the same shared Compose UI as the mobile apps, talking to a
Pebble watch over BLE.

**Requires macOS 14 or later, Apple Silicon (arm64).**

### What works

- Connecting a Pebble watch over Bluetooth LE
- Notifications, and sync of time, weather, calendar and health data
- Installing watchapps and watchfaces from the store
- Watchapp config pages, shown in a sheet with an embedded WebKit view
- Permissions, settings, diagnostics

### Not included yet

- Pebble Index 01 (the ring) — that module is Android/iOS only
- Plugins, and anything that depends on the ring
- Firebase-backed features (bug reporting, analytics, cloud transcription) — stubbed out

### Installing

1. Open `{{DMG_NAME}}` and drag **Pebble** into *Applications*.
2. This build is **ad-hoc signed, not notarized**, so macOS will refuse to open it on first launch
   ("Apple could not verify…"). Fix it in one of two ways:
   - Right-click the app in Finder → **Open** → **Open** again, or
   - Clear the download quarantine flag:

     ```bash
     xattr -dr com.apple.quarantine /Applications/Pebble.app
     ```

### Checksum

```
{{SHA256}}  {{DMG_NAME}}
```

### Debugging

- Log file: `~/Library/Logs/CoreApp/macos.log`
- On-screen log panel: *Settings → Debug → Show debug log panel* (off by default)
- Verbose connection logging: *Settings → Diagnostics*
- To see a Kotlin stack trace on a crash, run the binary from a terminal:
  `Pebble.app/Contents/MacOS/Pebble`

### Known limitations

- No in-app bug reporting on macOS yet; attach `macos.log` to an issue instead.
- The window remembers its size and position; if it opens on a different display, drag it once and
  it will stay there.
