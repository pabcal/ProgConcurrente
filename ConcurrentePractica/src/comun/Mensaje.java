package comun;

import java.io.Serializable;

public abstract class Mensaje implements Serializable {
    protected int tipo;

    public int getTipo() {
        return tipo;
    }
}