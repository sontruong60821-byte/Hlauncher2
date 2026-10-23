# HLauncher source preparation

This first HLauncher preparation uses HyperLauncher HyperX 2.9.4 as the Minecraft Java launcher base. The Android display name is changed to HLauncher and the Gradle root project name is changed to HLauncher. The application namespace/applicationId are intentionally preserved to avoid breaking internal package/resource references during the first build.

## Preserved from HyperLauncher
- Minecraft Java version/instance launch flow
- Mod and modpack management included by the source
- Existing touch controls, keyboard input bridge, pointer capture and mouse handling
- Existing renderer/runtime integration and native build setup

## Dulashi graphics port status
SGSR/ReShade from Winlator Dulashi are NOT copied into this source. Dulashi implements them in a Winlator/XServer Vulkan window post-processing path, not the Minecraft Java launcher's renderer path. Copying those files would not make them work and could break the build. Porting would require a compatible hook in the actual renderer used by Minecraft and separate device/runtime tests.

## Build
This is source code, not a verified APK. Build with the project's Gradle/Android NDK toolchain. Check `./gradlew tasks` first; the expected full release variant is likely `assembleFullRelease`, but the exact task must be confirmed in the build environment.
