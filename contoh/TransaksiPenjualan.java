import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Class TransaksiPenjualan merepresentasikan transaksi kasir yang selesai.
 * Sesuai rancangan OOP: noNota, waktu, totalBayar, nominalTunai, kembalian.
 */
public class TransaksiPenjualan {
    private String noNota;
    private LocalDateTime waktu;
    private double totalBayar;
    private double nominalTunai;
    private double kembalian;
    private List<ItemBelanja> daftarItem;

    public TransaksiPenjualan(String noNota, LocalDateTime waktu, double totalBayar, double nominalTunai, List<ItemBelanja> daftarItem) {
        this.noNota = noNota;
        this.waktu = waktu;
        this.totalBayar = totalBayar;
        this.nominalTunai = nominalTunai;
        this.kembalian = nominalTunai - totalBayar;
        this.daftarItem = new ArrayList<>(daftarItem);
    }

    public String getNoNota() {
        return noNota;
    }

    public LocalDateTime getWaktu() {
        return waktu;
    }

    public String getWaktuFormatted() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return waktu.format(dtf);
    }

    public double getTotalBayar() {
        return totalBayar;
    }

    public double getNominalTunai() {
        return nominalTunai;
    }

    public double getKembalian() {
        return kembalian;
    }

    public List<ItemBelanja> getDaftarItem() {
        return daftarItem;
    }

    private static String formatRupiah(double nilai) {
        Locale idLocale = Locale.forLanguageTag("id-ID");
        NumberFormat nf = NumberFormat.getCurrencyInstance(idLocale);
        return nf.format(nilai).replace("Rp", "Rp ");
    }

    /**
     * Format satu baris ringkasan untuk rekap_transaksi_kasir.txt
     */
    public String toRekapLine() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return String.format("%s | %s | Total: %s | Tunai: %s | Kembali: %s | %d Item",
                noNota,
                waktu.format(dtf),
                formatRupiah(totalBayar),
                formatRupiah(nominalTunai),
                formatRupiah(kembalian),
                daftarItem.size()
        );
    }

    /**
     * Menghasilkan teks struk pembelian kasir yang rapi untuk dicetak / ditampilkan di dialog.
     */
    public String cetakStruk() {
        StringBuilder sb = new StringBuilder();
        sb.append("==================================================\n");
        sb.append("               KOPMA MART MINIMARKET              \n");
        sb.append("          Koperasi Mahasiswa Terpadu             \n");
        sb.append("             Kampus Universitas                   \n");
        sb.append("==================================================\n");
        sb.append(String.format("No. Nota : %-20s\n", noNota));
        sb.append(String.format("Waktu    : %-20s\n", getWaktuFormatted()));
        sb.append(String.format("Kasir    : %-20s\n", "Kasir 01 (Cepat)"));
        sb.append("--------------------------------------------------\n");
        sb.append(String.format("%-22s %4s %10s %10s\n", "Item", "Qty", "Harga", "Subtotal"));
        sb.append("--------------------------------------------------\n");

        for (ItemBelanja item : daftarItem) {
            String nama = item.getBarang().getNamaBarang();
            if (nama.length() > 20) {
                nama = nama.substring(0, 19) + ".";
            }
            sb.append(String.format("%-22s %4d %,10.0f %,10.0f\n",
                    nama,
                    item.getJumlah(),
                    item.getBarang().getHargaJual(),
                    item.getSubtotal()
            ));
        }

        sb.append("--------------------------------------------------\n");
        sb.append(String.format("TOTAL BELANJA    : %28s\n", formatRupiah(totalBayar)));
        sb.append(String.format("TUNAI DITERIMA   : %28s\n", formatRupiah(nominalTunai)));
        sb.append(String.format("KEMBALIAN        : %28s\n", formatRupiah(kembalian)));
        sb.append("==================================================\n");
        sb.append("           Terima Kasih Atas Kunjungan Anda!      \n");
        sb.append("         Barang yang dibeli tidak dapat ditukar   \n");
        sb.append("==================================================\n");

        return sb.toString();
    }
}
