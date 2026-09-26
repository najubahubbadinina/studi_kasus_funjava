import java.text.NumberFormat;
import java.util.Locale;

/**
 * Class ItemBelanja merepresentasikan satu baris barang dalam KeranjangBelanja.
 */
public class ItemBelanja {
    private Barang barang;
    private int jumlah;

    public ItemBelanja(Barang barang, int jumlah) {
        this.barang = barang;
        this.jumlah = jumlah;
    }

    public Barang getBarang() {
        return barang;
    }

    public void setBarang(Barang barang) {
        this.barang = barang;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public void tambahJumlah(int qty) {
        this.jumlah += qty;
    }

    public double getSubtotal() {
        return barang.getHargaJual() * jumlah;
    }

    public String getFormattedSubtotal() {
        Locale idLocale = Locale.forLanguageTag("id-ID");
        NumberFormat nf = NumberFormat.getCurrencyInstance(idLocale);
        return nf.format(getSubtotal()).replace("Rp", "Rp ");
    }
}
