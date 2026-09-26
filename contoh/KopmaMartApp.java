import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Aplikasi Utama GUI: "Kopma Mart (Koperasi Mahasiswa) POS" – Kasir Cepat Minimarket Kampus.
 * 
 * Fitur Utama:
 * 1. Input Barcode Cepat dengan Listener Enter otomatis masuk ke tabel belanja tanpa klik mouse.
 * 2. Panel ringkasan Total Bayar dengan huruf berukuran besar (Font Size 28).
 * 3. Validasi stok real-time (peringatan jika kuantitas melebihi stok gudang).
 * 4. Data Persistence: master_stok_barang.txt dan rekap_transaksi_kasir.txt.
 * 5. Tab Monitor Stok Gudang Live dan Tab Rekap Transaksi untuk Demo Expo.
 */
public class KopmaMartApp extends JFrame {

    private DataManager dataManager;
    private KeranjangBelanja keranjang;
    private long counterNota = 1;

    // Komponen GUI Tab Kasir
    private JTextField txtBarcode;
    private JLabel lblStatusBarcode;
    private JTable tblKeranjang;
    private DefaultTableModel modelKeranjang;
    private JLabel lblTotalBayarBesar;
    private JTextField txtNominalTunai;
    private JLabel lblKembalian;
    private JButton btnBayar;
    private JButton btnHapusItem;
    private JButton btnKosongkan;

    // Komponen GUI Tab Monitor Stok Gudang
    private JTable tblStokGudang;
    private DefaultTableModel modelStokGudang;
    private JTextField txtCariStok;

    // Komponen GUI Tab Rekap
    private JTextArea txtAreaRekap;

    private final Locale idLocale = Locale.forLanguageTag("id-ID");
    private final NumberFormat nfRupiah = NumberFormat.getCurrencyInstance(idLocale);

    public KopmaMartApp() {
        super("Kopma Mart POS – Kasir Cepat Minimarket Kampus");
        this.dataManager = new DataManager();
        this.keranjang = new KeranjangBelanja();
        this.counterNota = hitungAwalNota();

        initUI();
        refreshTabelStok();
        refreshTampilanRekap();
    }

    private long hitungAwalNota() {
        List<String> rekap = dataManager.bacaRekapTransaksi();
        long maxNota = 0;
        for (String r : rekap) {
            if (r.startsWith("NOTA-")) {
                try {
                    String[] parts = r.split("\\|");
                    String notaPart = parts[0].trim().replace("NOTA-", "");
                    String[] sub = notaPart.split("-");
                    if (sub.length >= 2) {
                        long n = Long.parseLong(sub[1]);
                        if (n > maxNota) maxNota = n;
                    }
                } catch (Exception ignored) {}
            }
        }
        return maxNota + 1;
    }

    private void initUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);

        // Gunakan tampilan sistem yang rapi
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored2) {}
        }

        // Layout Utama
        JPanel mainContainer = new JPanel(new BorderLayout(0, 0));

        // 1. Header Banner
        mainContainer.add(createHeaderPanel(), BorderLayout.NORTH);

        // 2. Tabbed Pane (Kasir POS, Monitor Stok Gudang, Rekap Transaksi)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tabbedPane.addTab("🛒 Kasir Cepat (POS)", createKasirPanel());
        tabbedPane.addTab("📦 Monitor Stok Gudang (Live)", createStokGudangPanel());
        tabbedPane.addTab("📋 Rekap Transaksi Kasir", createRekapPanel());

        tabbedPane.addChangeListener(e -> {
            int selected = tabbedPane.getSelectedIndex();
            if (selected == 1) {
                refreshTabelStok();
            } else if (selected == 2) {
                refreshTampilanRekap();
            }
        });

        mainContainer.add(tabbedPane, BorderLayout.CENTER);

        // 3. Status Bar Bawah
        mainContainer.add(createFooterPanel(), BorderLayout.SOUTH);

        setContentPane(mainContainer);
    }

    private JPanel createHeaderPanel() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(24, 76, 120)); // Navy Blue profesional
        header.setBorder(new EmptyBorder(12, 20, 12, 20));

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);

        JLabel title = new JLabel("🏪 KOPMA MART - POS KASIR CEPAT");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Sistem Kasir Minimarket Kampus Koperasi Mahasiswa | Transaksi Instan Barcode");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(210, 230, 250));

        left.add(title);
        left.add(subtitle);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        right.setOpaque(false);

        JLabel lblDate = new JLabel(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMMM yyyy", idLocale)));
        lblDate.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblDate.setForeground(Color.WHITE);
        right.add(lblDate);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel createKasirPanel() {
        JPanel panel = new JPanel(new BorderLayout(12, 12));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        // Area Kiri: Input Barcode + Tabel Keranjang + Tombol Demo Barcode
        JPanel leftPanel = new JPanel(new BorderLayout(8, 8));

        // --- Panel Barcode Input (FITUR KUNCI ENTER LISTENER) ---
        JPanel barcodePanel = new JPanel(new BorderLayout(8, 6));
        barcodePanel.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder(new LineBorder(new Color(41, 128, 185), 2), " [F2] Pindai / Ketik Kode Barcode (Tekan ENTER) ", TitledBorder.LEFT, TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 13), new Color(41, 128, 185)),
                new EmptyBorder(8, 10, 10, 10)
        ));
        barcodePanel.setBackground(Color.WHITE);

        txtBarcode = new JTextField();
        txtBarcode.setFont(new Font("Consolas", Font.BOLD, 18));
        txtBarcode.setToolTipText("Ketik barcode lalu tekan Enter tanpa menggunakan mouse");

        // Action Listener tombol ENTER pada kolom barcode
        txtBarcode.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                prosesInputBarcode();
            }
        });

        lblStatusBarcode = new JLabel("💡 Siap memindai. Ketik barcode lalu tekan ENTER untuk menambah belanjaan.");
        lblStatusBarcode.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblStatusBarcode.setForeground(new Color(90, 90, 90));

        // Quick Demo Barcode Buttons untuk Kemudahan Demo Expo
        JPanel quickDemoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        quickDemoPanel.setOpaque(false);
        JLabel lblDemo = new JLabel("Demo Cepat:");
        lblDemo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        quickDemoPanel.add(lblDemo);

        String[][] demoBarcodes = {
                {"8991001", "Indomie (3.5k)"},
                {"8992001", "Aqua 600ml (3.5k)"},
                {"8992002", "Teh Botol (4k)"},
                {"8993001", "Chitato (11.5k)"},
                {"8994002", "Pulpen AE7 (3k)"}
        };

        for (String[] demo : demoBarcodes) {
            JButton btnDemo = new JButton(demo[0] + " " + demo[1]);
            btnDemo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btnDemo.setMargin(new Insets(2, 6, 2, 6));
            btnDemo.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnDemo.addActionListener(ev -> {
                txtBarcode.setText(demo[0]);
                prosesInputBarcode();
            });
            quickDemoPanel.add(btnDemo);
        }

        JPanel topBarcode = new JPanel(new BorderLayout(5, 5));
        topBarcode.setOpaque(false);
        topBarcode.add(txtBarcode, BorderLayout.CENTER);

        JButton btnInputManual = new JButton("Enter ⏎");
        btnInputManual.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnInputManual.setBackground(new Color(41, 128, 185));
        btnInputManual.setForeground(Color.WHITE);
        btnInputManual.addActionListener(e -> prosesInputBarcode());
        topBarcode.add(btnInputManual, BorderLayout.EAST);

        barcodePanel.add(topBarcode, BorderLayout.NORTH);
        barcodePanel.add(lblStatusBarcode, BorderLayout.CENTER);
        barcodePanel.add(quickDemoPanel, BorderLayout.SOUTH);

        leftPanel.add(barcodePanel, BorderLayout.NORTH);

        // --- Tabel Keranjang Belanja ---
        String[] colNames = {"No", "Barcode ID", "Nama Barang", "Kategori", "Harga Satuan", "Qty", "Subtotal"};
        modelKeranjang = new DefaultTableModel(colNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tblKeranjang = new JTable(modelKeranjang);
        tblKeranjang.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblKeranjang.setRowHeight(28);
        tblKeranjang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tblKeranjang.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Alignment format
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);

        tblKeranjang.getColumnModel().getColumn(0).setPreferredWidth(35);
        tblKeranjang.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblKeranjang.getColumnModel().getColumn(1).setPreferredWidth(90);
        tblKeranjang.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tblKeranjang.getColumnModel().getColumn(2).setPreferredWidth(210);
        tblKeranjang.getColumnModel().getColumn(3).setPreferredWidth(90);
        tblKeranjang.getColumnModel().getColumn(4).setPreferredWidth(90);
        tblKeranjang.getColumnModel().getColumn(4).setCellRenderer(rightRenderer);
        tblKeranjang.getColumnModel().getColumn(5).setPreferredWidth(50);
        tblKeranjang.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);
        tblKeranjang.getColumnModel().getColumn(6).setPreferredWidth(100);
        tblKeranjang.getColumnModel().getColumn(6).setCellRenderer(rightRenderer);

        JScrollPane scrollTable = new JScrollPane(tblKeranjang);
        scrollTable.setBorder(BorderFactory.createTitledBorder("Daftar Barang Belanjaan"));

        // Tombol kontrol keranjang (Hapus & Kosongkan)
        JPanel cartControlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        btnHapusItem = new JButton("🗑 Hapus Item Terpilih");
        btnHapusItem.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnHapusItem.addActionListener(e -> hapusItemTerpilih());

        btnKosongkan = new JButton("❌ Batalkan / Kosongkan Keranjang");
        btnKosongkan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnKosongkan.addActionListener(e -> batalkanKeranjang());

        cartControlPanel.add(btnHapusItem);
        cartControlPanel.add(btnKosongkan);

        JPanel centerCart = new JPanel(new BorderLayout());
        centerCart.add(scrollTable, BorderLayout.CENTER);
        centerCart.add(cartControlPanel, BorderLayout.SOUTH);

        leftPanel.add(centerCart, BorderLayout.CENTER);

        // Area Kanan: Panel Total Bayar Besar (Font Size 28) + Pembayaran Tunai
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setPreferredSize(new Dimension(380, 0));

        // 1. Panel Ringkasan Total Bayar (FONT SIZE 28 SESUAI SPESIFIKASI)
        JPanel totalPanel = new JPanel(new BorderLayout(5, 5));
        totalPanel.setBackground(new Color(20, 30, 45)); // Dark background mewah
        totalPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel lblTotalTitle = new JLabel("TOTAL TAGIHAN");
        lblTotalTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTotalTitle.setForeground(new Color(180, 205, 230));

        // FONT SIZE 28
        lblTotalBayarBesar = new JLabel("Rp 0", SwingConstants.RIGHT);
        lblTotalBayarBesar.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTotalBayarBesar.setForeground(new Color(46, 213, 115)); // Hijau terang mencolok

        totalPanel.add(lblTotalTitle, BorderLayout.NORTH);
        totalPanel.add(lblTotalBayarBesar, BorderLayout.CENTER);

        // 2. Form Pembayaran Tunai
        JPanel paymentForm = new JPanel(new GridLayout(6, 1, 6, 6));
        paymentForm.setBorder(BorderFactory.createCompoundBorder(
                new TitledBorder(new LineBorder(new Color(200, 200, 200)), "Pembayaran Kasir Tunai"),
                new EmptyBorder(10, 12, 10, 12)
        ));

        paymentForm.add(new JLabel("Uang Tunai Diterima (Rp):"));
        txtNominalTunai = new JTextField();
        txtNominalTunai.setFont(new Font("Segoe UI", Font.BOLD, 18));
        txtNominalTunai.setHorizontalAlignment(JTextField.RIGHT);
        txtNominalTunai.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                hitungKembalianLive();
            }
        });
        txtNominalTunai.addActionListener(e -> prosesTransaksiPembayaran());
        paymentForm.add(txtNominalTunai);

        // Tombol cepat uang pecahan
        JPanel quickCash = new JPanel(new GridLayout(1, 4, 4, 4));
        String[] pecahan = {"Pas", "20rb", "50rb", "100rb"};
        double[] nominal = {0, 20000, 50000, 100000};
        for (int i = 0; i < pecahan.length; i++) {
            final int idx = i;
            JButton btnUang = new JButton(pecahan[i]);
            btnUang.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btnUang.addActionListener(e -> {
                if (idx == 0) {
                    txtNominalTunai.setText(String.valueOf((long) keranjang.hitungSubtotal()));
                } else {
                    txtNominalTunai.setText(String.valueOf((long) nominal[idx]));
                }
                hitungKembalianLive();
            });
            quickCash.add(btnUang);
        }
        paymentForm.add(quickCash);

        paymentForm.add(new JLabel("Kembalian Kasir:"));
        lblKembalian = new JLabel("Rp 0", SwingConstants.RIGHT);
        lblKembalian.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblKembalian.setForeground(new Color(20, 100, 220));
        paymentForm.add(lblKembalian);

        // Tombol Selesaikan Transaksi
        btnBayar = new JButton("✔ BAYAR & SELESAI TRANSAKSI");
        btnBayar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnBayar.setBackground(new Color(46, 175, 95));
        btnBayar.setForeground(Color.WHITE);
        btnBayar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBayar.addActionListener(e -> prosesTransaksiPembayaran());

        JPanel paymentContainer = new JPanel(new BorderLayout(10, 10));
        paymentContainer.add(totalPanel, BorderLayout.NORTH);
        paymentContainer.add(paymentForm, BorderLayout.CENTER);
        paymentContainer.add(btnBayar, BorderLayout.SOUTH);

        rightPanel.add(paymentContainer, BorderLayout.NORTH);

        // Petunjuk Cepat Demo
        JPanel guidePanel = new JPanel(new BorderLayout(5, 5));
        guidePanel.setBorder(BorderFactory.createTitledBorder("💡 Petunjuk Demo Expo"));
        JTextArea txtGuide = new JTextArea(
                "Langkah Cepat Demo:\n" +
                "1. Ketik salah satu barcode di atas (misal: 8991001) lalu tekan ENTER.\n" +
                "2. Barang langsung muncul di tabel dan kuantitas bertambah.\n" +
                "3. Bila melebihi sisa stok gudang, muncul validasi otomatis!\n" +
                "4. Masukkan nominal uang / klik tombol pecahan.\n" +
                "5. Tekan Enter / klik BAYAR. Struk muncul, stok di file otomatis terpotong!"
        );
        txtGuide.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtGuide.setBackground(new Color(245, 248, 252));
        txtGuide.setEditable(false);
        txtGuide.setLineWrap(true);
        txtGuide.setWrapStyleWord(true);
        guidePanel.add(txtGuide, BorderLayout.CENTER);

        rightPanel.add(guidePanel, BorderLayout.CENTER);

        panel.add(leftPanel, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createStokGudangPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topBar.add(new JLabel("Pencarian Barang Gudang:"));
        txtCariStok = new JTextField(20);
        txtCariStok.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtCariStok.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                filterTabelStok(txtCariStok.getText());
            }
        });
        topBar.add(txtCariStok);

        JButton btnMuatUlang = new JButton("🔄 Muat Ulang dari File");
        btnMuatUlang.addActionListener(e -> {
            dataManager.muatDataMaster();
            refreshTabelStok();
            JOptionPane.showMessageDialog(this, "Data master stok berhasil dimuat ulang dari " + DataManager.FILE_MASTER_STOK, "Info", JOptionPane.INFORMATION_MESSAGE);
        });
        topBar.add(btnMuatUlang);

        panel.add(topBar, BorderLayout.NORTH);

        String[] cols = {"Barcode ID", "Nama Barang", "Kategori", "Harga Jual", "Sisa Stok Gudang", "Status Stok"};
        modelStokGudang = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        tblStokGudang = new JTable(modelStokGudang);
        tblStokGudang.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tblStokGudang.setRowHeight(26);
        tblStokGudang.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tblStokGudang.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tblStokGudang.getColumnModel().getColumn(4).setCellRenderer(centerRenderer);
        tblStokGudang.getColumnModel().getColumn(5).setCellRenderer(centerRenderer);

        JScrollPane scroll = new JScrollPane(tblStokGudang);
        scroll.setBorder(BorderFactory.createTitledBorder("Live Monitor Data Stok Barang (Sumber: master_stok_barang.txt)"));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRekapPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton btnRefresh = new JButton("🔄 Muat Ulang Rekap");
        btnRefresh.addActionListener(e -> refreshTampilanRekap());
        topBar.add(btnRefresh);

        JLabel lblInfo = new JLabel("Menampilkan catatan histori transaksi dari file: " + DataManager.FILE_REKAP_TRANSAKSI);
        lblInfo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        topBar.add(lblInfo);

        panel.add(topBar, BorderLayout.NORTH);

        txtAreaRekap = new JTextArea();
        txtAreaRekap.setFont(new Font("Consolas", Font.PLAIN, 13));
        txtAreaRekap.setEditable(false);
        txtAreaRekap.setBackground(new Color(250, 250, 250));

        JScrollPane scroll = new JScrollPane(txtAreaRekap);
        scroll.setBorder(BorderFactory.createTitledBorder("Log Transaksi Kasir"));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createFooterPanel() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBorder(new EmptyBorder(4, 15, 4, 15));
        footer.setBackground(new Color(235, 240, 245));

        JLabel lblLeft = new JLabel("Kopma Mart POS v1.0 • Sistem Kasir Cepat Berbasis Java Swing & OOP");
        lblLeft.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        JLabel lblRight = new JLabel("Shortcut: [Enter] Tambah Barcode / Bayar • [F2] Fokus Barcode");
        lblRight.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        footer.add(lblLeft, BorderLayout.WEST);
        footer.add(lblRight, BorderLayout.EAST);
        return footer;
    }

    /**
     * PROSES INPUT BARCODE DENGAN TOMBOL ENTER (FITUR UTAMA)
     */
    private void prosesInputBarcode() {
        String input = txtBarcode.getText().trim();
        if (input.isEmpty()) {
            return;
        }

        Barang barang = dataManager.cariBarang(input);
        if (barang == null) {
            Toolkit.getDefaultToolkit().beep();
            lblStatusBarcode.setText("❌ Barcode \"" + input + "\" tidak ditemukan dalam master data!");
            lblStatusBarcode.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this,
                    "Barcode \"" + input + "\" tidak terdaftar dalam sistem!",
                    "Barang Tidak Ditemukan",
                    JOptionPane.WARNING_MESSAGE);
            txtBarcode.selectAll();
            txtBarcode.requestFocus();
            return;
        }

        // Cek stok gudang: Validasi Stok
        if (barang.getStokGudang() <= 0) {
            Toolkit.getDefaultToolkit().beep();
            lblStatusBarcode.setText("⚠ Stok barang " + barang.getNamaBarang() + " HABIS di gudang!");
            lblStatusBarcode.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this,
                    "Stok barang \"" + barang.getNamaBarang() + "\" habis di gudang!\nSisa stok: 0",
                    "Peringatan Stok Habis",
                    JOptionPane.WARNING_MESSAGE);
            txtBarcode.setText("");
            txtBarcode.requestFocus();
            return;
        }

        // Hitung total kuantitas yang saat ini sudah ada di keranjang untuk barang ini
        int qtyDiKeranjang = 0;
        for (ItemBelanja item : keranjang.getDaftarItem()) {
            if (item.getBarang().getBarcodeId().equalsIgnoreCase(barang.getBarcodeId())) {
                qtyDiKeranjang = item.getJumlah();
                break;
            }
        }

        if (qtyDiKeranjang + 1 > barang.getStokGudang()) {
            Toolkit.getDefaultToolkit().beep();
            lblStatusBarcode.setText("⚠ Penambahan melebihi stok gudang (" + barang.getStokGudang() + ")!");
            lblStatusBarcode.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this,
                    "Validasi Stok:\nJumlah pembelian untuk \"" + barang.getNamaBarang() + "\" melebihi stok yang ada!\n" +
                    "Stok Gudang: " + barang.getStokGudang() + " | Sudah di Keranjang: " + qtyDiKeranjang,
                    "Peringatan Stok Tidak Cukup",
                    JOptionPane.WARNING_MESSAGE);
            txtBarcode.setText("");
            txtBarcode.requestFocus();
            return;
        }

        // Tambahkan ke keranjang
        keranjang.tambahItem(barang, 1);

        // Suara beep kasir minimarket
        Toolkit.getDefaultToolkit().beep();

        // Refresh tabel keranjang dan total bayar
        refreshTabelKeranjang();

        lblStatusBarcode.setText("✔ Berhasil menambahkan: " + barang.getNamaBarang() + " (" + formatRupiah(barang.getHargaJual()) + ")");
        lblStatusBarcode.setForeground(new Color(25, 130, 60));

        // Bersihkan input barcode dan langsung siap untuk scan berikutnya tanpa sentuh mouse!
        txtBarcode.setText("");
        txtBarcode.requestFocus();
    }

    private void refreshTabelKeranjang() {
        modelKeranjang.setRowCount(0);
        int no = 1;
        for (ItemBelanja item : keranjang.getDaftarItem()) {
            modelKeranjang.addRow(new Object[]{
                    no++,
                    item.getBarang().getBarcodeId(),
                    item.getBarang().getNamaBarang(),
                    item.getBarang().getKategori(),
                    formatRupiah(item.getBarang().getHargaJual()),
                    item.getJumlah(),
                    formatRupiah(item.getSubtotal())
            });
        }

        // Update Total Bayar Besar (Font Size 28)
        lblTotalBayarBesar.setText(formatRupiah(keranjang.hitungSubtotal()));
        hitungKembalianLive();
    }

    private void hapusItemTerpilih() {
        int selectedRow = tblKeranjang.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Pilih salah satu item di tabel keranjang untuk dihapus!", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String barcode = (String) modelKeranjang.getValueAt(selectedRow, 1);
        keranjang.hapusItem(barcode);
        refreshTabelKeranjang();
        txtBarcode.requestFocus();
    }

    private void batalkanKeranjang() {
        if (keranjang.getDaftarItem().isEmpty()) return;
        int conf = JOptionPane.showConfirmDialog(this, "Yakin ingin membatalkan semua belanjaan dalam keranjang?", "Konfirmasi Batalkan", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            keranjang.kosongkan();
            refreshTabelKeranjang();
            txtNominalTunai.setText("");
            lblKembalian.setText("Rp 0");
            lblStatusBarcode.setText("Keranjang belanja telah dikosongkan.");
            txtBarcode.requestFocus();
        }
    }

    private void hitungKembalianLive() {
        double total = keranjang.hitungSubtotal();
        String strTunai = txtNominalTunai.getText().trim();
        if (strTunai.isEmpty()) {
            lblKembalian.setText("Rp 0");
            lblKembalian.setForeground(new Color(20, 100, 220));
            return;
        }

        try {
            double tunai = Double.parseDouble(strTunai);
            double kembalian = tunai - total;
            if (kembalian >= 0) {
                lblKembalian.setText(formatRupiah(kembalian));
                lblKembalian.setForeground(new Color(25, 130, 60));
            } else {
                lblKembalian.setText("Kurang: " + formatRupiah(Math.abs(kembalian)));
                lblKembalian.setForeground(Color.RED);
            }
        } catch (NumberFormatException e) {
            lblKembalian.setText("Nominal Salah");
            lblKembalian.setForeground(Color.RED);
        }
    }

    /**
     * PROSES PEMBAYARAN KASIR, PEMOTONGAN STOK, PENYIMPANAN KE FILE
     */
    private void prosesTransaksiPembayaran() {
        if (keranjang.getDaftarItem().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Keranjang belanja masih kosong! Silakan scan barang terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            txtBarcode.requestFocus();
            return;
        }

        double total = keranjang.hitungSubtotal();
        String strTunai = txtNominalTunai.getText().trim();

        if (strTunai.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Masukkan nominal pembayaran tunai terlebih dahulu!", "Validasi Pembayaran", JOptionPane.WARNING_MESSAGE);
            txtNominalTunai.requestFocus();
            return;
        }

        double tunai;
        try {
            tunai = Double.parseDouble(strTunai);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Format nominal tunai tidak valid!", "Error", JOptionPane.ERROR_MESSAGE);
            txtNominalTunai.requestFocus();
            return;
        }

        if (tunai < total) {
            JOptionPane.showMessageDialog(this,
                    "Uang tunai tidak mencukupi!\nTotal: " + formatRupiah(total) + "\nTunai: " + formatRupiah(tunai) + "\nKekurangan: " + formatRupiah(total - tunai),
                    "Uang Kurang",
                    JOptionPane.WARNING_MESSAGE);
            txtNominalTunai.requestFocus();
            return;
        }

        // 1. Generate Nomor Nota Kasir
        String tanggalNota = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String noNota = String.format("NOTA-%s-%04d", tanggalNota, counterNota++);

        // 2. Buat objek TransaksiPenjualan
        TransaksiPenjualan transaksi = new TransaksiPenjualan(noNota, LocalDateTime.now(), total, tunai, keranjang.getDaftarItem());

        // 3. Kurangi stok gudang untuk setiap barang yang dibeli
        for (ItemBelanja item : keranjang.getDaftarItem()) {
            Barang master = dataManager.cariBarang(item.getBarang().getBarcodeId());
            if (master != null) {
                master.kurangiStok(item.getJumlah());
            }
        }

        // 4. Data Persistence: Simpan perubahan stok ke master_stok_barang.txt
        boolean simpanStokOk = dataManager.simpanSemuaBarang();

        // 5. Data Persistence: Catat riwayat transaksi ke rekap_transaksi_kasir.txt
        boolean catatRekapOk = dataManager.catatRekapTransaksi(transaksi);

        // 6. Tampilkan Struk Pembelian
        String strukText = transaksi.cetakStruk();
        tampilkanDialogStruk(strukText, noNota);

        // 7. Reset State Kasir
        keranjang.kosongkan();
        refreshTabelKeranjang();
        txtNominalTunai.setText("");
        lblKembalian.setText("Rp 0");
        lblStatusBarcode.setText("✔ Transaksi " + noNota + " berhasil diselesaikan. File stok & rekap telah diperbarui.");
        lblStatusBarcode.setForeground(new Color(25, 130, 60));

        // Refresh tabel stok live & rekap
        refreshTabelStok();
        refreshTampilanRekap();

        txtBarcode.requestFocus();
    }

    private void tampilkanDialogStruk(String struk, String noNota) {
        JDialog dialog = new JDialog(this, "Struk Kasir: " + noNota, true);
        dialog.setSize(440, 520);
        dialog.setLocationRelativeTo(this);

        JTextArea area = new JTextArea(struk);
        area.setFont(new Font("Consolas", Font.PLAIN, 12));
        area.setEditable(false);
        area.setBackground(new Color(255, 255, 245));
        area.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnTutup = new JButton("Tutup Struk (Siap Transaksi Baru)");
        btnTutup.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnTutup.addActionListener(e -> dialog.dispose());
        btnPanel.add(btnTutup);

        dialog.getContentPane().add(new JScrollPane(area), BorderLayout.CENTER);
        dialog.getContentPane().add(btnPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void refreshTabelStok() {
        filterTabelStok("");
    }

    private void filterTabelStok(String query) {
        if (modelStokGudang == null) return;
        modelStokGudang.setRowCount(0);
        String q = query.toLowerCase().trim();

        for (Barang b : dataManager.getSemuaBarang()) {
            boolean match = q.isEmpty() ||
                    b.getBarcodeId().toLowerCase().contains(q) ||
                    b.getNamaBarang().toLowerCase().contains(q) ||
                    b.getKategori().toLowerCase().contains(q);

            if (match) {
                String status = b.getStokGudang() > 10 ? "Tersedia" : (b.getStokGudang() > 0 ? "Menipis" : "Habis");
                modelStokGudang.addRow(new Object[]{
                        b.getBarcodeId(),
                        b.getNamaBarang(),
                        b.getKategori(),
                        formatRupiah(b.getHargaJual()),
                        b.getStokGudang(),
                        status
                });
            }
        }
    }

    private void refreshTampilanRekap() {
        if (txtAreaRekap == null) return;
        List<String> lines = dataManager.bacaRekapTransaksi();
        if (lines.isEmpty()) {
            txtAreaRekap.setText("Belum ada transaksi yang tercatat.");
        } else {
            StringBuilder sb = new StringBuilder();
            for (String l : lines) {
                sb.append(l).append("\n");
            }
            txtAreaRekap.setText(sb.toString());
        }
    }

    private String formatRupiah(double nilai) {
        return nfRupiah.format(nilai).replace("Rp", "Rp ");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            KopmaMartApp app = new KopmaMartApp();
            app.setVisible(true);
            // Fokus otomatis ke input barcode
            app.txtBarcode.requestFocusInWindow();
        });
    }
}
