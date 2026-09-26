import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Class KeranjangBelanja mengelola item belanja sementara di kasir.
 * Sesuai rancangan OOP: atribut daftarItem, method tambahItem(), hapusItem(), hitungSubtotal().
 */
public class KeranjangBelanja {
    private List<ItemBelanja> daftarItem;

    public KeranjangBelanja() {
        this.daftarItem = new ArrayList<>();
    }

    public List<ItemBelanja> getDaftarItem() {
        return daftarItem;
    }

    /**
     * Menambah item ke keranjang dengan validasi ketersediaan stok gudang.
     * Mengembalikan true jika berhasil ditambahkan, false jika stok tidak mencukupi.
     */
    public boolean tambahItem(Barang barang, int qty) {
        if (barang == null || qty <= 0) {
            return false;
        }

        // Cek apakah item sudah ada di dalam keranjang
        for (ItemBelanja item : daftarItem) {
            if (item.getBarang().getBarcodeId().equalsIgnoreCase(barang.getBarcodeId())) {
                int totalDiminta = item.getJumlah() + qty;
                if (totalDiminta > barang.getStokGudang()) {
                    return false; // Melebihi stok gudang
                }
                item.tambahJumlah(qty);
                return true;
            }
        }

        // Jika belum ada di keranjang, cek stok gudang cukup
        if (qty > barang.getStokGudang()) {
            return false;
        }

        daftarItem.add(new ItemBelanja(barang, qty));
        return true;
    }

    /**
     * Overload tambahItem: menambah 1 buah barang (biasa untuk scan barcode enter)
     */
    public boolean tambahItem(Barang barang) {
        return tambahItem(barang, 1);
    }

    /**
     * Menghapus item berdasarkan barcodeId
     */
    public boolean hapusItem(String barcodeId) {
        if (barcodeId == null) return false;
        return daftarItem.removeIf(item -> item.getBarang().getBarcodeId().equalsIgnoreCase(barcodeId));
    }

    /**
     * Menghapus item berdasarkan index di list
     */
    public boolean hapusItem(int index) {
        if (index >= 0 && index < daftarItem.size()) {
            daftarItem.remove(index);
            return true;
        }
        return false;
    }

    /**
     * Menghitung total akumulasi subtotal seluruh item dalam keranjang
     */
    public double hitungSubtotal() {
        double total = 0.0;
        for (ItemBelanja item : daftarItem) {
            total += item.getSubtotal();
        }
        return total;
    }

    /**
     * Mengosongkan seluruh isi keranjang
     */
    public void kosongkan() {
        daftarItem.clear();
    }

    public int getJumlahTotalItem() {
        int totalQty = 0;
        for (ItemBelanja item : daftarItem) {
            totalQty += item.getJumlah();
        }
        return totalQty;
    }

    public String getFormattedTotal() {
        Locale idLocale = Locale.forLanguageTag("id-ID");
        NumberFormat nf = NumberFormat.getCurrencyInstance(idLocale);
        return nf.format(hitungSubtotal()).replace("Rp", "Rp ");
    }
}
