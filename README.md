# Mojolauncher-Android

A phone-first Android launcher shell for Minecraft: Java Edition profiles.

> Project note: this repository does not bundle Mojang/Microsoft game binaries. You add your own legally obtained game/runtime files. The launcher names imported versions as Minecraft_<version>.jar.

## What is included

- Native Android Kotlin launcher UI (landscape-friendly)
- Version scanner for Minecraft_<version>.jar
- Version selector and per-version profile settings
- Import game JAR and mod JAR/ZIP files through Android's document picker
- Per-version mods/ storage
- Profile presets with RAM, FPS cap, resolution scale and JVM arguments
- Touch-control settings storage: joystick/camera sensitivity, button opacity and left-handed mode
- Performance settings storage
- Crash/log viewer for launcher-side process output
- Local Java runtime discovery under the app's runtimes/ directory
- Launch plan generation: java -jar Minecraft_<version>.jar by default, with optional launch.json for an explicit main class/classpath
- GitHub Actions debug APK build
- scripts/build-termux.sh for building from Termux with system Gradle
- Optional external-engine bridge: the settings UI can open an installed Java launcher if its package is configured

## Important launcher limitation

Minecraft: Java Edition is not a self-contained Android executable. Modern versions normally need a compatible Java runtime, LWJGL/graphics/audio integration, libraries, assets and version metadata. This project therefore keeps the game/runtime files outside the Git repository and provides a clean launcher/profile layer around them.

For a full Android game backend, this project is designed to be paired with an open-source Android Java launcher/runtime such as Amethyst/PojavLauncher rather than redistributing proprietary Minecraft binaries.

## Version file layout

~~~text
Minecraft/
  versions/
    1.20.1/
      Minecraft_1.20.1.jar
      launch.json                 # optional
      mods/
  runtimes/
    java-17/
      bin/java
  logs/
  profiles.json
~~~

## Build

### GitHub Actions

Push to GitHub. The workflow at .github/workflows/android.yml builds app-debug.apk.

### Termux

~~~bash
pkg update
pkg install git openjdk-17
git clone https://github.com/huter413/- ~/Mojolauncher-Android
cd ~/Mojolauncher-Android
bash scripts/build-termux.sh
~~~

The APK is copied to Download/Mojolauncher-Android/Mojolauncher-debug.apk when the Android SDK/Gradle environment is available.

## License

Launcher code in this repository is MIT licensed. Third-party runtimes/engines remain under their own licenses. Minecraft itself is proprietary and is not included.
