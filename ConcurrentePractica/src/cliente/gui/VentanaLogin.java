package cliente.gui;

import cliente.Cliente;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;

public class VentanaLogin extends JFrame {
    private JTextField campoNombre;
    private JButton botonConectar;

    public VentanaLogin() {
        setTitle("Inicio de sesión");
        setSize(300, 150);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel etiqueta = new JLabel("Nombre de usuario:");
        etiqueta.setBounds(20, 20, 150, 25);
        add(etiqueta);

        campoNombre = new JTextField();
        campoNombre.setBounds(20, 50, 200, 25);
        add(campoNombre);

        botonConectar = new JButton("Conectar");
        botonConectar.setBounds(20, 85, 100, 25);
        add(botonConectar);

        botonConectar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String nombre = campoNombre.getText().trim();
                if (!nombre.isEmpty()) {
                    try {
                        String ip = InetAddress.getLocalHost().getHostAddress();
                        int puertoP2P = 5000 + (int)(Math.random() * 1000); // Puerto aleatorio por cliente
                        Cliente cliente = new Cliente(nombre, ip, puertoP2P);
                        cliente.conectarAlServidor();

                        VentanaPrincipal vp = new VentanaPrincipal(cliente);
                        cliente.setVentanaPrincipal(vp);  // ✅ AÑADE ESTA LÍNEA
                        cliente.solicitarListaArchivos();
                        vp.setVisible(true);
                        dispose();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Error al iniciar cliente: " + ex.getMessage());
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Introduce un nombre de usuario.");
                }
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new VentanaLogin().setVisible(true);
        });
    }
}