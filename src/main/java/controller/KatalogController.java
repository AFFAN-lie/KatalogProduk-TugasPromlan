package controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import data.ProdukRepository;
import model.Produk;

public class KatalogController {

    @FXML
    private TextField namaField;
    @FXML
    private TextField hargaField;
    @FXML
    private ComboBox<String> fotoBox;
    @FXML
    private Label pesanLabel;
    @FXML
    private Label jumlahLabel;
    @FXML
    private TilePane katalogPane;
    private final ProdukRepository repository = new ProdukRepository();
    @FXML 
    private javafx.scene.control.Button simpanButton;
    @FXML 
    private javafx.scene.control.Button batalButton;
    private Long editProductId = null;

    @FXML
    private void initialize() {
        fotoBox.getItems().add("roti.png");
        fotoBox.getItems().add("susu.png");
        fotoBox.getItems().add("kopi.png");
        fotoBox.setValue("roti.png");
        try {
            showData();
        } catch (SQLException e) {
            pesanLabel.setText("Gagal membaca database. Lihat Output.");
            e.printStackTrace();
        }
    }
    
    @FXML
    private void cancelEdit() {
        editProductId = null;
        namaField.clear();
        hargaField.clear();
        fotoBox.setValue("roti.png");
        simpanButton.setText("Simpan");
        batalButton.setVisible(false);
        batalButton.setManaged(false);
    }

    private void showData() throws SQLException {
        ArrayList<Produk> daftar = repository.getAll();
        katalogPane.getChildren().clear();
        for (Produk produk : daftar) {
            VBox kartu = createProductCard(produk);
            katalogPane.getChildren().add(kartu);
        }
        jumlahLabel.setText("Jumlah produk: " + daftar.size());
    }

    private VBox createProductCard(Produk produk) {
        ImageView fotoView = new ImageView();
        fotoView.setFitWidth(170);
        fotoView.setFitHeight(110);
        fotoView.setPreserveRatio(true);
        StackPane bingkai = new StackPane();
        bingkai.setPrefSize(180, 120);
        bingkai.setMinSize(180, 120);
        bingkai.setMaxSize(180, 120);

        URL lokasi = getClass().getResource("/image/" + produk.getFoto());
        if (lokasi != null) {
            Image gambar = new Image(lokasi.toExternalForm(),
                    170, 110, true, true);
            if (!gambar.isError()) {
                fotoView.setImage(gambar);
                bingkai.getChildren().add(fotoView);
            } else {
                bingkai.getChildren().add(new Label("Gambar tidak terbaca"));
            }
        } else {
            bingkai.getChildren().add(new Label("Gambar tidak tersedia"));
        }

        Label namaLabel = new Label(produk.getNama());
        namaLabel.setWrapText(true);
        namaLabel.getStyleClass().add("nama-produk");
        Label hargaLabel = new Label("Rp" + produk.getHarga());
        Label idLabel = new Label("ID: " + produk.getId());
        idLabel.getStyleClass().add("muted");
        
        javafx.scene.control.Button editBtn = new javafx.scene.control.Button("Edit");
        editBtn.setOnAction(e -> {
            editProductId = produk.getId();
            namaField.setText(produk.getNama());
            hargaField.setText(String.valueOf(produk.getHarga()));
            fotoBox.setValue(produk.getFoto());
            simpanButton.setText("Update");
            batalButton.setVisible(true);
            batalButton.setManaged(true);
        });

        javafx.scene.control.Button hapusBtn = new javafx.scene.control.Button("Hapus");
        hapusBtn.setOnAction(e -> {
            try {
                repository.delete(produk.getId());
                showData();
                pesanLabel.setText("Produk dihapus.");
                if (editProductId != null && editProductId.equals(produk.getId())) {
                    cancelEdit(); // Reset form jika produk yang sedang diedit dihapus
                }
            } catch (SQLException ex) {
                pesanLabel.setText("Gagal menghapus produk.");
                ex.printStackTrace();
            }
        });

        javafx.scene.layout.HBox aksiBox = new javafx.scene.layout.HBox(8, editBtn, hapusBtn);

        VBox kartu = new VBox();
        kartu.setSpacing(8);
        kartu.setMinWidth(220);
        kartu.setPrefWidth(220);
        kartu.setMaxWidth(220);
        kartu.getStyleClass().add("kartu-produk");
        kartu.getChildren().add(bingkai);
        kartu.getChildren().add(namaLabel);
        kartu.getChildren().add(hargaLabel);
        kartu.getChildren().add(idLabel);
        kartu.getChildren().add(aksiBox);
        return kartu;
    }

    @FXML
    private void saveProduct() {
        Produk produk;
        try {
            String nama = namaField.getText();
            long harga = Long.parseLong(hargaField.getText().trim());
            String foto = fotoBox.getValue();
            if (editProductId != null) {
                produk = new Produk(editProductId, nama, harga, foto);
            } else {
                produk = new Produk(nama, harga, foto);
            }
        } catch (NumberFormatException e) {
            pesanLabel.setText("Harga harus angka bulat, contoh 12000.");
            return;
        } catch (IllegalArgumentException e) {
            pesanLabel.setText(e.getMessage());
            return;
        }
        try {
            if (editProductId != null) {
                repository.update(produk);
                pesanLabel.setText("Produk berhasil diperbarui.");
            } else {
                repository.add(produk);
                pesanLabel.setText("Produk berhasil disimpan.");
            }
        } catch (SQLException e) {
            pesanLabel.setText("Gagal menyimpan. Lihat Output.");
            e.printStackTrace();
            return;
        }
        cancelEdit(); 
        try {
            showData();
        } catch (SQLException e) {
            pesanLabel.setText("Katalog gagal dimuat ulang.");
            e.printStackTrace();
        }
        
        namaField.clear();
        hargaField.clear();
        try {
            showData();
            pesanLabel.setText("Produk berhasil disimpan.");
        } catch (SQLException e) {
            pesanLabel.setText("Data tersimpan, tetapi katalog gagal dimuat ulang.");
            e.printStackTrace();
        }
    }
}
