@echo off
setlocal
cd /d "%~dp0"
echo Aplicando V3.2 visual PainelUP + login/cadastro nativo...
if exist "app\src\main\assets" rmdir /S /Q "app\src\main\assets"
if exist "app\src\main\java\com\rodriguesacai\entregador\web" rmdir /S /Q "app\src\main\java\com\rodriguesacai\entregador\web"
if exist "app\src\main\java\com\rodriguesacai\entregador\service\DriverSessionStore.kt" del /F /Q "app\src\main\java\com\rodriguesacai\entregador\service\DriverSessionStore.kt"
git add -A
git commit -m "V3.2 visual PainelUP login cadastro nativo"
git push
echo.
echo Pronto. Abra o GitHub Actions e baixe o APK quando o build ficar verde.
pause
endlocal
