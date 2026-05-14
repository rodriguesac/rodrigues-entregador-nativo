@echo off
cd /d "%~dp0"

echo ========================================
echo Corrigir V3 nativo - remover hibrido
echo ========================================

if exist "app\src\main\assets" (
  rmdir /s /q "app\src\main\assets"
)

if exist "app\src\main\java\com\rodriguesacai\entregador\web" (
  rmdir /s /q "app\src\main\java\com\rodriguesacai\entregador\web"
)

if exist "app\src\main\java\com\rodriguesacai\entregador\service\DriverSessionStore.kt" (
  del /f /q "app\src\main\java\com\rodriguesacai\entregador\service\DriverSessionStore.kt"
)

if exist "APLICAR_E_PUSH_WINDOWS.bat" (
  del /f /q "APLICAR_E_PUSH_WINDOWS.bat"
)

git add -A
git commit -m "Remove sobras hibridas da V3 nativa"
git push

echo.
echo Pronto. Abra o GitHub Actions e rode/aguarde o build mais recente.
pause
