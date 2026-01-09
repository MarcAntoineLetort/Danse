@echo off
setlocal enabledelayedexpansion

REM ================================
REM  CONFIGURATION
REM ================================
set APP_NAME=Danse
set APP_VERSION=1.0
set MAIN_JAR=DanseV11.jar
set MAIN_CLASS=application.MainDanse

if not exist "%MAIN_JAR%" (
  echo ERREUR : %MAIN_JAR% introuvable
  pause
  exit /b 1
)

set JDK=C:\Program Files\Java\jdk-21
set JAVAFX_JMODS=C:\Program Files\Java\javafx-jmods-21.0.9
set ICON=icons\Danse.ico

set DIST=dist
set INPUT=input
set RUNTIME_TEMP=runtime-temp

REM ================================
REM  NETTOYAGE
REM ================================
echo ===================================
echo  Nettoyage
echo ===================================

if exist "%DIST%" rmdir /s /q "%DIST%"
if exist "%INPUT%" rmdir /s /q "%INPUT%"
if exist "%RUNTIME_TEMP%" rmdir /s /q "%RUNTIME_TEMP%"

mkdir "%INPUT%"

REM ================================
REM  CREATION DU RUNTIME (jlink)
REM ================================
echo ===================================
echo  Creation du runtime Java
echo ===================================

"%JDK%\bin\jlink.exe" ^
  --output "%RUNTIME_TEMP%" ^
  --module-path "%JDK%\jmods;%JAVAFX_JMODS%" ^
  --add-modules java.base,java.desktop,java.logging,javafx.controls,javafx.fxml,javafx.media ^
  --strip-debug ^
  --no-man-pages ^
  --no-header-files

if errorlevel 1 (
    echo ERREUR jlink
    pause
    exit /b 1
)

REM ================================
REM  PREPARATION DES JARS
REM ================================
copy /Y "%MAIN_JAR%" "%INPUT%\"
copy /Y "C:\Program Files\Java\vlcj\jna-5.13.0.jar" "%INPUT%\"
copy /Y "C:\Program Files\Java\vlcj\jna-platform-5.13.0.jar" "%INPUT%\"
copy /Y "C:\Program Files\Java\vlcj\vlcj-4.8.1.jar" "%INPUT%\"
copy /Y "C:\Program Files\Java\vlcj\vlcj-javafx-1.2.0.jar" "%INPUT%\"
copy /Y "C:\Program Files\Java\vlcj\vlcj-natives-4.8.1.jar" "%INPUT%\"

REM ================================
REM  COPIE DU FICHIER CONFIG
REM ================================
copy /Y "Config.txt" "%INPUT%\Config.txt"

REM ================================
REM  CREATION DE L'APPLICATION
REM ================================
echo ===================================
echo  Creation de l'application EXE
echo ===================================

echo ICON UTILISEE : %ICON%
dir "%ICON%"

"%JDK%\bin\jpackage.exe" ^
  --type app-image ^
  --dest "%DIST%" ^
  --name "%APP_NAME%" ^
  --app-version "%APP_VERSION%" ^
  --input "%INPUT%" ^
  --main-jar "%MAIN_JAR%" ^
  --main-class "%MAIN_CLASS%" ^
  --runtime-image "%RUNTIME_TEMP%" ^
  --java-options "-Xmx2G" ^
  --icon "%ICON%"

REM  --win-console

if errorlevel 1 (
    echo ERREUR jpackage
    pause
    exit /b 1
)

REM ================================
REM  COPIE DE FFMPEG
REM ================================
echo ===================================
echo  Copie de ffmpeg
echo ===================================

set APP_DIR=%DIST%\%APP_NAME%

if not exist "%APP_DIR%" (
    echo ERREUR : dossier application introuvable
    pause
    exit /b 1
)

mkdir "%APP_DIR%\ffmpeg"
copy /Y "ffmpeg\ffmpeg.exe" "%APP_DIR%\ffmpeg\ffmpeg.exe"

REM ================================
REM  NETTOYAGE FINAL
REM ================================
rmdir /s /q "%RUNTIME_TEMP%"
rmdir /s /q "%INPUT%"

echo ===================================
echo  BUILD TERMINE AVEC SUCCES
echo ===================================
pause
