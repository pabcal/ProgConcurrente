package comun.tiposmensajes;

import comun.Mensaje;

public class MensajeFinSesion extends Mensaje {
    private String nombreUsuario;

    public MensajeFinSesion(String nombreUsuario) {
        this.tipo = 6;
        this.nombreUsuario = nombreUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }
}