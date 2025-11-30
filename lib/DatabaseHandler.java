import java.io.*;

public class DatabaseHandler {
    private static final String FILE_NAME = "booking_data.dat";
    private static final int SLOT_SIZE = 200;
    private static final int JUMLAH_JAM = 5; 

    public boolean cekStatusBooking(int indexLap, int indexJam) {
        long posisi = ((indexLap * JUMLAH_JAM) + indexJam) * SLOT_SIZE;
        try (RandomAccessFile raf = new RandomAccessFile(FILE_NAME, "r")) {
            if (posisi >= raf.length()) return false;
            raf.seek(posisi);
            return raf.readBoolean();
        } catch (IOException e) {
            return false;
        }
    }

    public void simpanBooking(int indexLap, int indexJam, String nama, String hp) {
        long posisi = ((indexLap * JUMLAH_JAM) + indexJam) * SLOT_SIZE;
        try (RandomAccessFile raf = new RandomAccessFile(FILE_NAME, "rw")) {
            raf.seek(posisi);
            raf.writeBoolean(true);
            raf.writeUTF(nama);
            raf.writeUTF(hp);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void hapusBooking(int indexLap, int indexJam) {
        long posisi = ((indexLap * JUMLAH_JAM) + indexJam) * SLOT_SIZE;
        try (RandomAccessFile raf = new RandomAccessFile(FILE_NAME, "rw")) {
            raf.seek(posisi);
            raf.writeBoolean(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}