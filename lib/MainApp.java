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

    // Panggil Logic dari file sebelah
    private DatabaseHandler db = new DatabaseHandler();
    private UserHandler userHandler = new UserHandler();
    private ArrayList<Lapangan> listLapangan = new ArrayList<>();
    
    private Stage primaryStage;
    private boolean isAdmin = false;
    private int currentLapIndex = 0;
    
    // Komponen UI
    private Label judulKanan, deskripsiLabel;
    private ImageView gambarView;
    private VBox jadwalContainer;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        isiDataLapangan(); 
        
        // Mulai dari halaman Login
        showLoginScene();
        
        stage.setTitle("Sistem Reservasi Padel (JavaFX)");
        stage.show();
    }

    // --- HALAMAN 1: LOGIN ---
    private void showLoginScene() {
        VBox root = new VBox(15);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #001f3f;"); // BACKGROUND BIRU TUA

        Label title = new Label("LOGIN SYSTEM");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));

        TextField userField = new TextField();
        userField.setPromptText("Username");
        userField.setMaxWidth(250);

        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        passField.setMaxWidth(250);

        Button btnLogin = new Button("LOGIN");
        styleButton(btnLogin, "#0074D9"); // Biru Terang

        Button btnSignUp = new Button("SIGN UP (DAFTAR)");
        styleButton(btnSignUp, "#2ECC40"); // Hijau

        Label msgLabel = new Label();
        msgLabel.setTextFill(Color.RED);

        // Aksi Login
        btnLogin.setOnAction(e -> {
            // Backdoor Admin (biar gampang testing)
            if(userField.getText().equals("admin") && passField.getText().equals("admin")) {
                this.isAdmin = true; showMainScene(); return;
            }
            
            String role = userHandler.login(userField.getText(), passField.getText());
            if (role != null) {
                this.isAdmin = role.equalsIgnoreCase("admin");
                showMainScene();
            } else {
                msgLabel.setText("Login Gagal!");
            }
        });

        // Aksi Sign Up
        btnSignUp.setOnAction(e -> showSignUpDialog());

        root.getChildren().addAll(title, userField, passField, btnLogin, btnSignUp, msgLabel);
        Scene scene = new Scene(root, 500, 500);
        primaryStage.setScene(scene);
    }

    // --- HALAMAN 2: DASHBOARD UTAMA ---
    private void showMainScene() {
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #001f3f;"); 

        // SIDEBAR KIRI
        VBox sidebar = new VBox(12);
        sidebar.setPadding(new Insets(20));
        sidebar.setStyle("-fx-background-color: #001a35; -fx-border-color: #aaaaaa; -fx-border-width: 0 1 0 0;");
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
        
        // Spacer & Logout
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Button btnLogout = new Button("LOG OUT");
        styleButton(btnLogout, "#FF4136");
        btnLogout.setMaxWidth(Double.MAX_VALUE);
        btnLogout.setOnAction(e -> showLoginScene()); // Balik ke Login
        
        sidebar.getChildren().addAll(spacer, btnLogout);
        mainLayout.setLeft(sidebar);

        // KONTEN TENGAH
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setAlignment(Pos.TOP_CENTER);

        judulKanan = new Label();
        judulKanan.setTextFill(Color.WHITE);
        judulKanan.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));

        gambarView = new ImageView();
        gambarView.setFitWidth(450);
        gambarView.setFitHeight(280);
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

    // --- UPDATE DETAIL & SLOT JAM ---
    private void updateDetail(int index) {
        this.currentLapIndex = index;
        Lapangan lap = listLapangan.get(index);
        judulKanan.setText(lap.getNama());
        deskripsiLabel.setText(lap.getDeskripsi());
        
        try {
            File fileImg = new File(lap.getPathFoto());
            if (fileImg.exists()) {
                gambarView.setImage(new Image(fileImg.toURI().toString()));
            } else {
                gambarView.setImage(null);
            }
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
                btnJam.setOnAction(e -> showBookingDialog(jam[idxJam], idxJam));
            }
            jadwalContainer.getChildren().add(btnJam);
        }
    }

    // --- FORM RESERVASI ---
    private void showBookingDialog(String jamText, int indexJam) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Form Booking");
        dialog.setHeaderText("Booking: " + jamText);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nama = new TextField(); nama.setPromptText("Nama");
        TextField hp = new TextField(); hp.setPromptText("HP (8-15 Digit)");

        grid.add(new Label("Nama:"), 0, 0); grid.add(nama, 1, 0);
        grid.add(new Label("HP:"), 0, 1); grid.add(hp, 1, 1);
        dialog.getDialogPane().setContent(grid);

        final Button btnOk = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        btnOk.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            if (nama.getText().isEmpty() || hp.getText().isEmpty()) { showAlert("Isi semua data!"); event.consume(); return; }
            if (!nama.getText().matches("[a-zA-Z ]+")) { showAlert("Nama harus huruf!"); event.consume(); return; }
            if (!hp.getText().matches("\\d+") || hp.getText().length() < 8 || hp.getText().length() > 15) { 
                showAlert("HP Invalid!"); event.consume(); return; 
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Yakin data benar?");
            Optional<ButtonType> res = confirm.showAndWait();
            if (res.isPresent() && res.get() == ButtonType.OK) {
                db.simpanBooking(currentLapIndex, indexJam, nama.getText(), hp.getText());
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
    
    private void showSignUpDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Sign Up");
        dialog.setHeaderText("Daftar User Baru");
        ButtonType daftar = new ButtonType("Daftar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(daftar, ButtonType.CANCEL);
        
        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 50, 10, 10));
        TextField user = new TextField(); TextField pass = new PasswordField();
        ComboBox<String> role = new ComboBox<>(); role.getItems().addAll("user", "admin"); role.setValue("user");
        
        grid.add(new Label("User:"), 0, 0); grid.add(user, 1, 0);
        grid.add(new Label("Pass:"), 0, 1); grid.add(pass, 1, 1);
        grid.add(new Label("Role:"), 0, 2); grid.add(role, 1, 2);
        dialog.getDialogPane().setContent(grid);
        
        dialog.setResultConverter(btn -> {
            if(btn == daftar) userHandler.signUp(user.getText(), pass.getText(), role.getValue());
            return null;
        });
        dialog.showAndWait();
    }

    private void styleButton(Button btn, String hex) {
        String base = "-fx-background-color: " + hex + "; -fx-text-fill: white; -fx-background-radius: 30; -fx-font-weight: bold;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: derive(" + hex + ", 20%); -fx-text-fill: white; -fx-background-radius: 30;"));
        btn.setOnMouseExited(e -> btn.setStyle(base));
    }
    
    private void showAlert(String msg) { new Alert(Alert.AlertType.ERROR, msg).show(); }

    private void isiDataLapangan() {
        listLapangan.add(new Lapangan("LAPANGAN A", "Lapangan Rumput", "sendiri/src/lap1.jpg"));
        listLapangan.add(new Lapangan("LAPANGAN B", "Lapangan Indoor", "sendiri/src/lap1.jpg"));
        listLapangan.add(new Lapangan("LAPANGAN C", "Lapangan Outdoor", "sendiri/src/lap1.jpg"));
        listLapangan.add(new Lapangan("LAPANGAN D", "Lapangan Pro", "sendiri/src/lap1.jpg"));
        listLapangan.add(new Lapangan("LAPANGAN E", "Lapangan Standard", "sendiri/src/lap1.jpg"));
    }

    public static void main(String[] args) { launch(args); }
}