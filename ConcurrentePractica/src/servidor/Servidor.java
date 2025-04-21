package servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {
    public static void main(String[] args) {
        final int PUERTO = 12345;
        MonitorUsuarios monitor = new MonitorUsuarios();

        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor escuchando en el puerto " + PUERTO);

            while (true) {
                Socket cliente = servidor.accept();
                new OyenteCliente(cliente, monitor).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}