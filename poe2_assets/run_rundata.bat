@echo off
set JAVA_HOME=C:\Users\19063\.jdks\ms-21.0.10
set GRADLE_USER_HOME=%CD%\.gradle_fresh
set GRADLE_OPTS=-Xmx4G -Dfile.encoding=UTF-8
call gradlew.bat runData --no-daemon --no-configuration-cache > poe2_assets\_rd16.log 2>&1
echo EXIT=%ERRORLEVEL% >> poe2_assets\_rd16.log
