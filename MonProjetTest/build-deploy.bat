@echo off
setlocal

set PROJECT_NAME=MonProjetServlet
set TOMCAT_HOME=C:\xampp\tomcat

echo === Build et déploiement WAR ===
mvn clean package

if errorlevel 1 (
    echo Erreur lors du build Maven !
    pause
    exit /b 1
)

copy target\%PROJECT_NAME%-1.0-SNAPSHOT.war "%TOMCAT_HOME%\webapps\%PROJECT_NAME%.war"

echo Déploiement terminé.
echo Accédez à : http://localhost:8080/%PROJECT_NAME%/hello
pause
