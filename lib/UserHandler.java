import java.io.*;
import java.util.Scanner;

public class UserHandler {
    private static final String USER_FILE = "users.txt";

    // --- METHOD BARU: Cek apakah username sudah ada? ---
    private boolean isUsernameExists(String username) {
        File file = new File(USER_FILE);
        if (!file.exists()) return false; // Kalau file belum ada, berarti username aman

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] data = line.split(":"); 
                // data[0] adalah username
                if (data.length >= 1) {
                    if (data[0].equals(username)) {
                        return true; // BAHAYA! Username sudah ditemukan
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false; // Aman, username belum ada
    }

    // --- UPDATE SIGN UP ---
    public boolean signUp(String username, String password, String role) {
        // 1. CEK DULU: Kalau username sudah ada, langsung tolak!
        if (isUsernameExists(username)) {
            System.out.println("Gagal: Username " + username + " sudah terpakai.");
            return false; 
        }

        // 2. Kalau aman, baru simpan ke file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE, true))) {
            writer.write(username + ":" + password + ":" + role);
            writer.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- LOGIN (Tetap Sama) ---
    public String login(String username, String password) {
        File file = new File(USER_FILE);
        if (!file.exists()) return null; 

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] data = line.split(":"); 
                if (data.length == 3) {
                    if (data[0].equals(username) && data[1].equals(password)) {
                        return data[2]; 
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null; 
    }
}