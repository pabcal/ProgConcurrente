package cliente;

import comun.Mensaje;
import comun.tiposmensajes.*;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.io.File;

import cliente.gui.VentanaPrincipal;


public class Cliente {
    private ObjectOutputStream out; //flujo para enviar mensajes al servidor
    private String nombreUsuario;
    private String ipLocal;
    private int puertoP2P;
    private File carpetaCompartida;
    private File carpetaDescargas;
    private final Semaphore semaforoDescargas = new Semaphore(3, true); // ✅ Semáforo justo, máx 3 descargas
    private Map<String, String> listaArchivos;
    private VentanaPrincipal ventanaPrincipal;
   
    
    //crea las carpetas si no existen e inicia el hilo OyenteP2P
    public Cliente(String nombreUsuario, String ipLocal, int puertoP2P) {
        this.nombreUsuario = nombreUsuario;
        this.ipLocal = ipLocal;
        this.puertoP2P = puertoP2P;

        this.carpetaCompartida = new File("compartido_" + nombreUsuario);
        if (!carpetaCompartida.exists()) 
        	carpetaCompartida.mkdir();

        this.carpetaDescargas = new File("descargas_" + nombreUsuario);
        if (!carpetaDescargas.exists()) 
        	carpetaDescargas.mkdir();

        new OyenteP2P(puertoP2P, carpetaCompartida).start();
    }
    
    //getter para el nombre de usuario
    public String getNombreUsuario() {
        return nombreUsuario;
    }
    
    //abre un Socket al puerto preestablecido, tambien inicializa out e in
    public void conectarAlServidor() {
        try {
            Socket socket = new Socket("localhost", 12345);
            out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            List<String> archivosCompartidos = obtenerArchivosCompartidos();
            MensajeInicioSesion mensaje = new MensajeInicioSesion(nombreUsuario, ipLocal, puertoP2P, archivosCompartidos);
            out.writeObject(mensaje);
            //iniciamos el hilo OyenteServidor para procesar las respuesta entrantes
            new OyenteServidor(in, this).start(); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    
    //las dos siguientes funciones envian al servidor los mensajes correspondientes
    public void solicitarListaArchivos() {
        try {
        	MensajePeticionLista mensajePeticion = new MensajePeticionLista();
            out.writeObject(mensajePeticion);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void solicitarDescarga(String nombreArchivo) {
        try {
        	MensajePeticionDescarga mensajeDescarga = new MensajePeticionDescarga(nombreArchivo);
            out.writeObject(mensajeDescarga);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //envia un mensaje fin de sesion para que el servidor de de baja al usuario
    public void cerrarSesion() {
        try {
        	MensajeFinSesion mensajeFin = new MensajeFinSesion(nombreUsuario);
            out.writeObject(mensajeFin);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void procesarMensaje(Mensaje m) {
        switch (m.getTipo()) {
            case 3: // Lista de archivos
                MensajeListaDisponible ml = (MensajeListaDisponible) m;
                this.listaArchivos = ml.getArchivosUsuarios();
                if (ventanaPrincipal != null) {
                    ventanaPrincipal.actualizarLista(listaArchivos);
                }
                break;
            case 5: // Info descarga
                MensajeInfoDescarga mid = (MensajeInfoDescarga) m;
                ClienteP2P clienteP2P = new ClienteP2P(
                        mid.getDireccionIP(),
                        mid.getPuertoP2P(),
                        getArchivoSolicitado(),
                        carpetaDescargas,
                        this
                );
                clienteP2P.start();
                break;
            case 99: // Error
                MensajeError me = (MensajeError) m;
                JOptionPane.showMessageDialog(null, "Error: " + me.getMensaje());
                break;
            default:
                System.out.println("Mensaje desconocido recibido.");
        }
    }

    private List<String> obtenerArchivosCompartidos() {
        List<String> archivos = new ArrayList<>();
        if (carpetaCompartida != null && carpetaCompartida.exists()) {
            File[] lista = carpetaCompartida.listFiles();
            if (lista != null) {
                for (File archivo : lista) {
                    if (archivo.isFile()) {
                        archivos.add(archivo.getName());
                    }
                }
            }
        }
        return archivos;
    }

    private String archivoSolicitado;

    public void setArchivoSolicitado(String archivo) {
        this.archivoSolicitado = archivo;
    }

    public String getArchivoSolicitado() {
        return archivoSolicitado;
    }

    public File getCarpetaCompartida() {
        return carpetaCompartida;
    }

    public File getCarpetaDescargas() {
        return carpetaDescargas;
    }

    public void setVentanaPrincipal(VentanaPrincipal ventanaPrincipal) {
        this.ventanaPrincipal = ventanaPrincipal;
    }

    public VentanaPrincipal getVentanaPrincipal() {
        return ventanaPrincipal;
    }

    // ✅ Getter para que ClienteP2P acceda al semáforo
    public Semaphore getSemaforoDescargas() {
        return semaforoDescargas;
    }
}
