# Studi Kasus: Kopma Mart (Koperasi Mahasiswa) POS
**"Kopma Mart (Koperasi Mahasiswa) POS" – Kasir Cepat Minimarket Kampus**

* **Latar Belakang:** Kasir Koperasi Mahasiswa memerlukan transaksi kasir yang cepat, dapat mengurangi stok secara otomatis, dan mendukung pencarian kode barcode.
* **Rancangan OOP:**
  * **Class `Barang`**: atribut `barcodeId`, `namaBarang`, `kategori`, `hargaJual`, `stokGudang`.
  * **Class `KeranjangBelanja`**: atribut `daftarItem`, method `tambahItem()`, `hapusItem()`, `hitungSubtotal()`.
  * **Class `TransaksiPenjualan`**: atribut `noNota`, `waktu`, `totalBayar`, `nominalTunai`, `kembalian`.
* **Antarmuka GUI:**
  * Kolom teks input kode barcode dengan listener tombol Enter (otomatis menambah item ke tabel tanpa perlu klik mouse).
  * Panel ringkasan total bayar dengan huruf berukuran besar (Font Size 28).
  * Validasi stok: memunculkan peringatan jika pembelian melebihi stok yang ada.
* **Data Persistence:** 
  * File `master_stok_barang.txt` (stok berkurang saat transaksi sukses)
  * File `rekap_transaksi_kasir.txt`
* **WOW Factor Demo Expo:** Memperagakan pengetikan kode barcode lalu menekan Enter, barang seketika muncul di baris tabel dan stok gudang otomatis berkurang.
