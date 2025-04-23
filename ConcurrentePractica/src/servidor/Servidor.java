package servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

//REMINDeR:
/*
 * Un socket es un punto de conexcion entre dos aplicaciones que se comunican a traves de una red
 * Hay dos roles:
 * 		ServerSocket -> es el que escucha en un puerto(12345), abre el puerto y queda a la espera
 * 		Socket -> es la conexion concurrente con un cliente
 * */


public class Servidor {
    public static void main(String[] args) {
        final int PUERTO = 12345; //establecemos este puerto por establecer uno
        MonitorUsuarios monitor = new MonitorUsuarios();

        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor intentando entrar en el puerto " + PUERTO);

            while (true) {
            	//el ServerSocket extrae de su cola la siguiente petición pendiente, 
            	//y devuelve el objeto Socket de esa conexion
                Socket cliente = servidor.accept(); 
                Thread t = new OyenteCliente(cliente, monitor);
                t.start();//aqui es donde arrancamos concurrencia
                			
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}