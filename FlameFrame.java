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
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FlameFrame extends JComponent {
	private static final long serialVersionUID = 2L;
	// UI components
	private final String appName = "FlameFrame";
	private JFrame frame = new JFrame(appName);
	ViewManager viewManager = new ViewManager();
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
	// program state information
	private Function[] functionSet;
	private ChaosGame game;
	private BufferedImage currentImage;
	private int targetFPS = 15;
	private boolean iterationPaused = false;
	private boolean renderingStopped = false;
	private int lowIterations = 1000;
	private int highIterations = 10000000;
	private Position previousScroll = new Position(0, 0);
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
// 			TODO Figure out why BMP images won't save
//			FileNameExtensionFilter bmpFilter = new FileNameExtensionFilter("Windows Bitmap (BMP)", "bmp");
//			chooser.addChoosableFileFilter(bmpFilter);
			chooser.setFileFilter(jpgFilter);
			chooser.addChoosableFileFilter(pngFilter);
			chooser.setAcceptAllFileFilterUsed(false);
			chooser.setMultiSelectionEnabled(false);
			int returnVal = chooser.showSaveDialog(frame);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String inputtedPath = chooser.getSelectedFile().getAbsolutePath();
				String extension = ((FileNameExtensionFilter)(chooser.getFileFilter())).getExtensions()[0];
				String outputImageFileName = ImageManager.saveImage(currentImage, inputtedPath, extension);
				Function.record(functionSet, outputImageFileName);
			}
			play();
		} catch (IOException ex) {
			// TODO report error to user in dialog box
			ex.printStackTrace();
		}
	}
	private void clearScreen() {
		game.resize(frame.getContentPane().getWidth(), frame.getContentPane().getHeight());
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
		setUpFrame();
	}
	private void setUpFrame() {
//		frame.pack();
		frame.getContentPane().add(this);
		frame.setJMenuBar(menuBar);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		viewManager.setParent(frame.getContentPane());
		frame.getContentPane().addMouseListener(viewManager);
		frame.getContentPane().addMouseMotionListener(viewManager);
		frame.getContentPane().addMouseWheelListener(viewManager);
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
				
		showBlackAndWhite = false;
		String bwMessage = showBlackAndWhite ? showColorMessage : showBlackAndWhiteMessage;
		blackAndWhiteMenuItem = new JMenuItem(bwMessage);
		blackAndWhiteMenuItem.addActionListener((ActionEvent e) -> {
			toggleBlackAndWhite();
		});
		blackAndWhiteMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, 
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		appearanceMenu.add(blackAndWhiteMenuItem);
		
		showBold = false;
		String boldMessage = showBold ? unBoldMessage : showBoldMessage;
		boldMenuItem = new JMenuItem(boldMessage);
		boldMenuItem.addActionListener((ActionEvent e) -> {
			toggleBold();
		});
		boldMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, 
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
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
		playPauseMenuItem.setAccelerator(KeyStroke.getKeyStroke(' '));
		iterationMenu.add(playPauseMenuItem);
		
		JMenuItem clearMenuItem = new JMenuItem("Clear Iteration");
		clearMenuItem.addActionListener((ActionEvent e) -> {
			clearScreen();
		});
		clearMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, 
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		iterationMenu.add(clearMenuItem);
		
		JMenuItem addIterationsMenuItem = new JMenuItem(String.format("Run %d Iterations", highIterations));
		addIterationsMenuItem.addActionListener((ActionEvent e) -> {
			// TODO add progress bar
			iterate(highIterations);
		});
		addIterationsMenuItem.setAccelerator(KeyStroke.getKeyStroke('\n'));
		iterationMenu.add(addIterationsMenuItem);
		menuBar.add(iterationMenu);
		
		//////////////
		// View menu
		//////////////
		JMenu viewMenu = new JMenu("View");
		
		JMenuItem zoomInMenuItem = new JMenuItem("Zoom In");
		zoomInMenuItem.addActionListener((ActionEvent e) -> {
			viewManager.setZoom(1.2);
		});
		zoomInMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		viewMenu.add(zoomInMenuItem);
		
		JMenuItem zoomOutMenuItem = new JMenuItem("Zoom Out");
		zoomOutMenuItem.addActionListener((ActionEvent e) -> {
			viewManager.setZoom(1.0 / 1.2);
		});
		zoomOutMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		viewMenu.add(zoomOutMenuItem);
		
		JMenuItem rotateClockwiseMenuItem = new JMenuItem("Rotate Clockwise");
		rotateClockwiseMenuItem.addActionListener((ActionEvent e) -> {
			viewManager.rotate(1);
		});
		rotateClockwiseMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0));
		viewMenu.add(rotateClockwiseMenuItem);

		JMenuItem rotateCounterClockwiseMenuItem = new JMenuItem("Rotate Counterclockwise");
		rotateCounterClockwiseMenuItem.addActionListener((ActionEvent e) -> {
			viewManager.rotate(-1);
		});
		rotateCounterClockwiseMenuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0));
		viewMenu.add(rotateCounterClockwiseMenuItem);

		menuBar.add(viewMenu);
		
	}
	private void initialize() {
		game = new ChaosGame(frame.getContentPane().getWidth(), frame.getContentPane().getHeight());
		functionSet = new Function[]{new Function(Palette.getColor(0+0.2), new double[] { 0.5f, 0f, 0f, 0f, 0.5f, 0f }, 1),
				new Function(Palette.getColor(1.0/3.0+0.2), new double[] { 0.5f, 0f, 0.5f, 0f, 0.5f, 0 }, 1),
				new Function(Palette.getColor(2.0/3.0+0.2), new double[] { 0.5f, 0f, 0f, 0f, 0.5f, 0.5f }, 1)};
		for (Function func : functionSet) {
			func.setBlend(0, 1.0);
//			func.setBlend(1, 0.25);
//			func.setBlend(2, 1.0); // TODO FIX ME: NaN
			func.setBlend(3, 1);
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
	private void iterate(int iterations) {
		Position scroll = viewManager.getScroll();
		if (!scroll.equals(previousScroll) || 
				game.getWidth() != frame.getContentPane().getWidth() || 
				game.getHeight() != frame.getContentPane().getHeight()) {
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
			iterate(lowIterations);
		}
		updateImage();
		g.drawImage(currentImage, 0, 0, null);
		if (iterationPaused) {
			g.setColor(Color.white);
			g.drawString("Paused", 15, 15);
		}
	}
}
