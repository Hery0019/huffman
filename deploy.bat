@echo off
setlocal

:: Compile, empaquette et deploie l'application dans Tomcat.
:: Prerequis : javac et jar dans le PATH ; CATALINA_HOME (ou l'installation par defaut ci-dessous).

:: Le repertoire du projet est celui de ce script (plus de chemin en dur).
set "work_dir=%~dp0"
set "work_dir=%work_dir:~0,-1%"
set "build=%work_dir%\temp"
set "web=%work_dir%\web"
set "lib=%work_dir%\lib"
set "src=%work_dir%\src"
set "war_name=huffman"
if "%CATALINA_HOME%"=="" set "CATALINA_HOME=C:\Program Files\Apache Software Foundation\Tomcat 10.1"
set "web_apps=%CATALINA_HOME%\webapps"

:: Dossier de build propre
if exist "%build%" rd /s /q "%build%"
mkdir "%build%\WEB-INF\lib"
mkdir "%build%\WEB-INF\classes"

:: Pages, fragments et ressources web
xcopy /s /y /q "%web%\*.*" "%build%" >nul

:: Jars d'execution uniquement : lib\provided\ est fourni par Tomcat, lib\test\ ne sert qu'aux tests.
copy /y "%lib%\*.jar" "%build%\WEB-INF\lib" >nul

:: Compilation
dir /s /b "%src%\*.java" > "%build%\sources.txt"
javac -encoding UTF-8 -d "%build%\WEB-INF\classes" -cp "%lib%\*;%lib%\provided\*" @"%build%\sources.txt"
if errorlevel 1 (
    echo Compilation echouee, deploiement annule.
    exit /b 1
)
del "%build%\sources.txt"

:: Archive WAR
pushd "%build%"
jar cf "%work_dir%\%war_name%.war" *
popd

:: Deploiement
if not exist "%web_apps%" (
    echo Dossier webapps introuvable : "%web_apps%". Definissez CATALINA_HOME.
    exit /b 1
)
if exist "%web_apps%\%war_name%.war" del /f /q "%web_apps%\%war_name%.war"
copy /y "%work_dir%\%war_name%.war" "%web_apps%" >nul
del "%work_dir%\%war_name%.war"

echo Deploiement termine : %web_apps%\%war_name%.war
endlocal
pause
