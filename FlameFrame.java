import java.awt.Color;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FlameFrame extends JComponent {
	private static final long serialVersionUID = 1L;
	MouseHandler mouseHandler = new MouseHandler();
	private JFrame frame = new JFrame("FlameFrame");
	private JMenuBar menuBar = new JMenuBar();
	private JMenuItem playPauseMenuItem;
	private JMenuItem boldMenuItem;
	private int targetFPS = 15;
	private boolean gamePaused = false;
	private boolean renderingStopped = false;
	private ChaosGame game;
	private BufferedImage currentImage;
	private int lowIterations = 1000;
	private int highIterations = 10000000;
	private Function[] functionSet;
	private double previousZoom;
	private Point previousScroll = new Point(0, 0);
	public static void main(String[] args) {
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
		if (gamePaused) {
			play();
		} else {
			pause();
		}
	}
	private void setInputEnabled(boolean status) {
		mouseHandler.setEnabled(status);
	}
	private void pause() {
		gamePaused = true;
		setInputEnabled(false);
		playPauseMenuItem.setText("Play");
		System.out.println("Paused simulation");
	}
	private void play() {
		renderingStopped = false;
		gamePaused = false;
		setInputEnabled(true);
		playPauseMenuItem.setText("Pause");
		System.out.println("Played simulation");
	}
	private void stop() {
		pause();
		renderingStopped = true;
	}
	private void saveImage() {
		try {
			// TODO let user choose image size
			stop();
			JFileChooser chooser = new JFileChooser(System.getProperty("user.dir"));
			chooser.setDialogType(JFileChooser.SAVE_DIALOG);
			FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG Image", "png");
			chooser.setFileFilter(filter);
			chooser.setSelectedFile(new File("fractal.png"));
			int returnVal = chooser.showSaveDialog(frame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String imageFileName = Painter.saveImage(currentImage, chooser.getSelectedFile().getName());
				Function.record(functionSet, imageFileName);
			}
			play();
		} catch (IOException ex) {
			// TODO report error to user in dialog box
			ex.printStackTrace();
		}
	}
	private void resetGame() {
//		System.out.println("Clearing points");
		game.resize(frame.getWidth(), frame.getHeight());
	}
	private void toggleBold() {
		Painter.boldPoints = !Painter.boldPoints;
		if (Painter.boldPoints) {
			boldMenuItem.setText("Unbold");
		} else {
			boldMenuItem.setText("Bold");
		}
	}
	public FlameFrame() {
		initialize();
		setDoubleBuffered(true);
		setUpMenuBar();
		setUpFrame();
	}
	private void setUpFrame() {
//		frame.pack();
		previousZoom = mouseHandler.getZoom();
		frame.getContentPane().add(this);
		frame.setJMenuBar(menuBar);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.addMouseListener(mouseHandler);
		frame.addMouseMotionListener(mouseHandler);
		frame.addMouseWheelListener(mouseHandler);
		frame.setVisible(true);
	}
	private void setUpMenuBar() {
		//////////////
		// File menu
		//////////////
		JMenu fileMenu = new JMenu("File");
		JMenuItem saveMenuItem = new JMenuItem("Save");
		saveMenuItem.addActionListener((ActionEvent e) -> {
			saveImage();
		});
		saveMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileMenu.add(saveMenuItem);
		menuBar.add(fileMenu);
		
		////////////////////
		// Appearance menu
		////////////////////
		JMenu appearanceMenu = new JMenu("Appearance");
		
		JMenuItem blackAndWhiteMenuItem = new JMenuItem("Black and White");
		blackAndWhiteMenuItem.addActionListener((ActionEvent e) -> {
			Painter.setColorOffset(Painter.ColorScheme.BLACK_AND_WHITE);	
		});
		blackAndWhiteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, 
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		appearanceMenu.add(blackAndWhiteMenuItem);
		
		JMenuItem sherbetMenuItem = new JMenuItem("Color Scheme 1");
		sherbetMenuItem.addActionListener((ActionEvent e) -> {
			Painter.setColorOffset(Painter.ColorScheme.SHERBET);	
		});
		sherbetMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1,
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		appearanceMenu.add(sherbetMenuItem);
		
		JMenuItem inverseSherbetMenuItem = new JMenuItem("Color Scheme 2");
		inverseSherbetMenuItem.addActionListener((ActionEvent e) -> {
			Painter.setColorOffset(Painter.ColorScheme.INVERSE_SHERBET);	
		});
		inverseSherbetMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, 
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		appearanceMenu.add(inverseSherbetMenuItem);
		
		boldMenuItem = new JMenuItem("Toggle Bold");
		boldMenuItem.addActionListener((ActionEvent e) -> {
			toggleBold();
		});
		boldMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, 
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		appearanceMenu.add(boldMenuItem);
		
		menuBar.add(appearanceMenu);
		
		//////////////
		// View menu
		//////////////
		JMenu viewMenu = new JMenu("View");
		
		playPauseMenuItem = new JMenuItem("Pause");
		playPauseMenuItem.addActionListener((ActionEvent e) -> {
			togglePlayPause();
		});
		playPauseMenuItem.setAccelerator(KeyStroke.getKeyStroke(' '));
		viewMenu.add(playPauseMenuItem);
		
		JMenuItem clearMenuItem = new JMenuItem("Refresh Screen");
		clearMenuItem.addActionListener((ActionEvent e) -> {
			resetGame();
		});
		clearMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, 
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		viewMenu.add(clearMenuItem);
		
		// TODO zoom in/out menu item
		
		menuBar.add(viewMenu);
	}
	private void initialize() {
		game = new ChaosGame(frame.getWidth(), frame.getHeight());
		functionSet = new Function[]{
				new Function(new double[] { 0.5f, 0f, 0f, 0f, 0.5f, 0f }, 1),
				new Function(new double[] { 0.5f, 0f, 0.5f, 0f, 0.5f, 0 }, 1),
				new Function(new double[] { 0.5f, 0f, 0f, 0f, 0.5f, 0.5f }, 1)
				};
		for (Function func : functionSet) {
			func.setBlend(0, 1.0);
//			func.setBlend(1, 0.25);
//			func.setBlend(2, 0.25);
			func.setBlend(3, 1.0);
//			func.setBlend(4, 0.25);
//			func.setBlend(5, 1.0);
//			func.setBlend(6, 1.0);
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
	private static boolean testEquality(Point a, Point b) {
		return a.x == b.x && a.y == b.y;
	}
	private void iterateChaos(int iterations) {
		Point scroll = mouseHandler.getOrigin();
		double zoom = mouseHandler.getZoom();
		boolean clearedScreen = false;
		if (zoom != previousZoom ||
			!testEquality(scroll, previousScroll) ||
			game.getWidth() != frame.getWidth() ||
			game.getHeight() != frame.getHeight()) {
			resetGame();
			clearedScreen = true;
		}
		previousZoom = zoom;
		previousScroll = new Point(scroll.x, scroll.y);
		if (clearedScreen) iterations *= 10;
		game.iterate(functionSet, iterations, previousScroll, zoom);
	}
	private void updateImage() {
		currentImage = Painter.getImage(game);
	}
	public void paintComponent(Graphics g) {
		if (!gamePaused) {
			iterateChaos(lowIterations);
		}
		updateImage();
		g.drawImage(currentImage, 0, 0, null);
		if (gamePaused) {
			g.setColor(Color.white);
			g.drawString("Paused", 15, 15);
		}
	}
}
