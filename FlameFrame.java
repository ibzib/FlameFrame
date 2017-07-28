import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FlameFrame extends JPanel {
    private static final long serialVersionUID = 3L;
    // program state information
    private Function[] functionSet;
    private ChaosGame game;
    private BufferedImage currentImage;
    private int targetFPS = 15;
    private boolean iterationPaused = false;
    private boolean renderingStopped = false;
    private int iterationsPerFrame = 1000;
    private int drawNowIterations = 5000000;
    private Position previousScroll = new Position(0, 0);
    // UI components
    private final String drawNowStr = "Draw Now";
    private final String appName = "FlameFrame";
    JTextField iterationsPerFrameInput = new JTextField(String.format("%d", iterationsPerFrame));
    JTextField drawNowInput = new JTextField(String.format("%d", drawNowIterations));
    private JTextField[] blendInputs = new JTextField[Function.variations.length];
    private JFrame frame = new JFrame();
    ViewManager viewManager = new ViewManager();
    private JPanel sidebar = new JPanel();
    private JMenuBar menuBar = new JMenuBar();
    private JMenuItem playPauseMenuItem;
    // black and white / color
    private boolean showBlackAndWhite;
    private JMenuItem blackAndWhiteMenuItem;
    private final String showColorMessage = "Show Color";
    private final String showBlackAndWhiteMessage = "Show Black and White";
    // bold / unbold
    private boolean showBold;
    private JMenuItem boldMenuItem;
    private final String showBoldMessage = "Bold Points";
    private final String unBoldMessage = "Unbold Points";

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        FlameFrame app = new FlameFrame();
        try {
            app.renderLoop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Application exited.");
    }

    private void togglePlayPause() {
        if (iterationPaused) {
            play();
        } else {
            pause();
        }
    }

    private void setInputEnabled(boolean status) {
        viewManager.setEnabled(status);
    }

    private void pause() {
        iterationPaused = true;
        playPauseMenuItem.setText("Play");
    }

    private void play() {
        renderingStopped = false;
        iterationPaused = false;
        setInputEnabled(true);
        playPauseMenuItem.setText("Pause");
    }

    private void stop() {
        setInputEnabled(false);
        pause();
        renderingStopped = true;
    }

    private void saveImage() {
        try {
            // TODO let user choose image size
            stop();
            File imagesDir = new File("images");
            imagesDir.mkdir();
            JFileChooser chooser = new JFileChooser(imagesDir);
            chooser.setSelectedFile(new File("cool_fractal"));
            chooser.setDialogType(JFileChooser.SAVE_DIALOG);
            FileNameExtensionFilter jpgFilter = new FileNameExtensionFilter("JPEG Image", "jpg", "jpeg");
            FileNameExtensionFilter pngFilter = new FileNameExtensionFilter("Portable Network Graphic (PNG)", "png");
            // TODO Figure out why BMP images won't save
            // FileNameExtensionFilter bmpFilter = new
            // FileNameExtensionFilter("Windows Bitmap (BMP)", "bmp");
            // chooser.addChoosableFileFilter(bmpFilter);
            chooser.setFileFilter(jpgFilter);
            chooser.addChoosableFileFilter(pngFilter);
            chooser.setAcceptAllFileFilterUsed(false);
            chooser.setMultiSelectionEnabled(false);
            int returnVal = chooser.showSaveDialog(frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                String inputtedPath = chooser.getSelectedFile().getAbsolutePath();
                String extension = ((FileNameExtensionFilter) (chooser.getFileFilter())).getExtensions()[0];
                String outputImageFileName = ImageManager.saveImage(currentImage, inputtedPath, extension);
                Function.record(functionSet, outputImageFileName);
            }
        } catch (IOException ioex) {
            // TODO FileNotFoundException not caught for some reason
            JOptionPane.showMessageDialog(frame, "Error: " + ioex.getMessage());
        } finally {
            play();
        }
    }

    private void clearScreen() {
        game.resize(this.getWidth(), this.getHeight());
    }

    private void toggleBlackAndWhite() {
        showBlackAndWhite = !showBlackAndWhite;
        if (showBlackAndWhite) {
            blackAndWhiteMenuItem.setText(showColorMessage);
        } else {
            blackAndWhiteMenuItem.setText(showBlackAndWhiteMessage);
        }
    }

    private void toggleBold() {
        showBold = !showBold;
        if (showBold) {
            boldMenuItem.setText(unBoldMessage);
        } else {
            boldMenuItem.setText(showBoldMessage);
        }
    }

    public FlameFrame() {
        initialize();
        setDoubleBuffered(true);
        setUpMenuBar();
        setUpSidebar();
        setUpFrame();
    }
    
    private void setUpSidebar() {
        sidebar.setLayout(new BorderLayout());
        // iteration settings
        JPanel iterationPanel = new JPanel(new GridLayout(4, 2));
        // draw now
        JLabel drawNowTitle = new JLabel(drawNowStr);
        Font boldFont = new Font(drawNowTitle.getFont().getFontName(), Font.BOLD, drawNowTitle.getFont().getSize());
        drawNowTitle.setFont(boldFont);
        iterationPanel.add(drawNowTitle);
        iterationPanel.add(new JLabel());
        iterationPanel.add(drawNowInput);
        JButton drawNowButton = new JButton(drawNowAction);
        drawNowButton.setText("Go");
        iterationPanel.add(drawNowButton);
        // per frame
        JLabel settingsTitle = new JLabel("Settings");
        settingsTitle.setFont(boldFont);
        iterationPanel.add(settingsTitle);
        iterationPanel.add(new JLabel());
        JLabel perFrameMessage = new JLabel("Points/Frame");
        perFrameMessage.setForeground(Color.gray);
        iterationPanel.add(perFrameMessage);
        iterationPanel.add(iterationsPerFrameInput);
        sidebar.add(iterationPanel, BorderLayout.NORTH);
        // blend settings
        JPanel blendPanel = new JPanel(new GridLayout(Function.variations.length, 2));
        for (int i = 0; i < Function.variations.length; i++) {
            JLabel variationLabel = new JLabel(Function.variations[i].getName());
            variationLabel.setForeground(Color.gray);
            blendPanel.add(variationLabel);
            blendInputs[i] = new JTextField(String.format("%f", functionSet[0].getBlend(i)));
            blendPanel.add(blendInputs[i]);
        }
        JScrollPane blendScrollPane = new JScrollPane(blendPanel);
        JLabel headerLabel = new JLabel("Blend Values");
        JPanel header = new JPanel();
        header.add(headerLabel);
        blendScrollPane.setColumnHeaderView(header);
        blendScrollPane.setBorder(null);
        sidebar.add(blendScrollPane, BorderLayout.CENTER);
        // finishing touches
        double minimumWidth = Math.max(iterationPanel.getPreferredSize().getWidth(),
                blendPanel.getPreferredSize().getWidth());
        sidebar.setMinimumSize(new Dimension((int) minimumWidth, 0));
        sidebar.setSize(sidebar.getMinimumSize());
        JButton applySettingsButton = new JButton("Apply Settings");
        applySettingsButton.addActionListener(applySettingsAction);
        sidebar.add(applySettingsButton, BorderLayout.SOUTH);
    }

    private void setUpFrame() {
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this, sidebar);
        splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.99);
        splitPane.setBorder(null);
        // TODO make double-click collapse sidebar
        splitPane.setOneTouchExpandable(true);
        frame.getContentPane().add(splitPane);
        // NOTE macOS does not support frame icons.
        try {
            String[] iconPaths = new String[] { "res/icon128.png", "res/icon64.png", "res/icon32.png",
                    "res/icon16.png" };
            ArrayList<BufferedImage> iconImages = new ArrayList<BufferedImage>();
            for (int i = 0; i < iconPaths.length; i++) {
                System.out.println("read in " + iconPaths[i]);
                InputStream imgStream = FlameFrame.class.getResourceAsStream(iconPaths[i]);
                iconImages.add(ImageIO.read(imgStream));
            }
            frame.setIconImages(iconImages);
        } catch (IOException e) {
            System.err.println("Failed to load icon");
            e.printStackTrace();
        }
        frame.setJMenuBar(menuBar);
        frame.setTitle(appName);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        viewManager.setParent(this);
        this.addMouseListener(viewManager);
        this.addMouseMotionListener(viewManager);
        this.addMouseWheelListener(viewManager);
        frame.setVisible(true);
    }

    private AbstractAction drawNowAction = new AbstractAction() {
        private static final long serialVersionUID = 5949543484405754612L;

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int iterations = Integer.parseInt(drawNowInput.getText());
                if (iterations <= 0) {
                    throw new NumberFormatException("Number of points to draw must be greater than 0");
                }
                Cursor normalCursor = frame.getCursor();
                frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                iterate(iterations);
                frame.setCursor(normalCursor);
            } catch (NumberFormatException ex) {
                stop();
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                play();
            }
        }
    };
    
    private AbstractAction applySettingsAction = new AbstractAction() {

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int ipfInput = Integer.parseInt(iterationsPerFrameInput.getText());
                if (ipfInput <= 0) {
                    throw new NumberFormatException("Points/Frame must be greater than 0");
                }
                iterationsPerFrame = ipfInput;
                for (int i = 0; i < Function.variations.length; i++) {
                    double blendInput = Double.parseDouble(blendInputs[i].getText());
                    if (blendInput < 0) {
                        throw new NumberFormatException("Blend value cannot be negative");
                    }
                    for (int j = 0; j < functionSet.length; j++) {
                        functionSet[j].setBlend(i, blendInput);
                    }
                }
            } catch (NumberFormatException ex) {
                stop();
                JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage());
                play();
            }
            for (int i = 0; i < Function.variations.length; i++) {
                blendInputs[i].setText(String.format("%f", functionSet[0].getBlend(i)));
            }
            clearScreen();
        }
    };

    private void setUpMenuBar() {
        //////////////
        // File menu
        //////////////
        JMenu fileMenu = new JMenu("File");
        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.addActionListener((ActionEvent e) -> {
            saveImage();
        });
        saveMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        fileMenu.add(saveMenuItem);
        menuBar.add(fileMenu);

        ////////////////////
        // Appearance menu
        ////////////////////
        JMenu appearanceMenu = new JMenu("Appearance");

        showBlackAndWhite = false;
        String bwMessage = showBlackAndWhite ? showColorMessage : showBlackAndWhiteMessage;
        blackAndWhiteMenuItem = new JMenuItem(bwMessage);
        blackAndWhiteMenuItem.addActionListener((ActionEvent e) -> {
            toggleBlackAndWhite();
        });

        blackAndWhiteMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_0, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        appearanceMenu.add(blackAndWhiteMenuItem);

        showBold = false;
        String boldMessage = showBold ? unBoldMessage : showBoldMessage;
        boldMenuItem = new JMenuItem(boldMessage);
        boldMenuItem.addActionListener((ActionEvent e) -> {
            toggleBold();
        });
        boldMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_B, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        appearanceMenu.add(boldMenuItem);

        menuBar.add(appearanceMenu);

        ///////////////////
        // iteration menu
        ///////////////////
        JMenu iterationMenu = new JMenu("Iterate");

        playPauseMenuItem = new JMenuItem("Pause");
        playPauseMenuItem.addActionListener((ActionEvent e) -> {
            togglePlayPause();
        });
        playPauseMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_K, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        iterationMenu.add(playPauseMenuItem);

        JMenuItem clearMenuItem = new JMenuItem("Refresh Screen");
        clearMenuItem.addActionListener(applySettingsAction);
        clearMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        iterationMenu.add(clearMenuItem);

        JMenuItem addIterationsMenuItem = new JMenuItem(drawNowStr);
        addIterationsMenuItem.addActionListener(drawNowAction);
        getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()),
                "drawNowAction");
        getActionMap().put("drawNowAction", drawNowAction);
        addIterationsMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        iterationMenu.add(addIterationsMenuItem);
        menuBar.add(iterationMenu);

        //////////////
        // View menu
        //////////////
        JMenu viewMenu = new JMenu("View");

        JMenuItem zoomInMenuItem = new JMenuItem("Zoom In");
        zoomInMenuItem.addActionListener((ActionEvent e) -> {
            if (!iterationPaused) {
                viewManager.setZoom(1.2);
            }
        });
        zoomInMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        viewMenu.add(zoomInMenuItem);

        JMenuItem zoomOutMenuItem = new JMenuItem("Zoom Out");
        zoomOutMenuItem.addActionListener((ActionEvent e) -> {
            if (!iterationPaused) {
                viewManager.setZoom(1.0 / 1.2);
            }
        });
        zoomOutMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        viewMenu.add(zoomOutMenuItem);

        JMenuItem rotateClockwiseMenuItem = new JMenuItem("Rotate Clockwise");
        rotateClockwiseMenuItem.addActionListener((ActionEvent e) -> {
            if (!iterationPaused) {
                viewManager.rotate(1);                
            }
        });
        rotateClockwiseMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        viewMenu.add(rotateClockwiseMenuItem);

        JMenuItem rotateCounterClockwiseMenuItem = new JMenuItem("Rotate Counterclockwise");
        rotateCounterClockwiseMenuItem.addActionListener((ActionEvent e) -> {
            if (!iterationPaused) {
                viewManager.rotate(-1);                
            }
        });
        rotateCounterClockwiseMenuItem.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        viewMenu.add(rotateCounterClockwiseMenuItem);

        menuBar.add(viewMenu);

    }

    private void initialize() {
        game = new ChaosGame(this.getWidth(), this.getHeight());
        functionSet = new Function[] {
                new Function(MyColor.getColor(0.0), new double[] { 0.5f, 0f, 0f, 0f, 0.5f, 0f }, 1),
                new Function(MyColor.getColor(0.3333333), new double[] { 0.5f, 0f, 0.5f, 0f, 0.5f, 0 }, 1),
                new Function(MyColor.getColor(0.6666666), new double[] { 0.5f, 0f, 0f, 0f, 0.5f, 0.5f }, 1) };
        for (Function func : functionSet) {
            func.setBlend(0, 1.0);
            // func.setBlend(1, 0.25);
            // func.setBlend(2, 1.0); // TODO FIX ME: NaN
            // func.setBlend(3, 1);
            func.setBlend(4, 1.0);
            // func.setBlend(5, 1.0);
            func.setBlend(6, 1.0);
        }
    }

    public void renderLoop() throws InterruptedException {
        while (true) {
            long startTime = System.nanoTime();
            if (!renderingStopped) {
                this.repaint();
            }
            long timeElapsedMs = (System.nanoTime() - startTime) / 1000;
            long sleepTime = 1000 / targetFPS - timeElapsedMs;
            if (sleepTime > 0) {
                Thread.sleep(sleepTime);
            }
        }
    }

    private void iterate(int iterations) {
        Position scroll = viewManager.getScroll();
        if (!scroll.equals(previousScroll) || game.getWidth() != this.getWidth()
                || game.getHeight() != this.getHeight()) {
            clearScreen();
            iterations *= 10;
        }
        previousScroll = scroll.clone();
        game.iterate(functionSet, iterations, previousScroll, viewManager.getZoom(), viewManager.getRotation());
    }

    private void updateImage() {
        currentImage = ImageManager.getImage(game, showBlackAndWhite, showBold);
    }

    public void paintComponent(Graphics g) {
        if (!iterationPaused) {
            iterate(iterationsPerFrame);
        }
        updateImage();
        g.drawImage(currentImage, 0, 0, null);
        if (iterationPaused) {
            g.setColor(Color.white);
            g.drawString("Paused", 15, 15);
        }
    }
}
