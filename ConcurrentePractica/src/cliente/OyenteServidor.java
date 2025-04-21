package cliente;

import comun.Mensaje;
import java.io.ObjectInputStream;

public class OyenteServidor extends Thread {
    private final ObjectInputStream in;
    private final Cliente cliente;

    public OyenteServidor(ObjectInputStream in, Cliente cliente) {
        this.in = in;
        this.cliente = cliente;
    }

    public void run() {
        try {
            while (true) {
                Mensaje m = (Mensaje) in.readObject();
                cliente.procesarMensaje(m);
            }
        } catch (Exception e) {
            System.out.println("Conexión con el servidor terminada.");
        }
    }
}