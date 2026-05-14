@echo off
setlocal EnableExtensions
chcp 65001 >nul

echo.
echo =====================================================
echo  Rodrigues Entregador - V5.2 Produto Nativo
echo =====================================================
echo.

if not exist ".git" (
  echo ERRO: rode este arquivo dentro de C:\RSITE\rodrigues-entregador-nativo
  pause
  exit /b 1
)

if not exist "app" (
  echo ERRO: pasta app nao encontrada.
  pause
  exit /b 1
)

echo Limpando sobras hibridas...
if exist "app\src\main\assets" rmdir /S /Q "app\src\main\assets"
if exist "app\src\main\java\com\rodriguesacai\entregador\web" rmdir /S /Q "app\src\main\java\com\rodriguesacai\entregador\web"
if exist "app\src\main\java\com\rodriguesacai\entregador\service\DriverSessionStore.kt" del /F /Q "app\src\main\java\com\rodriguesacai\entregador\service\DriverSessionStore.kt"

echo Limpando builds locais...
if exist ".gradle" rmdir /S /Q ".gradle"
if exist "build" rmdir /S /Q "build"
if exist "app\build" rmdir /S /Q "app\build"

echo.
echo Status dos arquivos:
git status

echo.
echo Enviando para o GitHub...
git add -A
git commit -m "V5.2 produto nativo completo" || echo Nada novo para commit ou commit ja feito.
git push

echo.
echo Finalizado. Agora abra GitHub Actions e baixe o APK em Artifacts.
echo.
pause
endlocal
