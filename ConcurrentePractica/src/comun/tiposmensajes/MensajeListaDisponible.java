package comun.tiposmensajes;

import comun.Mensaje;
import java.util.Map;

public class MensajeListaDisponible extends Mensaje {
    private Map<String, String> archivosUsuarios;

    public MensajeListaDisponible(Map<String, String> archivosUsuarios) {
        this.tipo = 3;
        this.archivosUsuarios = archivosUsuarios;
    }

    public Map<String, String> getArchivosUsuarios() {
        return archivosUsuarios;
    }
}