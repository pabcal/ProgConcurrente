package comun.tiposmensajes;

import comun.Mensaje;
import java.util.List;

public class MensajeInicioSesion extends Mensaje {
    private String nombreUsuario;
    private String direccionIP;
    private int puertoP2P;
    private List<String> archivosCompartidos;

    public MensajeInicioSesion(String nombreUsuario, String direccionIP, int puertoP2P, List<String> archivosCompartidos) {
        this.tipo = 1;
        this.nombreUsuario = nombreUsuario;
        this.direccionIP = direccionIP;
        this.puertoP2P = puertoP2P;
        this.archivosCompartidos = archivosCompartidos;
    }

    public String getNombreUsuario() { return nombreUsuario; }
    public String getDireccionIP() { return direccionIP; }
    public int getPuertoP2P() { return puertoP2P; }
    public List<String> getArchivosCompartidos() { return archivosCompartidos; }
}
