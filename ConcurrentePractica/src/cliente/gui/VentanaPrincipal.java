package cliente.gui;

import cliente.Cliente;
import cliente.LockBakery;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.HashMap;

public class VentanaPrincipal extends JFrame {
    private final Cliente cliente;
    private final DefaultListModel<String> modeloLista;
    private final JList<String> listaArchivos;
    private final JButton botonActualizar;
    private final JButton botonDescargar;
    private final JButton botonSalir;

    private final JPanel panelDescargas;
    private final Map<String, JProgressBar> barrasDescarga;

    private final JTextArea areaLog;

    private final LockBakery lockBarras = new LockBakery(50); // Máximo 50 hilos
    private static int contadorID = 0;
    private final ThreadLocal<Integer> hiloID = ThreadLocal.withInitial(() -> contadorID++);

    public VentanaPrincipal(Cliente cliente) {
        this.cliente = cliente;

        setTitle("Cliente P2P - " + cliente.getNombreUsuario());
        setSize(400, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        JLabel etiqueta = new JLabel("Archivos disponibles:");
        etiqueta.setBounds(20, 10, 200, 20);
        add(etiqueta);

        modeloLista = new DefaultListModel<>();
        listaArchivos = new JList<>(modeloLista);
        JScrollPane scroll = new JScrollPane(listaArchivos);
        scroll.setBounds(20, 35, 340, 140);
        add(scroll);

        botonActualizar = new JButton("Actualizar");
        botonActualizar.setBounds(20, 185, 100, 25);
        add(botonActualizar);

        botonDescargar = new JButton("Descargar");
        botonDescargar.setBounds(130, 185, 100, 25);
        add(botonDescargar);

        botonSalir = new JButton("Salir");
        botonSalir.setBounds(240, 185, 100, 25);
        add(botonSalir);

        panelDescargas = new JPanel();
        panelDescargas.setLayout(new BoxLayout(panelDescargas, BoxLayout.Y_AXIS));
        panelDescargas.setBorder(BorderFactory.createTitledBorder("Descargas en curso"));
        panelDescargas.setBounds(20, 220, 340, 160);
        add(panelDescargas);

        barrasDescarga = new HashMap<>();

        areaLog = new JTextArea(8, 50);
        areaLog.setEditable(false);
        JScrollPane scrollLog = new JScrollPane(areaLog);
        JPanel panelLog = new JPanel(new BorderLayout());
        panelLog.setBorder(BorderFactory.createTitledBorder("Log de actividad"));
        panelLog.setBounds(20, 390, 340, 140);
        panelLog.add(scrollLog, BorderLayout.CENTER);
        add(panelLog);

        botonActualizar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cliente.solicitarListaArchivos();
            }
        });

        botonDescargar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String seleccionado = listaArchivos.getSelectedValue();
                if (seleccionado != null) {
                    String[] partes = seleccionado.split(" \\(" );
                    String archivo = partes[0];
                    cliente.setArchivoSolicitado(archivo);
                    cliente.solicitarDescarga(archivo);
                } else {
                    JOptionPane.showMessageDialog(null, "Selecciona un archivo para descargar.");
                }
            }
        });

        botonSalir.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cliente.cerrarSesion();
                System.exit(0);
            }
        });

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                cliente.cerrarSesion();
                System.exit(0);
            }
        });
    }

    public void actualizarLista(Map<String, String> archivos) {
        modeloLista.clear();
        for (String archivo : archivos.keySet()) {
            modeloLista.addElement(archivo + " (de " + archivos.get(archivo) + ")");
        }
    }

    public void registrarDescarga(String archivo) {
        int id = hiloID.get();
        lockBarras.takeLock(id);
        try {
            if (!barrasDescarga.containsKey(archivo)) {
                JProgressBar barra = new JProgressBar();
                barra.setStringPainted(true);
                barra.setString("Descargando: " + archivo);
                barra.setMinimum(0);
                barra.setMaximum(100);
                panelDescargas.add(barra);
                barrasDescarga.put(archivo, barra);
                panelDescargas.revalidate();
                panelDescargas.repaint();
            }
        } finally {
            lockBarras.releaseLock(id);
        }
    }

    public void actualizarProgreso(String archivo, int porcentaje) {
        int id = hiloID.get();
        lockBarras.takeLock(id);
        try {
            JProgressBar barra = barrasDescarga.get(archivo);
            if (barra != null) {
                barra.setValue(porcentaje);
            }
        } finally {
            lockBarras.releaseLock(id);
        }
    }

    public void marcarCompleta(String archivo) {
        int id = hiloID.get();
        lockBarras.takeLock(id);
        try {
            JProgressBar barra = barrasDescarga.get(archivo);
            if (barra != null) {
                barra.setValue(100);
                barra.setString("✅ Completada: " + archivo);
            }
        } finally {
            lockBarras.releaseLock(id);
        }
    }

    public void log(String mensaje) {
        areaLog.append("[" + new java.util.Date() + "] " + mensaje + "\n");
        areaLog.setCaretPosition(areaLog.getDocument().getLength());
    }
}