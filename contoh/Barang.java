import java.text.NumberFormat;
import java.util.Locale;

/**
 * Class Barang merepresentasikan data master komoditas pada Kopma Mart.
 * Sesuai rancangan OOP: barcodeId, namaBarang, kategori, hargaJual, stokGudang.
 */
public class Barang {
    private String barcodeId;
    private String namaBarang;
    private String kategori;
    private double hargaJual;
    private int stokGudang;

    public Barang(String barcodeId, String namaBarang, String kategori, double hargaJual, int stokGudang) {
        this.barcodeId = barcodeId;
        this.namaBarang = namaBarang;
        this.kategori = kategori;
        this.hargaJual = hargaJual;
        this.stokGudang = stokGudang;
    }

    public String getBarcodeId() {
        return barcodeId;
    }

    public void setBarcodeId(String barcodeId) {
        this.barcodeId = barcodeId;
    }

    public String getNamaBarang() {
        return namaBarang;
    }

    public void setNamaBarang(String namaBarang) {
        this.namaBarang = namaBarang;
    }

    public String getKategori() {
        return kategori;
    }

    public void setKategori(String kategori) {
        this.kategori = kategori;
    }

    public double getHargaJual() {
        return hargaJual;
    }

    public void setHargaJual(double hargaJual) {
        this.hargaJual = hargaJual;
    }

    public int getStokGudang() {
        return stokGudang;
    }

    public void setStokGudang(int stokGudang) {
        this.stokGudang = stokGudang;
    }

    public boolean kurangiStok(int jumlah) {
        if (this.stokGudang >= jumlah) {
            this.stokGudang -= jumlah;
            return true;
        }
        return false;
    }

    public void tambahStok(int jumlah) {
        this.stokGudang += jumlah;
    }

    public String getFormattedHarga() {
        Locale idLocale = Locale.forLanguageTag("id-ID");
        NumberFormat nf = NumberFormat.getCurrencyInstance(idLocale);
        return nf.format(hargaJual).replace("Rp", "Rp ");
    }

    /**
     * Konversi ke baris data file master_stok_barang.txt
     */
    public String toFileLine() {
        return barcodeId + ";" + namaBarang + ";" + kategori + ";" + (long)hargaJual + ";" + stokGudang;
    }

    /**
     * Parser dari baris file master_stok_barang.txt
     */
    public static Barang fromFileLine(String line) {
        if (line == null || line.trim().isEmpty() || line.startsWith("#")) {
            return null;
        }
        String[] parts = line.split(";");
        if (parts.length >= 5) {
            String barcodeId = parts[0].trim();
            String nama = parts[1].trim();
            String kategori = parts[2].trim();
            double harga = Double.parseDouble(parts[3].trim());
            int stok = Integer.parseInt(parts[4].trim());
            return new Barang(barcodeId, nama, kategori, harga, stok);
        }
        return null;
    }

    @Override
    public String toString() {
        return namaBarang + " [" + barcodeId + "] - " + getFormattedHarga() + " (Stok: " + stokGudang + ")";
    }
}
