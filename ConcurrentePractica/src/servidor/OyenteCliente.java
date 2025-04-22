package servidor;

import comun.Mensaje;
import comun.tiposmensajes.*;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class OyenteCliente extends Thread {
    private final Socket socket;
    private final MonitorUsuarios monitor;

    public OyenteCliente(Socket socket, MonitorUsuarios monitor) {
        this.socket = socket;
        this.monitor = monitor;
    }

    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {

            boolean conectado = true;
            String usuarioActual = null;

            while (conectado) {
                Mensaje m = (Mensaje) in.readObject();
                //case 3 y 5 son respuestas (mensajes que envia el servidor)
                //estos cases son mensajes al servidor
                
                switch (m.getTipo()) {
                    case 1: // Inicio de sesión
                        MensajeInicioSesion mi = (MensajeInicioSesion) m;
                        usuarioActual = mi.getNombreUsuario();
                        Usuario usuario = new Usuario(
                        	    mi.getNombreUsuario(),
                        	    mi.getDireccionIP(),
                        	    mi.getPuertoP2P(),
                        	    mi.getArchivosCompartidos()  //aqui usamos los archivos reales del cliente
                        	);
                        monitor.registrarUsuario(usuario);
                        break;

                    case 2: // Petición de lista
                        var disponibles = monitor.obtenerArchivosDisponibles();
                        out.writeObject(new MensajeListaDisponible(disponibles));
                        break;

                    case 4: // Petición de descarga
                        MensajePeticionDescarga mpd = (MensajePeticionDescarga) m;
                        Usuario propietario = monitor.getUsuarioPorArchivo(mpd.getNombreArchivo());
                        if (propietario != null) {
                            out.writeObject(new MensajeInfoDescarga(propietario.getDireccionIP(), propietario.getPuertoP2P()));
                        } else {
                            out.writeObject(new MensajeError("Archivo no disponible."));
                        }
                        break;

                    case 6: // Fin de sesión
                        MensajeFinSesion mf = (MensajeFinSesion) m;
                        monitor.eliminarUsuario(mf.getNombreUsuario());
                        conectado = false;
                        break;

                    default:
                        out.writeObject(new MensajeError("Tipo de mensaje no reconocido."));
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}