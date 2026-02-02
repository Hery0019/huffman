@echo off
setlocal

:: Compile src\ et test\ puis execute les tests JUnit 5 (lanceur autonome dans lib\test\).
set "work_dir=%~dp0"
set "work_dir=%work_dir:~0,-1%"
set "out=%work_dir%\temp\test-classes"
set "junit=%work_dir%\lib\test\junit-platform-console-standalone-1.14.4.jar"

if exist "%out%" rd /s /q "%out%"
mkdir "%out%"

dir /s /b "%work_dir%\src\*.java" "%work_dir%\test\*.java" > "%out%\sources.txt"
javac -encoding UTF-8 -d "%out%" -cp "%work_dir%\lib\*;%work_dir%\lib\provided\*;%junit%" @"%out%\sources.txt"
if errorlevel 1 (
    echo Compilation echouee.
    exit /b 1
)
del "%out%\sources.txt"

java -jar "%junit%" execute --class-path "%out%" --scan-class-path --disable-banner
endlocal
