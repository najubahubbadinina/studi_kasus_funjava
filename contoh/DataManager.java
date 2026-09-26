import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * DataManager menangani pembacaan dan penyimpanan file persistence:
 * 1. master_stok_barang.txt (data barang dan pembaruan stok)
 * 2. rekap_transaksi_kasir.txt (catatan log riwayat penjualan kasir)
 */
public class DataManager {
    public static final String FILE_MASTER_STOK = "master_stok_barang.txt";
    public static final String FILE_REKAP_TRANSAKSI = "rekap_transaksi_kasir.txt";

    private Map<String, Barang> barangMap; // key: barcodeId

    public DataManager() {
        this.barangMap = new LinkedHashMap<>();
        muatDataMaster();
    }

    /**
     * Memuat data barang dari file master_stok_barang.txt.
     * Jika file belum ada, sistem akan membuat file default dengan beberapa produk contoh.
     */
    public void muatDataMaster() {
        barangMap.clear();
        File file = new File(FILE_MASTER_STOK);

        if (!file.exists()) {
            buatDataMasterDefault(file);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                Barang b = Barang.fromFileLine(line);
                if (b != null) {
                    barangMap.put(b.getBarcodeId(), b);
                }
            }
        } catch (IOException e) {
            System.err.println("Gagal membaca " + FILE_MASTER_STOK + ": " + e.getMessage());
        }
    }

    /**
     * Membuat file master stok awal jika belum ada
     */
    private void buatDataMasterDefault(File file) {
        List<String> defaultRows = Arrays.asList(
                "# FORMAT: barcodeId;namaBarang;kategori;hargaJual;stokGudang",
                "8991001;Indomie Goreng Spesial;Makanan;3500;45",
                "8991002;Indomie Kuah Ayam Bawang;Makanan;3500;30",
                "8992001;Aqua Botol 600ml;Minuman;3500;50",
                "8992002;Teh Botol Sosro Kotak 250ml;Minuman;4000;25",
                "8992003;Ultra Milk Coklat 250ml;Minuman;6500;20",
                "8992004;Nescafe Kopi Can Original;Minuman;9000;15",
                "8993001;Chitato Sapi Panggang 68g;Snack;11500;18",
                "8993002;Roti Sari Roti Coklat;Snack;6000;12",
                "8994001;Buku Tulis Sinar Dunia 38;ATK;5000;40",
                "8994002;Pulpen Standard AE7 Hitam;ATK;3000;60",
                "8994003;Correction Tape Joyko Tipex;ATK;8500;15",
                "8995001;Kopma Tote Bag Kanvas;Merchandise;35000;10"
        );

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            for (String row : defaultRows) {
                writer.write(row);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Gagal membuat data master default: " + e.getMessage());
        }
    }

    /**
     * Menyimpan seluruh perubahan stok barang ke file master_stok_barang.txt
     */
    public synchronized boolean simpanSemuaBarang() {
        File file = new File(FILE_MASTER_STOK);
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            writer.write("# FORMAT: barcodeId;namaBarang;kategori;hargaJual;stokGudang");
            writer.newLine();
            for (Barang b : barangMap.values()) {
                writer.write(b.toFileLine());
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Gagal menyimpan ke " + FILE_MASTER_STOK + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Mencatat transaksi yang berhasil ke file rekap_transaksi_kasir.txt
     */
    public synchronized boolean catatRekapTransaksi(TransaksiPenjualan transaksi) {
        File file = new File(FILE_REKAP_TRANSAKSI);
        boolean fileBaru = !file.exists();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), StandardCharsets.UTF_8))) {
            if (fileBaru) {
                writer.write("# ==========================================================================");
                writer.newLine();
                writer.write("# REKAP TRANSAKSI PENJUALAN KOPMA MART");
                writer.newLine();
                writer.write("# Format: NoNota | Waktu | Total | Tunai | Kembalian | Jumlah Item");
                writer.newLine();
                writer.write("# ==========================================================================");
                writer.newLine();
            }
            writer.write(transaksi.toRekapLine());
            writer.newLine();
            return true;
        } catch (IOException e) {
            System.err.println("Gagal mencatat rekap transaksi: " + e.getMessage());
            return false;
        }
    }

    /**
     * Membaca seluruh baris isi file rekap transaksi
     */
    public List<String> bacaRekapTransaksi() {
        List<String> lines = new ArrayList<>();
        File file = new File(FILE_REKAP_TRANSAKSI);
        if (!file.exists()) {
            return lines;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Gagal membaca rekap transaksi: " + e.getMessage());
        }
        return lines;
    }

    public Barang cariBarang(String barcodeId) {
        if (barcodeId == null) return null;
        return barangMap.get(barcodeId.trim());
    }

    public Collection<Barang> getSemuaBarang() {
        return barangMap.values();
    }
}
