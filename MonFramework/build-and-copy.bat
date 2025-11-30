@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%

call mvn clean install
if errorlevel 1 exit /b 1

REM Copier le jar généré vers ../MonProjetTest/WEB-INF/lib/
copy target\framework-sprint-1.jar ..\MonProjetTest\WEB-INF\lib\
