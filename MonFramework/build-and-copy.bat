REM Compile le projet et génère le jar
call mvn clean package
if errorlevel 1 exit /b 1

REM Copier le jar généré vers ../framework-test/lib/
copy target\framework-sprint-1.jar ..\MonProjetTest\WEB-INF\lib\
