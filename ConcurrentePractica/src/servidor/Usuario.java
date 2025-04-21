package servidor;

import java.io.Serializable;
import java.util.List;

public class Usuario implements Serializable {
    private String nombre;
    private String direccionIP;
    private int puertoP2P;
    private List<String> archivosCompartidos;

    public Usuario(String nombre, String direccionIP, int puertoP2P, List<String> archivosCompartidos) {
        this.nombre = nombre;
        this.direccionIP = direccionIP;
        this.puertoP2P = puertoP2P;
        this.archivosCompartidos = archivosCompartidos;
    }

    public String getNombre() { return nombre; }
    public String getDireccionIP() { return direccionIP; }
    public int getPuertoP2P() { return puertoP2P; }
    public List<String> getArchivosCompartidos() { return archivosCompartidos; }
}