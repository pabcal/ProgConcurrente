package cliente;

import comun.MonitorContadorDescargas;
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

    private static final MonitorContadorDescargas monitorContador = new MonitorContadorDescargas();

    public ClienteP2P(String ip, int puerto, String archivo, File destino, Cliente cliente) {
        this.ip = ip;
        this.puerto = puerto;
        this.archivo = archivo;
        this.destino = destino;
        this.cliente = cliente;
    }

    public void run() {
        try {
            cliente.getSemaforoDescargas().acquire();
            monitorContador.incrementar();
            int activas = monitorContador.getValor();
            SwingUtilities.invokeLater(() ->
                cliente.getVentanaPrincipal().log("➡️ INICIO descarga de " + archivo + " | Descargas activas: " + activas)
            );

            try (Socket socket = new Socket(ip, puerto);
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                 DataInputStream in = new DataInputStream(socket.getInputStream())) {

                out.writeUTF(archivo);
                String respuesta = in.readUTF();

                if ("OK".equals(respuesta)) {
                    String nombreCliente = destino.getName().replace("descargas_", "");
                    String idDescarga = archivo + " (" + nombreCliente + ")";

                    SwingUtilities.invokeLater(() ->
                        cliente.getVentanaPrincipal().registrarDescarga(idDescarga)
                    );

                    File destinoArchivo = new File(destino, archivo);
                    try (FileOutputStream fos = new FileOutputStream(destinoArchivo)) {
                        byte[] buffer = new byte[4096];
                        int bytes;
                        int totalLeidos = 0;

                        while ((bytes = in.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytes);
                            totalLeidos += bytes;

                            int porcentaje = Math.min(100, totalLeidos / 1000);
                            int finalTotalLeidos = totalLeidos;
                            SwingUtilities.invokeLater(() -> {
                                cliente.getVentanaPrincipal().actualizarProgreso(idDescarga, porcentaje);
                                cliente.getVentanaPrincipal().log("⏳ Fragmento de " + archivo + " (" + finalTotalLeidos + " bytes totales)");
                            });

                            Thread.sleep(1500 + new Random().nextInt(4000));
                        }

                        SwingUtilities.invokeLater(() -> {
                            cliente.getVentanaPrincipal().marcarCompleta(idDescarga);
                            cliente.getVentanaPrincipal().log("✅ Descarga completada: " + idDescarga);
                        });
                    }
                } else {
                    SwingUtilities.invokeLater(() ->
                        cliente.getVentanaPrincipal().log("❌ Archivo no disponible: " + archivo)
                    );
                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                monitorContador.decrementar();
                int activasFinal = monitorContador.getValor();
                SwingUtilities.invokeLater(() ->
                    cliente.getVentanaPrincipal().log("⬅️ FIN descarga de " + archivo + " | Descargas activas: " + activasFinal)
                );

                cliente.getSemaforoDescargas().release();
            }

        } catch (InterruptedException e) {
            SwingUtilities.invokeLater(() ->
                cliente.getVentanaPrincipal().log("⛔ Hilo de descarga interrumpido mientras esperaba turno para " + archivo)
            );
        }
    }
}
