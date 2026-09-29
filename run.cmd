@echo off
call build.cmd
if errorlevel 1 exit /b %errorlevel%
java -cp "build;lib/*" grooveforge.App
