@echo off
REM Detached runData launcher: sets the toolchain env explicitly (Start-Process does
REM not inherit env from other PowerShell calls) and logs to an absolute path.
set "JAVA_HOME=C:\Users\19063\.jdks\ms-21.0.10"
set "GRADLE_USER_HOME=E:\McModding\Modding\CraftOfExile-NeoForge\.gradle_fresh"
set "GRADLE_OPTS=-Xmx4G -Dfile.encoding=UTF-8"
cd /d "E:\McModding\Modding\CraftOfExile-NeoForge"
call gradlew.bat runData --no-daemon --no-configuration-cache > "E:\McModding\Modding\CraftOfExile-NeoForge\poe2_assets\_rd21.log" 2>&1
echo EXIT=%ERRORLEVEL% >> "E:\McModding\Modding\CraftOfExile-NeoForge\poe2_assets\_rd21.log"
