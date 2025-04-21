package cliente;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class OyenteP2P extends Thread {
	private final int puerto;
	private final File carpetaCompartida;

	public OyenteP2P(int puerto, File carpetaCompartida) {
		this.puerto = puerto;
		this.carpetaCompartida = carpetaCompartida;
	}

	public void run() {
		try (ServerSocket servidor = new ServerSocket(puerto)) {
			System.out.println("Oyente P2P esperando en puerto " + puerto);

			while (true) {
				Socket cliente = servidor.accept();
				new Thread(() -> manejarCliente(cliente)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void manejarCliente(Socket cliente) {
		try (DataInputStream in = new DataInputStream(cliente.getInputStream());
				DataOutputStream out = new DataOutputStream(cliente.getOutputStream())) {

			String nombreArchivo = in.readUTF();
			File archivo = new File(carpetaCompartida, nombreArchivo);

			if (!archivo.exists()) {
				out.writeUTF("ERROR");
				return;
			}

			out.writeUTF("OK");
			try (FileInputStream fis = new FileInputStream(archivo)) {
				byte[] buffer = new byte[4096];
				int bytes;
				while ((bytes = fis.read(buffer)) != -1) {
					out.write(buffer, 0, bytes);
					out.flush(); // asegurar envío
					Thread.sleep(1000); // ✅ simula transferencia lenta para ver concurrencia
				}
			}

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
