@echo off
setlocal

set "TARGET_DIR=%ProgramFiles%\AIM Fitness Park"
if not exist "%TARGET_DIR%" mkdir "%TARGET_DIR%"

robocopy "AIM-Fitness-Park" "%TARGET_DIR%" /E /IS /IT /NFL /NDL /NJH /NJS
if exist "aimlogo.ico" copy /Y "aimlogo.ico" "%TARGET_DIR%\" >nul

powershell -ExecutionPolicy Bypass -Command "$WshShell = New-Object -ComObject WScript.Shell; $Desktop = [Environment]::GetFolderPath('Desktop'); $s = $WshShell.CreateShortcut(\"$Desktop\AIM Fitness Park.lnk\"); $s.TargetPath = [System.IO.Path]::Combine($env:ProgramFiles, 'AIM Fitness Park\AIM-Fitness-Park.exe'); $s.WorkingDirectory = [System.IO.Path]::Combine($env:ProgramFiles, 'AIM Fitness Park'); $s.IconLocation = [System.IO.Path]::Combine($env:ProgramFiles, 'AIM Fitness Park\aimlogo.ico, 0'); $s.Save(); $Programs = [Environment]::GetFolderPath('Programs'); $AimDir = [System.IO.Path]::Combine($Programs, 'AIM Fitness Park'); if (!(Test-Path $AimDir)) { New-Item -ItemType Directory -Path $AimDir | Out-Null }; $s2 = $WshShell.CreateShortcut([System.IO.Path]::Combine($AimDir, 'AIM Fitness Park.lnk')); $s2.TargetPath = [System.IO.Path]::Combine($env:ProgramFiles, 'AIM Fitness Park\AIM-Fitness-Park.exe'); $s2.WorkingDirectory = [System.IO.Path]::Combine($env:ProgramFiles, 'AIM Fitness Park'); $s2.IconLocation = [System.IO.Path]::Combine($env:ProgramFiles, 'AIM Fitness Park\aimlogo.ico, 0'); $s2.Save();"

start "" "%TARGET_DIR%\AIM-Fitness-Park.exe"
exit /b 0
