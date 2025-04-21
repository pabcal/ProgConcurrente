package comun.tiposmensajes;

import comun.Mensaje;

public class MensajePeticionDescarga extends Mensaje {
    private String nombreArchivo;

    public MensajePeticionDescarga(String nombreArchivo) {
        this.tipo = 4;
        this.nombreArchivo = nombreArchivo;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }
}