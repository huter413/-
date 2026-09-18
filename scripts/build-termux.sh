#!/data/data/com.termux/files/usr/bin/bash
set -e
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"
if [ -x "./gradlew" ]; then
  ./gradlew :app:assembleDebug
else
  gradle :app:assembleDebug
fi
mkdir -p "$HOME/storage/downloads/Mojolauncher-Android"
cp -f app/build/outputs/apk/debug/app-debug.apk "$HOME/storage/downloads/Mojolauncher-Android/Mojolauncher-debug.apk"
echo "APK: $HOME/storage/downloads/Mojolauncher-Android/Mojolauncher-debug.apk"
