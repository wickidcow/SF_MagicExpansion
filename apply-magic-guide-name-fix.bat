@echo off
setlocal
cd /d "%~dp0"

where py >nul 2>nul
if %errorlevel%==0 (
    py -3 apply-magic-guide-name-fix.py "%CD%"
) else (
    python apply-magic-guide-name-fix.py "%CD%"
)

echo.
pause
