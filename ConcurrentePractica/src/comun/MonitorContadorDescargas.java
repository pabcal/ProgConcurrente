package comun;

public class MonitorContadorDescargas {
    private int contador = 0;

    public synchronized void incrementar() {
        contador++;
    }

    public synchronized void decrementar() {
        contador--;
    }

    public synchronized int getValor() {
        return contador;
    }
}
