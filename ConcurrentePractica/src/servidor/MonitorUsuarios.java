package servidor;

import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;

public class MonitorUsuarios {
	//este mapa representa todos los clientes conectados o registrados
    private final Map<String, Usuario> usuarios;
    private final Semaphore semaforo; //semaforo que utilizamos como lock FIFO
    private static final String ARCHIVO_USUARIOS = "usuarios.dat";
    
    /*Necesitamos un tipo de lock aqui porque multiples hilos de OyenteCliente van a acceder
     * al mismo mapa compartido al mismo tiempo. Si tenemos que dos hilos llaman a la vez a 
     * registrarUsuario y eliminarUsuario por ejemplo, matariamos la concurrencia
     * Hemos elegido un semaphore con parametro true, para garantizar una exclusion mutua
     * que vaya en orden*/
    
    public MonitorUsuarios() {
        usuarios = new HashMap<>();
        semaforo = new Semaphore(1, true);
        cargarUsuariosDesdeDisco();
    }
    
    //añade o actualiza un usuario
    public void registrarUsuario(Usuario usuario) {
        try {
            semaforo.acquire();
            usuarios.put(usuario.getNombre(), usuario);
            guardarUsuariosEnDisco();
            System.out.println("Registrado usuario: " + usuario.getNombre());
            System.out.println("Archivos compartidos: " + usuario.getArchivosCompartidos());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            semaforo.release();
        }
    }
    
    //quita un usuario del mapa
    public void eliminarUsuario(String nombre) {
        try {
            semaforo.acquire();
            usuarios.remove(nombre);
            guardarUsuariosEnDisco();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            semaforo.release();
        }
    }

    //funcion que utilizamos para recorrer todos los usuarios y construir el hashmap
    public Map<String, String> obtenerArchivosDisponibles() {
        try {
            semaforo.acquire();
            Map<String, String> resultado = new HashMap<>();
            for (Usuario u : usuarios.values()) {
                for (String archivo : u.getArchivosCompartidos()) {
                    resultado.put(archivo, u.getNombre());
                }
            }
            System.out.println("Archivos enviados a cliente: " + resultado);
            return resultado;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return Collections.emptyMap();
        } finally {
            semaforo.release();
        }
    }
    
    //devuelve el usuario que comparte el archivo dado
    public Usuario getUsuarioPorArchivo(String archivo) {
        try {
            semaforo.acquire();
            Usuario userToReturn = null;
            for (Usuario u : usuarios.values()) {
                if (u.getArchivosCompartidos().contains(archivo)) {
                	userToReturn = u;
                    break;
                }
            }
            return userToReturn;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            semaforo.release();
        }
    }

    private void guardarUsuariosEnDisco() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_USUARIOS))) {
            oos.writeObject(usuarios);
        } catch (IOException e) {
            System.err.println("Error guardando usuarios: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void cargarUsuariosDesdeDisco() {
        File archivo = new File(ARCHIVO_USUARIOS);
        if (archivo.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
                Map<String, Usuario> cargados = (Map<String, Usuario>) ois.readObject();
                usuarios.putAll(cargados);
                System.out.println("📂 Usuarios cargados desde disco: " + usuarios.keySet());
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error cargando usuarios: " + e.getMessage());
            }
        }
    }
}
