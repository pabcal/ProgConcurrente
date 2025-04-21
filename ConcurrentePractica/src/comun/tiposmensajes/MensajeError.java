package comun.tiposmensajes;

import comun.Mensaje;

public class MensajeError extends Mensaje {
    private String mensaje;

    public MensajeError(String mensaje) {
        this.tipo = 99;
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }
}