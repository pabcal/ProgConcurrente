package comun.tiposmensajes;

import comun.Mensaje;

public class MensajeInfoDescarga extends Mensaje {
    private String direccionIP;
    private int puertoP2P;

    public MensajeInfoDescarga(String direccionIP, int puertoP2P) {
        this.tipo = 5;
        this.direccionIP = direccionIP;
        this.puertoP2P = puertoP2P;
    }

    public String getDireccionIP() {
        return direccionIP;
    }

    public int getPuertoP2P() {
        return puertoP2P;
    }
}