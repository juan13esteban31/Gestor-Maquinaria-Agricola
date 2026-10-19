
import java.awt.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class Main extends JFrame {

    String[] vector = new String[100];
    String[][] matriz = new String[10][10];

    LinkedList<String> listaSimple = new LinkedList<>();
    LinkedList<String> listaDoble = new LinkedList<>();
    LinkedList<String> listaCircular = new LinkedList<>();

    Queue<Tarea> alta = new LinkedList<>();
    Queue<Tarea> media = new LinkedList<>();
    Queue<Tarea> baja = new LinkedList<>();
    Stack<Tarea> pausadas = new Stack<>();

    ArrayList<String> maquinas = new ArrayList<>();
    ArrayList<Tarea> tareas = new ArrayList<>();

    DefaultTableModel modelo = new DefaultTableModel(
        new String[]{"ID", "Tarea", "Maquina", "Prioridad", "Estado"}, 0
    );

    JTable tabla = new JTable(modelo);
    int siguienteId = 1;
    int quantum = 2;
    int turno = 0;

    public Main() {
        setTitle("Gestor de Maquinaria Agricola");
        setSize(750, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JLabel titulo = new JLabel(
            "GESTOR DE MAQUINARIA AGRICOLA",
            SwingConstants.CENTER
        );
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        add(titulo, BorderLayout.NORTH);

        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel botones = new JPanel(new GridLayout(2, 3, 5, 5));

        JButton b1 = new JButton("Agregar maquina");
        JButton b2 = new JButton("Agregar tarea");
        JButton b3 = new JButton("Ejecutar turno");
        JButton b4 = new JButton("Pausar tarea");
        JButton b5 = new JButton("Recuperar tarea");
        JButton b6 = new JButton("Ver estado");

        botones.add(b1);
        botones.add(b2);
        botones.add(b3);
        botones.add(b4);
        botones.add(b5);
        botones.add(b6);

        add(botones, BorderLayout.SOUTH);

        b1.addActionListener(e -> agregarMaquina());
        b2.addActionListener(e -> agregarTarea());
        b3.addActionListener(e -> ejecutarTurno());
        b4.addActionListener(e -> pausarTarea());
        b5.addActionListener(e -> recuperarTarea());
        b6.addActionListener(e -> verEstado());

        setVisible(true);
    }

    void agregarMaquina() {
        String nombre = JOptionPane.showInputDialog(
            this, "Nombre de la maquina:"
        );

        if (nombre != null && !nombre.trim().isEmpty()) {
            nombre = nombre.trim();
            maquinas.add(nombre);
            listaCircular.add(nombre);

            JOptionPane.showMessageDialog(
                this, "Maquina agregada."
            );
        }
    }
    void agregarTarea() {
        if (maquinas.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "Primero agrega una maquina."
            );
            return;
        }

        if (tareas.size() >= vector.length) {
            JOptionPane.showMessageDialog(
                this, "Ya se alcanzo el limite de tareas."
            );
            return;
        }

        String nombre = JOptionPane.showInputDialog(
            this, "Nombre de la tarea:"
        );

        if (nombre == null || nombre.trim().isEmpty()) {
            return;
        }

        String[] prioridades = {"Alta", "Media", "Baja"};

        String prioridad = (String) JOptionPane.showInputDialog(
            this, "Selecciona la prioridad:",
            "Prioridad", JOptionPane.QUESTION_MESSAGE,
            null, prioridades, prioridades[1]
        );

        if (prioridad == null) {
            return;
        }

        String maquina = (String) JOptionPane.showInputDialog(
            this, "Selecciona la maquina:",
            "Maquina", JOptionPane.QUESTION_MESSAGE,
            null, maquinas.toArray(), maquinas.get(0)
        );

        if (maquina == null) {
            return;
        }

        Tarea t = new Tarea(
            siguienteId++, nombre.trim(), maquina, prioridad
        );

        tareas.add(t);
        vector[tareas.size() - 1] = t.nombre;

        int posicion = tareas.size() - 1;
        matriz[posicion / 10][posicion % 10] = t.nombre;

        listaSimple.add(t.nombre);
        listaDoble.add(t.nombre);

        if (prioridad.equals("Alta")) {
            alta.offer(t);
        } else if (prioridad.equals("Media")) {
            media.offer(t);
        } else {
            baja.offer(t);
        }

        actualizarTabla();
    }

    void ejecutarTurno() {
        Tarea t = null;

        if (!alta.isEmpty()) {
            t = alta.poll();
        } else if (!media.isEmpty()) {
            t = media.poll();
        } else if (!baja.isEmpty()) {
            t = baja.poll();
        }

        if (t == null) {
            JOptionPane.showMessageDialog(
                this, "No hay tareas pendientes."
            );
            return;
        }

        turno++;
        t.restante -= quantum;

        if (t.restante <= 0) {
            t.estado = "Completada";
            JOptionPane.showMessageDialog(
                this, "Tarea completada: " + t.nombre
            );
        } else {
            t.estado = "Pendiente";

            if (t.prioridad.equals("Alta")) {
                alta.offer(t);
            } else if (t.prioridad.equals("Media")) {
                media.offer(t);
            } else {
                baja.offer(t);
            }

            JOptionPane.showMessageDialog(
                this, "Turno terminado. La tarea sigue pendiente."
            );
        }

        actualizarTabla();
    }

    void pausarTarea() {
        int fila = tabla.getSelectedRow();

        if (fila == -1) {
            JOptionPane.showMessageDialog(
                this, "Selecciona una tarea de la tabla."
            );
            return;
        }

        int id = (int) modelo.getValueAt(fila, 0);
        Tarea t = buscarTarea(id);

        if (t == null || !t.estado.equals("Pendiente")) {
            JOptionPane.showMessageDialog(
                this, "Selecciona una tarea pendiente."
            );
            return;
        }

        boolean encontrada = alta.remove(t);
        if (!encontrada) {
            encontrada = media.remove(t);
        }
        if (!encontrada) {
            encontrada = baja.remove(t);
        }

        if (encontrada) {
            t.estado = "Pausada";
            pausadas.push(t);
            actualizarTabla();
        }
    }
    void recuperarTarea() {
        if (pausadas.isEmpty()) {
            JOptionPane.showMessageDialog(
                this, "No hay tareas pausadas."
            );
            return;
        }

        Tarea t = pausadas.pop();
        t.estado = "Pendiente";

        if (t.prioridad.equals("Alta")) {
            alta.offer(t);
        } else if (t.prioridad.equals("Media")) {
            media.offer(t);
        } else {
            baja.offer(t);
        }

        actualizarTabla();
    }

    Tarea buscarTarea(int id) {
        for (Tarea t : tareas) {
            if (t.id == id) {
                return t;
            }
        }
        return null;
    }


    void verEstado() {
        String mensaje =
            "Maquinas: " + maquinas.size()
            + "\nTareas creadas: " + tareas.size()
            + "\nCola alta: " + alta.size()
            + "\nCola media: " + media.size()
            + "\nCola baja: " + baja.size()
            + "\nTareas pausadas: " + pausadas.size()
            + "\nTurnos ejecutados: " + turno
            + "\nQuantum: " + quantum;

        JOptionPane.showMessageDialog(this, mensaje);
    }

    void actualizarTabla() {
        modelo.setRowCount(0);

        for (Tarea t : tareas) {
            modelo.addRow(new Object[]{
                t.id, t.nombre, t.maquina,
                t.prioridad, t.estado
            });
        }
    }

    
    class Tarea {
        int id;
        String nombre;
        String maquina;
        String prioridad;
        int restante = 6;
        String estado = "Pendiente";

        Tarea(int id, String nombre, String maquina, String prioridad) {
            this.id = id;
            this.nombre = nombre;
            this.maquina = maquina;
            this.prioridad = prioridad;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main());
    }
}
