# Kopma Mart POS – Kasir Cepat Minimarket Kampus

Aplikasi Point of Sale (POS) desktop kasir cepat untuk Koperasi Mahasiswa berbasis **Java Swing** dan mengimplementasikan konsep **Pemrograman Berorientasi Objek (OOP)** secara menyeluruh.

---

## 📌 Struktur & Rancangan OOP

1. **`Barang` ([Barang.java](file:///c:/Users/ThinkPad/contoh/Barang.java))**
   - Atribut: `barcodeId`, `namaBarang`, `kategori`, `hargaJual`, `stokGudang`
   - Method: Getter/Setter, `kurangiStok(int qty)`, `tambahStok(int qty)`, konversi format data file (`toFileLine()`, `fromFileLine()`).

2. **`ItemBelanja` ([ItemBelanja.java](file:///c:/Users/ThinkPad/contoh/ItemBelanja.java))**
   - Mewakili baris item dalam keranjang belanja: referensi `Barang`, `jumlah`, dan `getSubtotal()`.

3. **`KeranjangBelanja` ([KeranjangBelanja.java](file:///c:/Users/ThinkPad/contoh/KeranjangBelanja.java))**
   - Atribut: `daftarItem` (`List<ItemBelanja>`)
   - Method:
     - `tambahItem(Barang barang, int qty)`: Menambah item dengan validasi stok gudang.
     - `hapusItem(String barcodeId)`: Menghapus item dari keranjang.
     - `hitungSubtotal()`: Menghitung total tagihan belanja.
     - `kosongkan()`: Mereset isi keranjang.

4. **`TransaksiPenjualan` ([TransaksiPenjualan.java](file:///c:/Users/ThinkPad/contoh/TransaksiPenjualan.java))**
   - Atribut: `noNota`, `waktu`, `totalBayar`, `nominalTunai`, `kembalian`, `daftarItem`
   - Method:
     - `cetakStruk()`: Menghasilkan struk kasir minimarket berformat rapi.
     - `toRekapLine()`: Format log ringkasan untuk rekapitulasi penjualan.

5. **`DataManager` ([DataManager.java](file:///c:/Users/ThinkPad/contoh/DataManager.java))**
   - Mengelola **Data Persistence**:
     - Membaca & memperbarui `master_stok_barang.txt` (stok otomatis berkurang saat transaksi selesai).
     - Mencatat setiap transaksi ke `rekap_transaksi_kasir.txt`.

6. **`KopmaMartApp` ([KopmaMartApp.java](file:///c:/Users/ThinkPad/contoh/KopmaMartApp.java))**
   - **Antarmuka GUI Interaktif**:
     - **Input Barcode Cepat**: Kolom teks dengan listener tombol `Enter` (otomatis menambah item ke tabel tanpa perlu klik mouse).
     - **Panel Total Bayar Besar**: Huruf berukuran besar (**Font Size 28**) dengan warna kontras.
     - **Validasi Stok**: Menampilkan dialog peringatan jika barang habis atau jumlah pembelian melebihi stok gudang.
     - **Tab Monitor Stok Gudang Live**: Memantau stok berkurang secara realtime untuk keperluan demo expo.
     - **Tab Rekap Transaksi**: Menampilkan histori penjualan langsung dari file rekap.

---

## 🚀 Cara Menjalankan Aplikasi

### Opsi 1: Menggunakan Script Cepat
Klik dua kali file [run.bat](file:///c:/Users/ThinkPad/contoh/run.bat) atau jalankan melalui terminal:
```cmd
.\run.bat
```

### Opsi 2: Manual Menggunakan Terminal
1. Buka terminal di folder project (`c:\Users\ThinkPad\contoh`).
2. Kompilasi semua file:
   ```cmd
   javac -encoding UTF-8 Barang.java ItemBelanja.java KeranjangBelanja.java TransaksiPenjualan.java DataManager.java KopmaMartApp.java
   ```
3. Jalankan aplikasi:
   ```cmd
   java KopmaMartApp
   ```

---

## 🌟 Panduan Demo Expo (WOW Factor)
1. Buka aplikasi Kopma Mart POS.
2. Pada kolom barcode, ketik `8991001` lalu tekan **ENTER**.
3. *Indomie Goreng Spesial* seketika muncul di tabel keranjang dan total belanja terhitung otomatis.
4. Tekan **ENTER** lagi beberapa kali untuk melihat kuantitas bertambah otomatis tanpa memegang mouse.
5. Coba ketik barcode barang yang stoknya sedikit lalu tambahkan melebihi batas: sistem akan otomatis menolak dan memunculkan peringatan validasi stok.
6. Masukkan nominal uang di kolom tunai (atau klik tombol cepat *50rb* / *100rb*), lalu tekan **ENTER** atau klik **BAYAR & SELESAI TRANSAKSI**.
7. Dialog struk kasir resmi akan muncul, file [master_stok_barang.txt](file:///c:/Users/ThinkPad/contoh/master_stok_barang.txt) dan [rekap_transaksi_kasir.txt](file:///c:/Users/ThinkPad/contoh/rekap_transaksi_kasir.txt) langsung diperbarui otomatis! 
