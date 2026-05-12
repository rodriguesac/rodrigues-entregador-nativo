@echo off
chcp 65001 >nul
cls

echo ===============================================
echo  Rodrigues Entregador Nativo - Correcao Windows
echo ===============================================
echo.

echo Removendo pasta antiga com pacote errado, se existir...

if exist "app\src\main\java\com\rodrigues\entregador" (
    rmdir /s /q "app\src\main\java\com\rodrigues\entregador"
    echo Pasta antiga removida: app\src\main\java\com\rodrigues\entregador
) else (
    echo Pasta antiga nao encontrada. Tudo certo.
)

echo.
echo Verificando nova pasta correta...

if exist "app\src\main\java\com\rodriguesacai\entregador" (
    echo OK: pasta correta encontrada.
) else (
    echo ATENCAO: pasta correta nao encontrada.
    echo Confira se voce extraiu o ZIP corrigido na raiz do projeto.
)

echo.
echo Correcao finalizada.
echo Agora faca commit/push e rode o GitHub Actions novamente.
echo.
pause
