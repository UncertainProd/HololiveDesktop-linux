@echo off
set TARGET=%APPDATA%\Microsoft\Windows\Start Menu\Programs\Startup\HololiveEN Myth Shimeji-ee.lnk
if exist "%TARGET%" (
    echo Removing HololiveEN Myth Shimeji-ee from Startup folder...
    del "%TARGET%"
) else (
    echo Shortcut not found in Startup folder.
)
