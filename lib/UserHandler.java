import java.io.*;
import java.util.Scanner;

public class UserHandler {
    private static final String USER_FILE = "users.txt";

    public boolean signUp(String username, String password, String role) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(USER_FILE, true))) {
            writer.write(username + ":" + password + ":" + role);
            writer.newLine();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String login(String username, String password) {
        File file = new File(USER_FILE);
        if (!file.exists()) return null; 

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] data = line.split(":"); 
                if (data.length == 3) {
                    if (data[0].equals(username) && data[1].equals(password)) {
                        return data[2]; // Return role (admin/user)
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null; 
    }
}