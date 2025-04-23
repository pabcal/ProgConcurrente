package cliente;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.Random;

public class ClienteP2P extends Thread {
    private final String ip;
    private final int puerto;
    private final String archivo;
    private final File destino;
    private final Cliente cliente;
    private final TicketLimiter limiter;
    private final int myTicket;

    public ClienteP2P(String ip, int puerto, String archivo, File destino, Cliente cliente, TicketLimiter limiter) {
        this.ip = ip;
        this.puerto = puerto;
        this.archivo = archivo;
        this.destino = destino;
        this.cliente = cliente;
        this.limiter = limiter;
        this.myTicket = limiter.takeTicket();
    }

    @Override
    public void run() {
        // 1) Espera su turno entre los K permitidos
        limiter.enter(myTicket);
        SwingUtilities.invokeLater(() ->
            cliente.getVentanaPrincipal().log("➡️ INICIO descarga de " + archivo + " (ticket " + myTicket + ")")
        );

        try (Socket socket = new Socket(ip, puerto);
             DataOutputStream out = new DataOutputStream(socket.getOutputStream());
             DataInputStream  in  = new DataInputStream(socket.getInputStream())) {

            // 2) Solicita el archivo
            out.writeUTF(archivo);
            if (!"OK".equals(in.readUTF())) {
                SwingUtilities.invokeLater(() ->
                    cliente.getVentanaPrincipal().log("❌ Archivo no disponible: " + archivo)
                );
                return;
            }

            // 3) Registra en la GUI y comienza a leer bytes
            String nombreCliente = destino.getName().replace("descargas_", "");
            String idDescarga     = archivo + " (" + nombreCliente + ")";

            SwingUtilities.invokeLater(() ->
                cliente.getVentanaPrincipal().registrarDescarga(idDescarga)
            );

            File destinoArchivo = new File(destino, archivo);
            try (FileOutputStream fos = new FileOutputStream(destinoArchivo)) {
                byte[] buffer = new byte[4096];
                int bytesLeidos;
                int totalLeidos = 0;

                while ((bytesLeidos = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesLeidos);
                    totalLeidos += bytesLeidos;

                    int porcentaje = Math.min(100, totalLeidos / 1000);
                    final int totalLeidoFinal = totalLeidos;
                    
                    SwingUtilities.invokeLater(() -> {
                        cliente.getVentanaPrincipal().actualizarProgreso(idDescarga, porcentaje);
                        cliente.getVentanaPrincipal().log( "Fragmento de " + archivo + " (" + totalLeidoFinal + " bytes totales)");
                    });

                    // Simulación de lentitud
                    Thread.sleep(1500 + new Random().nextInt(4000));
                }

                SwingUtilities.invokeLater(() -> {
                    cliente.getVentanaPrincipal().marcarCompleta(idDescarga);
                    cliente.getVentanaPrincipal().log("Descarga completada: " + idDescarga);
                });
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 4) Libera su plaza y despierta a los siguientes tickets
            limiter.leave();
            SwingUtilities.invokeLater(() ->
                cliente.getVentanaPrincipal().log("⬅️ FIN descarga de " + archivo + " (ticket " + myTicket + ")")
            );
        }
    }
}
