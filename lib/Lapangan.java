public class Lapangan {
    private String nama;
    private String deskripsi;
    private String pathFoto;

    public Lapangan(String nama, String deskripsi, String pathFoto) {
        this.nama = nama;
        this.deskripsi = deskripsi;
        this.pathFoto = pathFoto;
    }

    public String getNama() { 
    return nama;
    }
    public String getDeskripsi() { 
    return deskripsi;
    }
    public String getPathFoto() { 
    return pathFoto;
    }
}