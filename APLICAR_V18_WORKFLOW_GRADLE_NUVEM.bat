@echo off
setlocal
chcp 65001 >nul
cd /d "%~dp0"
echo.
echo Rodrigues Entregador V18 - limpar workflows antigos e usar Gradle na nuvem
echo Este BAT NAO faz commit e NAO faz push.
echo.
if not exist app (
  echo ERRO: execute dentro da pasta do repositorio rodrigues-entregador-nativo.
  pause
  exit /b 1
)
if not exist .github\workflows mkdir .github\workflows
for %%F in (.github\workflows\*.yml .github\workflows\*.yaml) do (
  if /I not "%%~nxF"=="build-apk.yml" del /f /q "%%F"
)
for %%F in (APLICAR_LIMPO_SEM_PUSH.bat APLICAR_V16_LIMPAR_WORKFLOWS.bat APLICAR_V17_WORKFLOW_UNICO.bat android-debug.yml build-native.yml build-debug.yml) do (
  if exist "%%F" del /f /q "%%F"
)
for %%F in (*debug-apk*.zip *fonte*.zip *icones*.zip *v8*.zip *v9*.zip *v10*.zip *v11*.zip *v15*.zip *v16*.zip *v17*.zip) do del /f /q "%%F" 2>nul
echo.
echo Versao declarada:
findstr /i "versionCode versionName" app\build.gradle.kts
echo.
echo Agora rode:
echo git status
echo git add -A
echo git commit -m "Rodrigues Entregador V18 Gradle nuvem UI polida"
echo git push origin main
echo.
pause
