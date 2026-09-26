@echo off
chcp 65001 > nul
echo ====================================================
echo   KOPMA MART - POS KASIR CEPAT MINIMARKET KAMPUS
echo ====================================================
echo Mengompilasi source code Java...
javac -encoding UTF-8 Barang.java ItemBelanja.java KeranjangBelanja.java TransaksiPenjualan.java DataManager.java KopmaMartApp.java
if %errorlevel% neq 0 (
    echo Gagal kompilasi! Pastikan JDK terpasang dengan benar.
    pause
    exit /b %errorlevel%
)
echo Menjalankan aplikasi...
java KopmaMartApp
