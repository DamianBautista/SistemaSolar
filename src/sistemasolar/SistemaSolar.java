package sistemasolar;
/**
 *
 * @author Damian Bautista
 */

import com.sun.j3d.utils.behaviors.keyboard.KeyNavigatorBehavior;
import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.geometry.Primitive;
import com.sun.j3d.utils.geometry.Sphere;
import com.sun.j3d.utils.image.TextureLoader;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.j3d.utils.universe.ViewingPlatform;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.media.j3d.Alpha;
import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.LineArray;
import javax.media.j3d.Material;
import javax.media.j3d.RotationInterpolator;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;


/**
 * Clase SistemaSolar que representa la interfaz gráfica del sistema solar.
 * Permite visualizar los planetas y realizar diferentes interacciones.
 */
class SistemaSolar extends Frame {

    //Atributos
    private SimpleUniverse universe;
    private TransformGroup sistemaSolarTransform;
    private int startX, startY;
    private double displacementFactor = 0.00005;
    private boolean isDragging = false;

    // Arreglo que almacena las velocidades orbitales relativas de los planetas
    private double[] orbitalSpeeds = {
        0, // Sol (estático)
        2.0, // Mercurio
        1.0, // Venus
        0.5, // Tierra
        0.2, // Marte
        1.5, // Júpiter
        0.8, // Saturno
        0.2, // Urano
        0.1 // Neptuno
    };

    /**
     * Constructor de la clase SistemaSolar. Configura la interfaz gráfica y
     * crea el sistema solar.
     */
    public SistemaSolar() {
        // Configuración inicial de la ventana
        super("SISTEMA SOLAR");
        setSize(1920, 1000);
        setLayout(new BorderLayout());

        // Crear un panel para los botones en el lado izquierdo de la ventana
        JPanel buttonPanel = createButtonPanel();
        add(buttonPanel, BorderLayout.WEST);

        // Obtener la configuración gráfica preferida para el lienzo 3D
        GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();

        // Crear el lienzo 3D
        Canvas3D canvas3D = new Canvas3D(config);
        add("Center", canvas3D);
        canvas3D.setFocusable(true);
        canvas3D.requestFocus();

        // Configurar el universo y la plataforma de visualización
        universe = new SimpleUniverse(canvas3D);
        universe.getViewingPlatform().setNominalViewingTransform();

        // Crear el grafo de escena que contiene los elementos del sistema solar
        BranchGroup escena = crearGrafoEscena();
        escena.compile(); // Compilar el grafo de escena para mejorar el rendimiento
        universe.addBranchGraph(escena);// Agregar el grafo de escena al universo

        ViewingPlatform viewingPlatform = universe.getViewingPlatform();
        TransformGroup vpTransformGroup = viewingPlatform.getViewPlatformTransform();

        // Agregar un MouseAdapter para detectar el inicio y fin del arrastre del mouse
        canvas3D.addMouseListener(new java.awt.event.MouseAdapter() {
            /**
             * Método que se ejecuta cuando el mouse es presionado. Se guarda la
             * posición inicial del mouse y se activa el indicador de arrastre
             * (isDragging).
             *
             * @param evt El evento del mouse.
             */
            public void mousePressed(java.awt.event.MouseEvent evt) {
                startX = evt.getX();
                startY = evt.getY();
                isDragging = true;
            }

            /**
             * Método que se ejecuta cuando el mouse es soltado. Se desactiva el
             * indicador de arrastre (isDragging).
             *
             * @param evt El evento del mouse.
             */
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                isDragging = false;
            }
        });

        // Agregar un MouseMotionAdapter para realizar la rotación de la vista mientras se arrastra el mouse
        canvas3D.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            /**
             * Método que se ejecuta cuando el mouse es arrastrado. Si
             * isDragging es verdadero, se calcula el desplazamiento (deltaX,
             * deltaY) y se ajusta la transformación de visualización.
             *
             * @param evt El evento del mouse.
             */
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                if (isDragging) {
                    int mouseX = evt.getX();
                    int mouseY = evt.getY();
                    int deltaX = mouseX - startX;
                    int deltaY = mouseY - startY;

                    Transform3D transform = new Transform3D();
                    vpTransformGroup.getTransform(transform);
                    Vector3f translation = new Vector3f();
                    transform.get(translation);
                    translation.x -= deltaX * displacementFactor;
                    translation.y += deltaY * displacementFactor;
                    transform.setTranslation(translation);
                    vpTransformGroup.setTransform(transform);
                }
            }
        });

        // Agregar un MouseWheelListener para realizar el zoom mientras se mueve la rueda del ratón
        canvas3D.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            /**
             * Método que se ejecuta cuando se mueve la rueda del ratón. Si
             * isDragging es falso, se ajusta la transformación de visualización
             * para realizar un zoom.
             *
             * @param evt El evento de la rueda del ratón.
             */
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                if (isDragging) {
                    return;
                }

                int rotation = evt.getWheelRotation();
                Transform3D transform = new Transform3D();
                vpTransformGroup.getTransform(transform);
                Vector3f translation = new Vector3f();
                transform.get(translation);
                double displacement = rotation * displacementFactor;
                translation.z -= displacement;
                transform.setTranslation(translation);
                vpTransformGroup.setTransform(transform);
            }
        });

        // Agregar un WindowListener para manejar el cierre de la ventana
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent event) {
                System.exit(0);
            }
        });
    }

    /**
     * Método para crear el panel de botones que muestra información sobre los planetas.
     * @return JPanel que contiene los botones con información de los planetas.
     */
    private JPanel createButtonPanel() {
        // Crear el panel que contendrá los botones
        JPanel panel = new JPanel();
        panel.setBackground(Color.BLACK);
        panel.setLayout(new GridLayout(9, 1));
        
        // BOTÓN SOL
        JButton solButton = new JButton("SOL");
        solButton.setBackground(Color.BLACK);
        solButton.setForeground(Color.WHITE);
        solButton.setBorder(new LineBorder(Color.BLACK));
        
        // Agregar un MouseAdapter para detectar cuando el cursor entra en el botón
        solButton.addMouseListener(new MouseAdapter() {
            /**
             * Método que se ejecuta cuando el cursor entra en el botón del Sol.
             * Muestra un cuadro de diálogo con información sobre el Sol.
             * @param evt El evento del mouse.
             */
            public void mouseEntered(MouseEvent evt) {
                Component parentComponent = (Component) evt.getSource();
                
                // Crear la lista de información sobre el Sol
                String[] info = {"<html><b>TITULO:</b> SOL.</html>",
                    "<html><b>Masa:</b> Aproximadamente 333,000 veces la masa de la Tierra.</html>",
                    "<html><b>Diámetro:</b> Aproximadamente 1,391,000 kilómetros.</html>",
                    "<html><b>Período orbital:</b> No aplica, ya que no orbita alrededor </html>",
                    "<html>de ningún objeto.</html>",
                    "<html><b>Características:</b> El Sol es una estrella en el centro </html>",
                    "<html> sistema solar. Es una bola de gas caliente compuesta </html>",
                    "<html> principalmente de hidrógeno y helio. Genera energía a través</html>",
                    "<html>de reacciones nucleares en su núcleo y emite luz y calor al espacio. </html>"
                };

                // Personalizar los colores del JOptionPane
                UIManager.put("OptionPane.background", Color.BLACK);
                UIManager.put("OptionPane.messageForeground", Color.BLACK);
                UIManager.put("OptionPane.messageFont", new Font("Arial", Font.BOLD, 16));
                UIManager.put("List.foreground", Color.BLACK);
                UIManager.put("List.font", new Font("Arial", Font.PLAIN, 14));

                // Crear el mensaje con la lista justificada
                JList<String> infoList = new JList<>(info);
                infoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                infoList.setLayoutOrientation(JList.VERTICAL);
                infoList.setVisibleRowCount(-1);
                infoList.setAlignmentX(Component.LEFT_ALIGNMENT);
                infoList.setBackground(Color.BLACK);
                infoList.setForeground(Color.WHITE);
                infoList.setSelectionBackground(Color.BLACK);
                infoList.setSelectionForeground(Color.BLACK);
                
                // Resaltar las primeras palabras en negrita en la lista
                infoList.setCellRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        label.setHorizontalAlignment(SwingConstants.LEFT);
                        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                        // Resaltar las primeras palabras en negrita
                        String[] parts = value.toString().split(":", 2);
                        if (parts.length == 2) {
                            label.setText("<html><b>" + parts[0] + ":</b> " + parts[1] + "</html>");
                        }

                        return label;
                    }
                });

                JScrollPane scrollPane = new JScrollPane(infoList);
                scrollPane.setPreferredSize(new Dimension(400, 300));
                scrollPane.setBorder(BorderFactory.createEmptyBorder());

                // Mostrar el cuadro de diálogo con la información del Sol
                JOptionPane optionPane = new JOptionPane(scrollPane, JOptionPane.INFORMATION_MESSAGE);
                JDialog dialog = optionPane.createDialog(parentComponent, "INFORMACIÓN DEL SOL");
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            }
        });
        
        // Agregar el botón del Sol al panel
        panel.add(solButton);

        //BOTÓN MERCURIO
        JButton mercurioButton = new JButton("MERCURIO");
        mercurioButton.setBackground(Color.BLACK);
        mercurioButton.setForeground(Color.WHITE);
        mercurioButton.setBorder(new LineBorder(Color.BLACK));
        
        // Agregar un MouseAdapter para detectar cuando el cursor entra en el botón de Mercurio
        mercurioButton.addMouseListener(new MouseAdapter() {
            /**
             * Método que se ejecuta cuando el cursor entra en el botón de Mercurio.
             * Muestra un cuadro de diálogo con información sobre Mercurio.
             * @param evt El evento del mouse.
             */
            public void mouseEntered(MouseEvent evt) {
                Component parentComponent = (Component) evt.getSource();
                
                // Crear la lista de información sobre Mercurio
                String[] info = {"<html><b>TITULO:</b> MERCURIO.</html>",
                    "<html><b>Masa:</b>  0.055 veces la masa de la Tierra.</html>",
                    "<html><b>Diámetro:</b> Aproximadamente 4,879 kilómetros.</html>",
                    "<html><b>Período orbital:</b> 88 días terrestres. </html>",
                    "<html><b>Características:</b> Es el planeta más cercano al Sol  </html>",
                    "<html>  también el más pequeño del sistema solar.  </html>"
                };

                // Personalizar los colores del JOptionPane
                UIManager.put("OptionPane.background", Color.BLACK);
                UIManager.put("OptionPane.messageForeground", Color.BLACK);
                UIManager.put("OptionPane.messageFont", new Font("Arial", Font.BOLD, 16));
                UIManager.put("List.foreground", Color.BLACK);
                UIManager.put("List.font", new Font("Arial", Font.PLAIN, 14));

                // Crear el mensaje con la lista justificada
                JList<String> infoList = new JList<>(info);
                infoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                infoList.setLayoutOrientation(JList.VERTICAL);
                infoList.setVisibleRowCount(-1);
                infoList.setAlignmentX(Component.LEFT_ALIGNMENT);
                infoList.setBackground(Color.BLACK);
                infoList.setForeground(Color.WHITE);
                infoList.setSelectionBackground(Color.BLACK);
                infoList.setSelectionForeground(Color.BLACK);
                infoList.setCellRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        label.setHorizontalAlignment(SwingConstants.LEFT);
                        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                        // Resaltar las primeras palabras en negrita
                        String[] parts = value.toString().split(":", 2);
                        if (parts.length == 2) {
                            label.setText("<html><b>" + parts[0] + ":</b> " + parts[1] + "</html>");
                        }

                        return label;
                    }
                });

                JScrollPane scrollPane = new JScrollPane(infoList);
                scrollPane.setPreferredSize(new Dimension(400, 300));
                scrollPane.setBorder(BorderFactory.createEmptyBorder());

                JOptionPane optionPane = new JOptionPane(scrollPane, JOptionPane.INFORMATION_MESSAGE);
                JDialog dialog = optionPane.createDialog(parentComponent, "INFORMACIÓN DE MERCUERIO");
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            }
        });
        // Agregar el botón de Mercurio al panel
        panel.add(mercurioButton);

        //BOTON VENUS
        JButton venusButton = new JButton("VENUS");
        venusButton.setBackground(Color.BLACK);
        venusButton.setForeground(Color.WHITE);
        venusButton.setBorder(new LineBorder(Color.BLACK));
        venusButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                Component parentComponent = (Component) evt.getSource();
                // Crear la lista de información
                String[] info = {"<html><b>TITULO:</b> VENUS.</html>",
                    "<html><b>Masa:</b> 0.815 veces la masa de la Tierra.</html>",
                    "<html><b>Diámetro:</b> Aproximadamente 12,104 kilómetros.</html>",
                    "<html><b>Período orbital:</b>  225 días terrestres. </html>",
                    "<html><b>Características:</b> es similar a la Tierra en tamaño y composición </html>",
                    "<html> su atmósfera es densa y compuesta principalmente de dióxido de carbono </html>"
                };

                // Personalizar los colores del JOptionPane
                UIManager.put("OptionPane.background", Color.BLACK);
                UIManager.put("OptionPane.messageForeground", Color.BLACK);
                UIManager.put("OptionPane.messageFont", new Font("Arial", Font.BOLD, 16));
                UIManager.put("List.foreground", Color.BLACK);
                UIManager.put("List.font", new Font("Arial", Font.PLAIN, 14));

                // Crear el mensaje con la lista justificada
                JList<String> infoList = new JList<>(info);
                infoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                infoList.setLayoutOrientation(JList.VERTICAL);
                infoList.setVisibleRowCount(-1);
                infoList.setAlignmentX(Component.LEFT_ALIGNMENT);
                infoList.setBackground(Color.BLACK);
                infoList.setForeground(Color.WHITE);
                infoList.setSelectionBackground(Color.BLACK);
                infoList.setSelectionForeground(Color.BLACK);
                infoList.setCellRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        label.setHorizontalAlignment(SwingConstants.LEFT);
                        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                        // Resaltar las primeras palabras en negrita
                        String[] parts = value.toString().split(":", 2);
                        if (parts.length == 2) {
                            label.setText("<html><b>" + parts[0] + ":</b> " + parts[1] + "</html>");
                        }

                        return label;
                    }
                });

                JScrollPane scrollPane = new JScrollPane(infoList);
                scrollPane.setPreferredSize(new Dimension(400, 300));
                scrollPane.setBorder(BorderFactory.createEmptyBorder());

                JOptionPane optionPane = new JOptionPane(scrollPane, JOptionPane.INFORMATION_MESSAGE);
                JDialog dialog = optionPane.createDialog(parentComponent, "INFORMACIÓN DEL PLANETA VENUS");
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            }
        });
        panel.add(venusButton);

        //BOTON TIERRA
        JButton tierraButton = new JButton("TIERRA");
        tierraButton.setBackground(Color.BLACK);
        tierraButton.setForeground(Color.WHITE);
        tierraButton.setBorder(new LineBorder(Color.BLACK));
        tierraButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                Component parentComponent = (Component) evt.getSource();
                // Crear la lista de información
                String[] info = {"<html><b>TITULO:</b> Tierra.</html>",
                    "<html><b>Masa:</b> 1 vez la masa de la Tierra. </html>",
                    "<html><b>Diámetro:</b> Aproximadamente 12,742 kilómetros.</html>",
                    "<html><b>Período orbital:</b> 365.24 días terrestres. </html>",
                    "<html><b>Características:</b> El Sol es una estrella en el centro </html>",
                    "<html> Tiene una atmósfera rica en nitrógeno y oxígeno </html>",
                    "<html> Tercer planeta desde el Sol</html>",
                    "<html>diversidad de ecosistemas. </html>"
                };

                // Personalizar los colores del JOptionPane
                UIManager.put("OptionPane.background", Color.BLACK);
                UIManager.put("OptionPane.messageForeground", Color.BLACK);
                UIManager.put("OptionPane.messageFont", new Font("Arial", Font.BOLD, 16));
                UIManager.put("List.foreground", Color.BLACK);
                UIManager.put("List.font", new Font("Arial", Font.PLAIN, 14));

                // Crear el mensaje con la lista justificada
                JList<String> infoList = new JList<>(info);
                infoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                infoList.setLayoutOrientation(JList.VERTICAL);
                infoList.setVisibleRowCount(-1);
                infoList.setAlignmentX(Component.LEFT_ALIGNMENT);
                infoList.setBackground(Color.BLACK);
                infoList.setForeground(Color.WHITE);
                infoList.setSelectionBackground(Color.BLACK);
                infoList.setSelectionForeground(Color.BLACK);
                infoList.setCellRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        label.setHorizontalAlignment(SwingConstants.LEFT);
                        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                        // Resaltar las primeras palabras en negrita
                        String[] parts = value.toString().split(":", 2);
                        if (parts.length == 2) {
                            label.setText("<html><b>" + parts[0] + ":</b> " + parts[1] + "</html>");
                        }

                        return label;
                    }
                });

                JScrollPane scrollPane = new JScrollPane(infoList);
                scrollPane.setPreferredSize(new Dimension(400, 300));
                scrollPane.setBorder(BorderFactory.createEmptyBorder());

                JOptionPane optionPane = new JOptionPane(scrollPane, JOptionPane.INFORMATION_MESSAGE);
                JDialog dialog = optionPane.createDialog(parentComponent, "INFORMACIÓN DEL PLANETA TIERRA");
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            }
        });
        panel.add(tierraButton);

        //BOTON MARTE
        JButton marteButton = new JButton("MARTE");
        marteButton.setBackground(Color.BLACK);
        marteButton.setForeground(Color.WHITE);
        marteButton.setBorder(new LineBorder(Color.BLACK));
        marteButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                Component parentComponent = (Component) evt.getSource();
                // Crear la lista de información
                String[] info = {"<html><b>TITULO:</b> Marte.</html>",
                    "<html><b>Masa:</b> 0.107 veces la masa de la Tierra. </html>",
                    "<html><b>Diámetro:</b> Aproximadamente 6,779 kilómetros.</html>",
                    "<html><b>Período orbital:</b> 687 días terrestres. </html>",
                    "<html><b>Características:</b> Conocido como el Planeta Rojo </html>",
                    "<html> Tiene una atmósfera delgada compuesta por dioxico de carbono </html>"
                };

                // Personalizar los colores del JOptionPane
                UIManager.put("OptionPane.background", Color.BLACK);
                UIManager.put("OptionPane.messageForeground", Color.BLACK);
                UIManager.put("OptionPane.messageFont", new Font("Arial", Font.BOLD, 16));
                UIManager.put("List.foreground", Color.BLACK);
                UIManager.put("List.font", new Font("Arial", Font.PLAIN, 14));

                // Crear el mensaje con la lista justificada
                JList<String> infoList = new JList<>(info);
                infoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                infoList.setLayoutOrientation(JList.VERTICAL);
                infoList.setVisibleRowCount(-1);
                infoList.setAlignmentX(Component.LEFT_ALIGNMENT);
                infoList.setBackground(Color.BLACK);
                infoList.setForeground(Color.WHITE);
                infoList.setSelectionBackground(Color.BLACK);
                infoList.setSelectionForeground(Color.BLACK);
                infoList.setCellRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        label.setHorizontalAlignment(SwingConstants.LEFT);
                        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                        // Resaltar las primeras palabras en negrita
                        String[] parts = value.toString().split(":", 2);
                        if (parts.length == 2) {
                            label.setText("<html><b>" + parts[0] + ":</b> " + parts[1] + "</html>");
                        }

                        return label;
                    }
                });

                JScrollPane scrollPane = new JScrollPane(infoList);
                scrollPane.setPreferredSize(new Dimension(400, 300));
                scrollPane.setBorder(BorderFactory.createEmptyBorder());

                JOptionPane optionPane = new JOptionPane(scrollPane, JOptionPane.INFORMATION_MESSAGE);
                JDialog dialog = optionPane.createDialog(parentComponent, "INFORMACIÓN DE MARTE");
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            }
        });
        panel.add(marteButton);

        //BOTON jupiter
        JButton jupiterButton = new JButton("JUPITER");
        jupiterButton.setBackground(Color.BLACK);
        jupiterButton.setForeground(Color.WHITE);
        jupiterButton.setBorder(new LineBorder(Color.BLACK));
        jupiterButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                Component parentComponent = (Component) evt.getSource();
                // Crear la lista de información
                String[] info = {"<html><b>TITULO:</b> Jupiter.</html>",
                    "<html><b>Masa:</b>  Aproximadamente 318 veces la masa de la Tierra. </html>",
                    "<html><b>Diámetro:</b> Aproximadamente 139,820 kilómetros.</html>",
                    "<html><b>Período orbital:</b> 11.9 años terrestres. </html>",
                    "<html><b>Características:</b> Júpiter es el planeta más grande del sistema solar </html>",
                    "<html> Tiene una atmósfera turbulenta  </html>",
                    "<html> Júpiter también tiene un sistema de anillos </html>"
                };

                // Personalizar los colores del JOptionPane
                UIManager.put("OptionPane.background", Color.BLACK);
                UIManager.put("OptionPane.messageForeground", Color.BLACK);
                UIManager.put("OptionPane.messageFont", new Font("Arial", Font.BOLD, 16));
                UIManager.put("List.foreground", Color.BLACK);
                UIManager.put("List.font", new Font("Arial", Font.PLAIN, 14));

                // Crear el mensaje con la lista justificada
                JList<String> infoList = new JList<>(info);
                infoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                infoList.setLayoutOrientation(JList.VERTICAL);
                infoList.setVisibleRowCount(-1);
                infoList.setAlignmentX(Component.LEFT_ALIGNMENT);
                infoList.setBackground(Color.BLACK);
                infoList.setForeground(Color.WHITE);
                infoList.setSelectionBackground(Color.BLACK);
                infoList.setSelectionForeground(Color.BLACK);
                infoList.setCellRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        label.setHorizontalAlignment(SwingConstants.LEFT);
                        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                        // Resaltar las primeras palabras en negrita
                        String[] parts = value.toString().split(":", 2);
                        if (parts.length == 2) {
                            label.setText("<html><b>" + parts[0] + ":</b> " + parts[1] + "</html>");
                        }

                        return label;
                    }
                });

                JScrollPane scrollPane = new JScrollPane(infoList);
                scrollPane.setPreferredSize(new Dimension(400, 300));
                scrollPane.setBorder(BorderFactory.createEmptyBorder());

                JOptionPane optionPane = new JOptionPane(scrollPane, JOptionPane.INFORMATION_MESSAGE);
                JDialog dialog = optionPane.createDialog(parentComponent, "INFORMACIÓN DE JUPITER");
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            }
        });
        panel.add(jupiterButton);

        //BOTON saturno
        JButton saturnoButton = new JButton("SATURNO");
        saturnoButton.setBackground(Color.BLACK);
        saturnoButton.setForeground(Color.WHITE);
        saturnoButton.setBorder(new LineBorder(Color.BLACK));
        saturnoButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                Component parentComponent = (Component) evt.getSource();
                // Crear la lista de información
                String[] info = {"<html><b>TITULO:</b> SATURNO.</html>",
                    "<html><b>Masa:</b> Aproximadamente 95 veces la masa de la Tierra. </html>",
                    "<html><b>Diámetro:</b>  Aproximadamente 116,460 kilómetros.</html>",
                    "<html><b>Período orbital:</b> 29.5 años terrestres. </html>",
                    "<html><b>Características:</b> Saturno es conocido por sus impresionantes anillos </html>",
                    "<html> Es el segundo planeta más grande  </html>",
                    "<html> Está compuesto principalmente de hidrógeno y helio. </html>"
                };

                // Personalizar los colores del JOptionPane
                UIManager.put("OptionPane.background", Color.BLACK);
                UIManager.put("OptionPane.messageForeground", Color.BLACK);
                UIManager.put("OptionPane.messageFont", new Font("Arial", Font.BOLD, 16));
                UIManager.put("List.foreground", Color.BLACK);
                UIManager.put("List.font", new Font("Arial", Font.PLAIN, 14));

                // Crear el mensaje con la lista justificada
                JList<String> infoList = new JList<>(info);
                infoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                infoList.setLayoutOrientation(JList.VERTICAL);
                infoList.setVisibleRowCount(-1);
                infoList.setAlignmentX(Component.LEFT_ALIGNMENT);
                infoList.setBackground(Color.BLACK);
                infoList.setForeground(Color.WHITE);
                infoList.setSelectionBackground(Color.BLACK);
                infoList.setSelectionForeground(Color.BLACK);
                infoList.setCellRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        label.setHorizontalAlignment(SwingConstants.LEFT);
                        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                        // Resaltar las primeras palabras en negrita
                        String[] parts = value.toString().split(":", 2);
                        if (parts.length == 2) {
                            label.setText("<html><b>" + parts[0] + ":</b> " + parts[1] + "</html>");
                        }

                        return label;
                    }
                });

                JScrollPane scrollPane = new JScrollPane(infoList);
                scrollPane.setPreferredSize(new Dimension(400, 300));
                scrollPane.setBorder(BorderFactory.createEmptyBorder());

                JOptionPane optionPane = new JOptionPane(scrollPane, JOptionPane.INFORMATION_MESSAGE);
                JDialog dialog = optionPane.createDialog(parentComponent, "INFORMACIÓN DE SATURNO");
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            }
        });
        panel.add(saturnoButton);

        //BOTON urano
        JButton uranoButton = new JButton("URANO");
        uranoButton.setBackground(Color.BLACK);
        uranoButton.setForeground(Color.WHITE);
        uranoButton.setBorder(new LineBorder(Color.BLACK));
        uranoButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                Component parentComponent = (Component) evt.getSource();
                // Crear la lista de información
                String[] info = {"<html><b>TITULO:</b> URANO.</html>",
                    "<html><b>Masa:</b> Aproximadamente 14 veces la masa de la Tierra. </html>",
                    "<html><b>Diámetro:</b> Aproximadamente 50,724 kilómetros.</html>",
                    "<html><b>Período orbital:</b>  84 años terrestres. </html>",
                    "<html><b>Características:</b> Urano es un gigante gaseoso  </html>",
                    "<html> Compuesto principalmente de hidrógeno y helio. </html>",
                    "<html> Su atmósfera contiene metano, que le da un color azul verdoso. </html>",
                    "<html> Tiene anillos y una inclinación axial única. </html>"
                };

                // Personalizar los colores del JOptionPane
                UIManager.put("OptionPane.background", Color.BLACK);
                UIManager.put("OptionPane.messageForeground", Color.BLACK);
                UIManager.put("OptionPane.messageFont", new Font("Arial", Font.BOLD, 16));
                UIManager.put("List.foreground", Color.BLACK);
                UIManager.put("List.font", new Font("Arial", Font.PLAIN, 14));

                // Crear el mensaje con la lista justificada
                JList<String> infoList = new JList<>(info);
                infoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                infoList.setLayoutOrientation(JList.VERTICAL);
                infoList.setVisibleRowCount(-1);
                infoList.setAlignmentX(Component.LEFT_ALIGNMENT);
                infoList.setBackground(Color.BLACK);
                infoList.setForeground(Color.WHITE);
                infoList.setSelectionBackground(Color.BLACK);
                infoList.setSelectionForeground(Color.BLACK);
                infoList.setCellRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        label.setHorizontalAlignment(SwingConstants.LEFT);
                        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                        // Resaltar las primeras palabras en negrita
                        String[] parts = value.toString().split(":", 2);
                        if (parts.length == 2) {
                            label.setText("<html><b>" + parts[0] + ":</b> " + parts[1] + "</html>");
                        }

                        return label;
                    }
                });

                JScrollPane scrollPane = new JScrollPane(infoList);
                scrollPane.setPreferredSize(new Dimension(400, 300));
                scrollPane.setBorder(BorderFactory.createEmptyBorder());

                JOptionPane optionPane = new JOptionPane(scrollPane, JOptionPane.INFORMATION_MESSAGE);
                JDialog dialog = optionPane.createDialog(parentComponent, "INFORMACIÓN DE URANO");
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            }
        });
        panel.add(uranoButton);

        //BOTON urano
        JButton neptunoButton = new JButton("NEPTUNO");
        neptunoButton.setBackground(Color.BLACK);
        neptunoButton.setForeground(Color.WHITE);
        neptunoButton.setBorder(new LineBorder(Color.BLACK));
        neptunoButton.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                Component parentComponent = (Component) evt.getSource();
                // Crear la lista de información
                String[] info = {"<html><b>TITULO:</b> NEPTUNO.</html>",
                    "<html><b>Masa:</b> 1 vez la masa de la Tierra. </html>",
                    "<html><b>Diámetro:</b> Aproximadamente 49,244 kilómetros. </html>",
                    "<html><b>Período orbital:</b> 165 años terrestres. </html>",
                    "<html><b>Características:</b> Neptuno es otro gigante gaseoso. </html>",
                    "<html> Su atmósfera contiene metano </html>",
                    "<html> Neptuno tiene vientos atmosféricos extremadamente rápidos. </html>"
                };

                // Personalizar los colores del JOptionPane
                UIManager.put("OptionPane.background", Color.BLACK);
                UIManager.put("OptionPane.messageForeground", Color.BLACK);
                UIManager.put("OptionPane.messageFont", new Font("Arial", Font.BOLD, 16));
                UIManager.put("List.foreground", Color.BLACK);
                UIManager.put("List.font", new Font("Arial", Font.PLAIN, 14));

                // Crear el mensaje con la lista justificada
                JList<String> infoList = new JList<>(info);
                infoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                infoList.setLayoutOrientation(JList.VERTICAL);
                infoList.setVisibleRowCount(-1);
                infoList.setAlignmentX(Component.LEFT_ALIGNMENT);
                infoList.setBackground(Color.BLACK);
                infoList.setForeground(Color.WHITE);
                infoList.setSelectionBackground(Color.BLACK);
                infoList.setSelectionForeground(Color.BLACK);
                infoList.setCellRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        label.setHorizontalAlignment(SwingConstants.LEFT);
                        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

                        // Resaltar las primeras palabras en negrita
                        String[] parts = value.toString().split(":", 2);
                        if (parts.length == 2) {
                            label.setText("<html><b>" + parts[0] + ":</b> " + parts[1] + "</html>");
                        }

                        return label;
                    }
                });

                JScrollPane scrollPane = new JScrollPane(infoList);
                scrollPane.setPreferredSize(new Dimension(400, 300));
                scrollPane.setBorder(BorderFactory.createEmptyBorder());

                JOptionPane optionPane = new JOptionPane(scrollPane, JOptionPane.INFORMATION_MESSAGE);
                JDialog dialog = optionPane.createDialog(parentComponent, "INFORMACIÓN DE NEPTUNO");
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            }
        });
        panel.add(neptunoButton);

        return panel;
    }

    /**
     * Método para crear el grafo de escena que representa el sistema solar.
     * @return BranchGroup que contiene los objetos y comportamientos del sistema solar.
     */
    private BranchGroup crearGrafoEscena() {
        // Crear el nodo raíz del grafo de escena
        BranchGroup objetoRaiz = new BranchGroup();

        // Crear un TransformGroup para el nodo raíz
        TransformGroup tgRaiz = new TransformGroup();
        tgRaiz.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        tgRaiz.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        objetoRaiz.addChild(tgRaiz);

        // Crear un TransformGroup para el sistema solar
        sistemaSolarTransform = new TransformGroup();
        sistemaSolarTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
        sistemaSolarTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        tgRaiz.addChild(sistemaSolarTransform);

        // Crear una esfera para representar el Sol
        Sphere esfera1 = new Sphere(0.17f, Primitive.GENERATE_TEXTURE_COORDS, crearApariencia("src/texturas_estrellas/sol.jpg"));
        TransformGroup esfera1Transform = new TransformGroup();
        esfera1Transform.addChild(esfera1);
        sistemaSolarTransform.addChild(esfera1Transform);

        // Mercurio
        Sphere mercurio = new Sphere(0.04f, Primitive.GENERATE_TEXTURE_COORDS, crearApariencia("src/texturas_planetas/mercurio.jpg"));
        TransformGroup mercurioRotXformGroup = Posi.rotate(mercurio, new Alpha(-1, 1700));
        TransformGroup mercurioTransXformGroup = Posi.translate(mercurioRotXformGroup, new Vector3f(0.216f, 0.0f, 0.125f));
        sistemaSolarTransform.addChild(mercurioTransXformGroup);

        // Crear la órbita para Mercurio
        LineArray orbitaMercurio = new LineArray(100, LineArray.COORDINATES);
        float radioOrbitaMercurio = 0.25f;
        for (int i = 0; i < 100; i++) {
            double angle = 2.0 * Math.PI * i / 100;
            float x = (float) (radioOrbitaMercurio * Math.cos(angle));
            float z = (float) (radioOrbitaMercurio * Math.sin(angle));
            orbitaMercurio.setCoordinate(i, new Point3f(x, 0.0f, z));
        }
        Shape3D orbitaMercurioShape = new Shape3D(orbitaMercurio);
        tgRaiz.addChild(orbitaMercurioShape);

        // Venus
        Sphere venus = new Sphere(0.06f, Primitive.GENERATE_TEXTURE_COORDS, crearApariencia("src/texturas_planetas/venus.jpg"));
        TransformGroup venusRotXformGroup = Posi.rotate(venus, new Alpha(-1, 1700));
        TransformGroup venusTransXformGroup = Posi.translate(venusRotXformGroup, new Vector3f(-0.201f, 0.0f, 0.346f));
        sistemaSolarTransform.addChild(venusTransXformGroup);
        // Crear la órbita para Venus
        LineArray orbitaVenus = new LineArray(100, LineArray.COORDINATES);
        float radioOrbitaVenus = 0.4f;
        for (int i = 0; i < 100; i++) {
            double angle = 2.0 * Math.PI * i / 100;
            float x = (float) (radioOrbitaVenus * Math.cos(angle));
            float z = (float) (radioOrbitaVenus * Math.sin(angle));
            orbitaVenus.setCoordinate(i, new Point3f(x, 0.0f, z));
        }
        Shape3D orbitaVenusShape = new Shape3D(orbitaVenus);
        tgRaiz.addChild(orbitaVenusShape);

        // Tierra
        Sphere tierra = new Sphere(0.07f, Primitive.GENERATE_TEXTURE_COORDS, crearApariencia("src/texturas_planetas/tierra.jpg"));
        TransformGroup tierraRotXformGroup = Posi.rotate(tierra, new Alpha(-1, 1700));
        TransformGroup tierraTransXformGroup = Posi.translate(tierraRotXformGroup, new Vector3f(-0.4769f, 0.0f, -0.2748f));
        sistemaSolarTransform.addChild(tierraTransXformGroup);
        // Crear la órbita para la Tierra
        LineArray orbitaTierra = new LineArray(100, LineArray.COORDINATES);
        float radioOrbitaTierra = 0.55f;
        for (int i = 0; i < 100; i++) {
            double angle = 2.0 * Math.PI * i / 100;
            float x = (float) (radioOrbitaTierra * Math.cos(angle));
            float z = (float) (radioOrbitaTierra * Math.sin(angle));
            orbitaTierra.setCoordinate(i, new Point3f(x, 0.0f, z));
        }
        Shape3D orbitaTierraShape = new Shape3D(orbitaTierra);
        tgRaiz.addChild(orbitaTierraShape);

        // Luna
        Sphere luna = new Sphere(0.03f, Primitive.GENERATE_TEXTURE_COORDS, crearApariencia("src/texturas_satelites/luna.jpg"));
        TransformGroup lunaRotXformGroup = Posi.rotate(luna, new Alpha(-1, 1700));
        TransformGroup lunaTransXformGroup = Posi.translate(lunaRotXformGroup, new Vector3f(-0.5405f, 0.0f, -0.1015f));
        sistemaSolarTransform.addChild(lunaTransXformGroup);

        // Crear la órbita para la Luna
        LineArray orbitaLuna = new LineArray(100, LineArray.COORDINATES);
        float radioOrbitaLuna = 0.55f;
        for (int i = 0; i < 100; i++) {
            double angle = 2.0 * Math.PI * i / 100;
            float x = (float) (radioOrbitaLuna * Math.cos(angle));
            float z = (float) (radioOrbitaLuna * Math.sin(angle));
            orbitaLuna.setCoordinate(i, new Point3f(x, 0.0f, z));
        }

        Shape3D orbitaLunaShape = new Shape3D(orbitaLuna);
        tgRaiz.addChild(orbitaLunaShape);

        // Marte
        Sphere marte = new Sphere(0.06f, Primitive.GENERATE_TEXTURE_COORDS, crearApariencia("src/texturas_planetas/marte.jpg"));
        TransformGroup marteRotXformGroup = Posi.rotate(marte, new Alpha(-1, 1700));
        TransformGroup marteTransXformGroup = Posi.translate(marteRotXformGroup, new Vector3f(0.3499f, 0.0f, -0.6063f));
        sistemaSolarTransform.addChild(marteTransXformGroup);

        // Crear la órbita para Marte
        LineArray orbitaMarte = new LineArray(100, LineArray.COORDINATES);
        float radioOrbitaMarte = 0.7f;
        for (int i = 0; i < 100; i++) {
            double angle = 2.0 * Math.PI * i / 100;
            float x = (float) (radioOrbitaMarte * Math.cos(angle));
            float z = (float) (radioOrbitaMarte * Math.sin(angle));
            orbitaMarte.setCoordinate(i, new Point3f(x, 0.0f, z));
        }
        Shape3D orbitaMarteShape = new Shape3D(orbitaMarte);
        tgRaiz.addChild(orbitaMarteShape);

        // Júpiter
        Sphere jupiter = new Sphere(0.11f, Primitive.GENERATE_TEXTURE_COORDS, crearApariencia("src/texturas_planetas/jupiter.jpg"));
        TransformGroup jupiterRotXformGroup = Posi.rotate(jupiter, new Alpha(-1, 1700));
        TransformGroup jupiterTransXformGroup = Posi.translate(jupiterRotXformGroup, new Vector3f(0.4498f, 0.0f, 0.7793f));
        sistemaSolarTransform.addChild(jupiterTransXformGroup);

        // Crear la órbita para Júpiter
        LineArray orbitaJupiter = new LineArray(100, LineArray.COORDINATES);
        float radioOrbitaJupiter = 0.9f;
        for (int i = 0; i < 100; i++) {
            double angle = 2.0 * Math.PI * i / 100;
            float x = (float) (radioOrbitaJupiter * Math.cos(angle));
            float z = (float) (radioOrbitaJupiter * Math.sin(angle));
            orbitaJupiter.setCoordinate(i, new Point3f(x, 0.0f, z));
        }
        Shape3D orbitaJupiterShape = new Shape3D(orbitaJupiter);
        tgRaiz.addChild(orbitaJupiterShape);

        // Saturno
        // Saturno
        Sphere saturno = new Sphere(0.1f, Primitive.GENERATE_TEXTURE_COORDS, crearApariencia("src/texturas_planetas/saturno.jpg"));
        TransformGroup saturnoRotXformGroup = Posi.rotate(saturno, new Alpha(-1, 1700));
        TransformGroup saturnoTransXformGroup = Posi.translate(saturnoRotXformGroup, new Vector3f(-1.0394f, 0.0f, 0.5595f));
        sistemaSolarTransform.addChild(saturnoTransXformGroup);

        // Crear la órbita para Saturno
        LineArray orbitaSaturno = new LineArray(100, LineArray.COORDINATES);
        float radioOrbitaSaturno = 1.2f;
        for (int i = 0; i < 100; i++) {
            double angle = 2.0 * Math.PI * i / 100;
            float x = (float) (radioOrbitaSaturno * Math.cos(angle));
            float z = (float) (radioOrbitaSaturno * Math.sin(angle));
            orbitaSaturno.setCoordinate(i, new Point3f(x, 0.0f, z));
        }
        Shape3D orbitaSaturnoShape = new Shape3D(orbitaSaturno);
        tgRaiz.addChild(orbitaSaturnoShape);

        // Urano
        Sphere urano = new Sphere(0.08f, Primitive.GENERATE_TEXTURE_COORDS, crearApariencia("src/texturas_planetas/urano.jpg"));
        TransformGroup uranoRotXformGroup = Posi.rotate(urano, new Alpha(-1, 1700));
        TransformGroup uranoTransXformGroup = Posi.translate(uranoRotXformGroup, new Vector3f(-0.749f, 0.0f, -1.3f));
        sistemaSolarTransform.addChild(uranoTransXformGroup);

        // Crear la órbita para Urano
        LineArray orbitaUrano = new LineArray(100, LineArray.COORDINATES);
        float radioOrbitaUrano = 1.5f;
        for (int i = 0; i < 100; i++) {
            double angle = 2.0 * Math.PI * i / 100;
            float x = (float) (radioOrbitaUrano * Math.cos(angle));
            float z = (float) (radioOrbitaUrano * Math.sin(angle));
            orbitaUrano.setCoordinate(i, new Point3f(x, 0.0f, z));
        }
        Shape3D orbitaUranoShape = new Shape3D(orbitaUrano);
        tgRaiz.addChild(orbitaUranoShape);

        // Neptuno
        Sphere neptuno = new Sphere(0.09f, Primitive.GENERATE_TEXTURE_COORDS, crearApariencia("src/texturas_planetas/neptuno.jpg"));
        TransformGroup neptunoRotXformGroup = Posi.rotate(neptuno, new Alpha(-1, 1700));
        TransformGroup neptunoTransXformGroup = Posi.translate(neptunoRotXformGroup, new Vector3f(1.4716f, 0.0f, -0.85f));
        sistemaSolarTransform.addChild(neptunoTransXformGroup);

        // Crear la órbita para Neptuno
        LineArray orbitaNeptuno = new LineArray(100, LineArray.COORDINATES);
        float radioOrbitaNeptuno = 1.7f;
        for (int i = 0; i < 100; i++) {
            double angle = 2.0 * Math.PI * i / 100;
            float x = (float) (radioOrbitaNeptuno * Math.cos(angle));
            float z = (float) (radioOrbitaNeptuno * Math.sin(angle));
            orbitaNeptuno.setCoordinate(i, new Point3f(x, 0.0f, z));
        }
        Shape3D orbitaNeptunoShape = new Shape3D(orbitaNeptuno);
        tgRaiz.addChild(orbitaNeptunoShape);

        // Crear la rotación del sistema solar
        Alpha sistemaSolarAlpha = new Alpha(-1, Alpha.INCREASING_ENABLE, 0, 0, 8000, 0, 0, 0, 0, 0);
        RotationInterpolator sistemaSolarInterpolator = new RotationInterpolator(sistemaSolarAlpha, sistemaSolarTransform);
        sistemaSolarInterpolator.setSchedulingBounds(objetoRaiz.getBounds());
        sistemaSolarTransform.addChild(sistemaSolarInterpolator);

        // Agregar comportamiento de rotación del mouse
        MouseRotate mr = new MouseRotate();
        mr.setTransformGroup(tgRaiz);
        mr.setSchedulingBounds(objetoRaiz.getBounds());
        tgRaiz.addChild(mr);

        // Agregar comportamiento de zoom del mouse
        MouseWheelZoom mwz = new MouseWheelZoom();
        mwz.setTransformGroup(tgRaiz);
        mwz.setSchedulingBounds(objetoRaiz.getBounds());
        tgRaiz.addChild(mwz);

        // Agregar comportamiento de navegación con teclado
        ViewingPlatform vp = universe.getViewingPlatform();
        TransformGroup vpTransformGroup = vp.getViewPlatformTransform();
        KeyNavigatorBehavior knb = new KeyNavigatorBehavior(vpTransformGroup);
        knb.setSchedulingBounds(objetoRaiz.getBounds());
        tgRaiz.addChild(knb);

        return objetoRaiz;
    }

    /**
     * Método para crear una apariencia para los objetos 3D utilizando una textura.
     * @param rutaImagen La ruta de la imagen que se utilizará como textura.
     * @return Una instancia de la clase Appearance que contiene la apariencia configurada con la textura y el material.
     */
    private Appearance crearApariencia(String rutaImagen) {
        // Crear una nueva instancia de Appearance
        Appearance apariencia = new Appearance();

        // Cargar la imagen como textura
        TextureLoader loader = new TextureLoader(rutaImagen, "RGB", this);
        ImageComponent2D imagen = loader.getImage();
        Texture2D textura = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA, imagen.getWidth(), imagen.getHeight());
        textura.setImage(0, imagen);
        textura.setEnable(true);
        
        // Configurar los filtros y el modo de repetición de la textura
        textura.setMagFilter(Texture.BASE_LEVEL_POINT);
        textura.setMinFilter(Texture.BASE_LEVEL_LINEAR);
        textura.setBoundaryModeS(Texture.WRAP);
        textura.setBoundaryModeT(Texture.WRAP);

        // Configurar los atributos de la textura
        TextureAttributes texturaAtributos = new TextureAttributes();
        texturaAtributos.setTextureMode(TextureAttributes.MODULATE);
        
        // Asignar la textura y los atributos de la textura a la apariencia
        apariencia.setTexture(textura);
        apariencia.setTextureAttributes(texturaAtributos);
        
        // Crear un nuevo material y configurar sus colores
        Material material = new Material();
        material.setAmbientColor(new Color3f(0.3f, 0.3f, 0.3f));
        material.setDiffuseColor(new Color3f(0.8f, 0.8f, 0.8f));
        material.setSpecularColor(new Color3f(0.0f, 0.0f, 0.0f));
        
        // Asignar el material a la apariencia
        apariencia.setMaterial(material);
        
        // Devolver la apariencia configurada
        return apariencia;
    }

    /**
     * Clase interna ButtonClickListener que implementa la interfaz ActionListener.
     * Esta clase es responsable de manejar los eventos de clic en los botones del panel.
     */
    private class ButtonClickListener implements ActionListener {
        /**
         * Método actionPerformed que se ejecuta cuando se presiona un botón.
         * @param event El evento de acción generado por el botón.
         */
        public void actionPerformed(ActionEvent event) {
            // Obtener el nombre del planeta desde el evento
            String planetName = event.getActionCommand();
            // Mostrar un mensaje en la consola indicando el botón presionado
            System.out.println("Presionaste el botón: " + planetName);
        }
    }

    /**
     * Método principal que inicia la aplicación.
     *
     * @param args Los argumentos de la línea de comandos (no se utilizan en este caso).
     */
    public static void main(String[] args) {
        // Invocar la interfaz de usuario en el hilo de eventos para garantizar la seguridad de subprocesos
        EventQueue.invokeLater(() -> {
            // Crear una instancia de la clase SistemaSolar (que extiende JFrame)
            SistemaSolar frame = new SistemaSolar();
            // Hacer visible el marco
            frame.setVisible(true);
        });
    }
}
