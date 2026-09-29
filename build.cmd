@echo off
setlocal
if not exist build mkdir build
javac -source 8 -target 8 -cp "lib/*" -d build src\grooveforge\Model.java src\grooveforge\TestConnection.java src\grooveforge\App.java
if errorlevel 1 exit /b %errorlevel%
echo Build successful.
