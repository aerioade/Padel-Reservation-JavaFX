import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

public class MainApp extends Application {

    // --- LOGIC BACKEND ---
    private DatabaseHandler db = new DatabaseHandler();
    private UserHandler userHandler = new UserHandler();
    private ArrayList<Lapangan> listLapangan = new ArrayList<>();
    
    // --- UI STATE ---
    private Stage primaryStage;
    private boolean isAdmin = false;
    private int currentLapIndex = 0;
    
    // --- KOMPONEN UI ---
    private Label judulKanan, deskripsiLabel;
    private ImageView gambarView;
    private VBox jadwalContainer;

    // --- SECURITY CONFIG ---
    private static final String ADMIN_SECRET_CODE = "nohackersyet537219"; // Kode Rahasia

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        isiDataLapangan(); 
        showLoginScene();
        stage.setTitle("Padel Reservation System (Secured)");
        stage.show();
    }

    // ==========================================
    // BAGIAN 1: HALAMAN LOGIN & SIGN UP
    // ==========================================
    private void showLoginScene() {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #001f3f;"); 

        Label title = new Label("LOGIN SYSTEM");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));

        TextField userField = new TextField(); userField.setPromptText("Username");
        PasswordField passField = new PasswordField(); passField.setPromptText("Password");
        
        userField.setMaxWidth(250); userField.setStyle("-fx-background-radius: 10;");
        passField.setMaxWidth(250); passField.setStyle("-fx-background-radius: 10;");

        Button btnLogin = new Button("LOGIN");
        styleButton(btnLogin, "#0074D9"); btnLogin.setPrefWidth(250);

        Button btnSignUp = new Button("SIGN UP (DAFTAR)");
        styleButton(btnSignUp, "#2ECC40"); btnSignUp.setPrefWidth(250);

        Label msgLabel = new Label();
        msgLabel.setTextFill(Color.RED);

        // Logic Login
        btnLogin.setOnAction(e -> {
            String username = userField.getText();
            String password = passField.getText();
            
            // Backdoor Admin (Bisa dihapus kalau mau full secure)
            if (username.equals("admin") && password.equals("admin")) {
                this.isAdmin = true; showMainScene(); return;
            }

            String role = userHandler.login(username, password);
            if (role != null) {
                this.isAdmin = role.equalsIgnoreCase("admin");
                showMainScene();
            } else {
                msgLabel.setText("Username atau Password Salah!");
            }
        });

        btnSignUp.setOnAction(e -> showSignUpDialog());

        root.getChildren().addAll(title, userField, passField, btnLogin, btnSignUp, msgLabel);
        Scene scene = new Scene(root, 450, 500);
        primaryStage.setScene(scene);
    }

    // ==========================================
    // BAGIAN 2: DASHBOARD UTAMA
    // ==========================================
    private void showMainScene() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #001f3f;"); 

        // SIDEBAR
        VBox sidebar = new VBox(12);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #001a35; -fx-border-color: #333333; -fx-border-width: 0 1 0 0;");
        sidebar.setPrefWidth(260);

        Label lblDaftar = new Label("DAFTAR LAPANGAN");
        lblDaftar.setTextFill(Color.WHITE);
        lblDaftar.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        sidebar.getChildren().add(lblDaftar);
        
        for (int i = 0; i < listLapangan.size(); i++) {
            Button btnLap = new Button(listLapangan.get(i).getNama());
            btnLap.setMaxWidth(Double.MAX_VALUE);
            styleButton(btnLap, "#0074D9"); 
            final int idx = i;
            btnLap.setOnAction(e -> updateDetail(idx));
            sidebar.getChildren().add(btnLap);
        }
        
        Region spacer = new Region(); VBox.setVgrow(spacer, Priority.ALWAYS);
        Button btnLogout = new Button("LOG OUT");
        styleButton(btnLogout, "#FF4136");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setOnAction(e -> showLoginScene());
        
        sidebar.getChildren().addAll(spacer, btnLogout);
        mainLayout.setLeft(sidebar);

        // KONTEN
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);

        judulKanan = new Label();
        judulKanan.setTextFill(Color.WHITE);
        judulKanan.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));

        gambarView = new ImageView();
        gambarView.setFitWidth(450); gambarView.setFitHeight(280);
        gambarView.setPreserveRatio(true);

        deskripsiLabel = new Label();
        deskripsiLabel.setTextFill(Color.LIGHTGRAY);
        deskripsiLabel.setFont(Font.font("Segoe UI", 14));

        Label lblPilih = new Label("PILIH JAM RESERVASI");
        lblPilih.setTextFill(Color.WHITE);
        lblPilih.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        
        jadwalContainer = new VBox(10);
        jadwalContainer.setAlignment(Pos.CENTER);

        content.getChildren().addAll(judulKanan, gambarView, deskripsiLabel, new Separator(), lblPilih, jadwalContainer);
        mainLayout.setCenter(content);

        updateDetail(0);
        Scene scene = new Scene(mainLayout, 1100, 700);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
    }

    // --- UPDATE DETAIL ---
    private void updateDetail(int index) {
        this.currentLapIndex = index;
        Lapangan lap = listLapangan.get(index);
        judulKanan.setText(lap.getNama());
        deskripsiLabel.setText(lap.getDeskripsi());
        
        try {
            File fileImg = new File(lap.getPathFoto());
            if (fileImg.exists()) gambarView.setImage(new Image(fileImg.toURI().toString()));
            else gambarView.setImage(null);
        } catch (Exception e) { e.printStackTrace(); }

        jadwalContainer.getChildren().clear();
        String[] jam = {"08.00 - 09.00", "10.00 - 11.00", "13.00 - 14.00", "15.00 - 16.00", "17.00 - 18.00"};

        for (int i = 0; i < jam.length; i++) {
            final int idxJam = i;
            Button btnJam = new Button(jam[i]);
            btnJam.setPrefWidth(400);
            btnJam.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            
            boolean isBooked = db.cekStatusBooking(currentLapIndex, i);
            
            if (isBooked) {
                if (isAdmin) {
                    btnJam.setText("BOOKED - HAPUS (ADMIN)");
                    btnJam.setStyle("-fx-background-color: #FF4136; -fx-text-fill: white; -fx-background-radius: 20;");
                    btnJam.setOnAction(e -> handleAdminDelete(idxJam));
                } else {
                    btnJam.setText("SUDAH DIPESAN");
                    btnJam.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-background-radius: 20;");
                    btnJam.setDisable(true);
                }
            } else {
                btnJam.setText(jam[i] + " (Tersedia)");
                btnJam.setStyle("-fx-background-color: #DDDDDD; -fx-text-fill: black; -fx-background-radius: 20;");
                btnJam.setOnMouseEntered(e -> btnJam.setStyle("-fx-background-color: #AAAAAA; -fx-text-fill: black; -fx-background-radius: 20;"));
                btnJam.setOnMouseExited(e -> btnJam.setStyle("-fx-background-color: #DDDDDD; -fx-text-fill: black; -fx-background-radius: 20;"));
                btnJam.setOnAction(e -> showBookingDialog(jam[idxJam], idxJam));
            }
            jadwalContainer.getChildren().add(btnJam);
        }
    }

    // ==========================================
    // BAGIAN 3: SIGN UP SECURE (FITUR BARU)
    // ==========================================
    private void showSignUpDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Sign Up");
        dialog.setHeaderText("Daftar User Baru");
        
        ButtonType daftarType = new ButtonType("Daftar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(daftarType, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 50, 10, 10));
        
        TextField user = new TextField(); user.setPromptText("Username");
        PasswordField pass = new PasswordField(); pass.setPromptText("Password");
        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("user", "admin");
        roleBox.setValue("user");
        
        grid.add(new Label("Username:"), 0, 0); grid.add(user, 1, 0);
        grid.add(new Label("Password:"), 0, 1); grid.add(pass, 1, 1);
        grid.add(new Label("Role:"), 0, 2); grid.add(roleBox, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        
        // --- LOGIC TOMBOL DAFTAR + SECURITY CHECK ---
        final Button btnDaftar = (Button) dialog.getDialogPane().lookupButton(daftarType);
        
        btnDaftar.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String username = user.getText();
            String password = pass.getText();
            String role = roleBox.getValue();

            // 1. Validasi Input Kosong
            if (username.isEmpty() || password.isEmpty()) {
                showAlert("Username dan Password tidak boleh kosong!");
                event.consume(); 
                return;
            }

            // 2. SECURITY CHECK: Jika daftar jadi ADMIN, minta KODE RAHASIA
            if (role.equals("admin")) {
                TextInputDialog secretDialog = new TextInputDialog();
                secretDialog.setTitle("Security Check");
                secretDialog.setHeaderText("Verifikasi Admin");
                secretDialog.setContentText("Masukkan Kode Rahasia Admin:");
                
                Optional<String> result = secretDialog.showAndWait();
                if (result.isPresent()) {
                    if (!result.get().equals(ADMIN_SECRET_CODE)) {
                        showAlert("YAHAHAHA PENGEN BANGET JADI ADMIN YA.");
                        event.consume(); // Batalkan Sign Up
                        return;
                    }
                } else {
                    event.consume(); // Batal kalau dicancel
                    return;
                }
            }

            // ... (kode security check admin di atasnya tetap sama) ...

            // 3. Jika Lolos Security, Simpan ke Database Text
            boolean success = userHandler.signUp(username, password, role);
            
            if (success) {
                showAlertInfo("Sign Up Berhasil! Role: " + role + "\nSilakan Login.");
            } else {
                // UPDATE PESAN ERROR DISINI
                showAlert("Gagal Sign Up!\nKemungkinan Username '" + username + "' sudah dipakai.\nSilakan gunakan username lain.");
            }
        });

        dialog.showAndWait();
    }

    // --- FORM BOOKING ---
    private void showBookingDialog(String jamText, int indexJam) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Form Booking");
        dialog.setHeaderText("Booking: " + jamText);
        ButtonType bookType = new ButtonType("Konfirmasi", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(bookType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 150, 10, 10));
        TextField nama = new TextField(); TextField hp = new TextField();
        grid.add(new Label("Nama:"), 0, 0); grid.add(nama, 1, 0);
        grid.add(new Label("HP:"), 0, 1); grid.add(hp, 1, 1);
        dialog.getDialogPane().setContent(grid);

        final Button btnOk = (Button) dialog.getDialogPane().lookupButton(bookType);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            String n = nama.getText(); String h = hp.getText();
            if (n.isEmpty() || h.isEmpty()) { showAlert("Isi semua data!"); event.consume(); return; }
            if (!n.matches("[a-zA-Z ]+")) { showAlert("Nama harus huruf!"); event.consume(); return; }
            if (!h.matches("\\d+") || h.length() < 8 || h.length() > 15) { showAlert("HP Invalid (8-15 digit)!"); event.consume(); return; }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin data benar?");
            Optional<ButtonType> res = confirm.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                db.simpanBooking(currentLapIndex, indexJam, n, h);
                updateDetail(currentLapIndex);
            } else {
                event.consume();
            }
        });
        dialog.showAndWait();
    }

    private void handleAdminDelete(int indexJam) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Hapus Reservasi?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            db.hapusBooking(currentLapIndex, indexJam);
            updateDetail(currentLapIndex);
        }
    }

    // Helpers
    private void styleButton(Button btn, String hex) {
        String base = "-fx-background-color: " + hex + "; -fx-text-fill: white; -fx-background-radius: 30; -fx-font-weight: bold; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: derive(" + hex + ", 20%); -fx-text-fill: white; -fx-background-radius: 30;"));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }
    private void showAlert(String msg) { new Alert(Alert.AlertType.ERROR, msg).show(); }
    private void showAlertInfo(String msg) { new Alert(Alert.AlertType.INFORMATION, msg).show(); }

    private void isiDataLapangan() {
        listLapangan.add(new Lapangan("LAPANGAN A", "Rumput Sintetis - Pasar Gedhe", "lib/img/1.jpeg"));
        listLapangan.add(new Lapangan("LAPANGAN B", "Indoor AC - Manahan", "lib/img/2.jpeg"));
        listLapangan.add(new Lapangan("LAPANGAN C", "Outdoor - Palur", "lib/img/3.jpeg"));
        listLapangan.add(new Lapangan("LAPANGAN D", "Pro - Kartasura", "lib/img/4.jpeg"));
        listLapangan.add(new Lapangan("LAPANGAN E", "Standard - UNS", "lib/img/5.jpeg"));
    }

    public static void main(String[] args) {
        launch(args);
    }
}