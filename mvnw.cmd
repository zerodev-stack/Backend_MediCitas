@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script
@REM ----------------------------------------------------------------------------

@IF "%__JAVA_HUB_DEBUG__%" == "" @ECHO OFF

@SETLOCAL

SET MAVEN_PROJECTBASEDIR=%~dp0
IF NOT "%MAVEN_PROJECTBASEDIR%"=="" SET MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%

SET MAVEN_BATCH_ECHO=off
SET MAVEN_BATCH_PAUSE=off

SET WRAPPER_JAR="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
SET WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain

IF EXIST %WRAPPER_JAR% GOTO run

SET DOWNLOAD_URL="https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar"
IF NOT EXIST "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper" MKDIR "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper"

powershell -Command "[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12; (New-Object Net.WebClient).DownloadFile('%DOWNLOAD_URL%', '%WRAPPER_JAR%')"

:run
SET EXEC_DIR=%CD%
CD /D "%MAVEN_PROJECTBASEDIR%"

IF EXIST "%JAVA_HOME%\bin\java.exe" (
    SET "JAVACMD=%JAVA_HOME%\bin\java.exe"
) ELSE (
    SET "JAVACMD=java"
)

"%JAVACMD%" "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" -classpath %WRAPPER_JAR% %WRAPPER_LAUNCHER% %*

CD /D "%EXEC_DIR%"
