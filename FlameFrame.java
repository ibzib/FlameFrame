import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JFrame;

public class FlameFrame extends JComponent implements MouseWheelListener {
	private static final long serialVersionUID = 1L;
	MouseHandler mouseHandler = new MouseHandler();
	KeyHandler keyHandler = new KeyHandler();
	private JFrame jFrame = new JFrame("FlameFrame");
	private int targetFPS = 15;
	private boolean isPaused = false;
	private double zoom = 0.22;
	private ChaosGame game;
	private BufferedImage currentImage;
	private int lowIterations = 1000;
	private int highIterations = 10000000;
	private Function[] functionSet;
	private double previousZoom = zoom;
	private java.awt.Point previousOrigin = new java.awt.Point(0, 0);
	public static void main(String[] args) {
//      Painter.colorTest();
		FlameFrame app = new FlameFrame();
		System.out.println("Application exited.");
	}
	private void playPause() {
		isPaused = !isPaused;
		if (isPaused) {
			mouseHandler.setEnabled(false);
			System.out.println("User paused simulation");				
		} else {
			mouseHandler.setEnabled(true);
			System.out.println("User unpaused simulation");
		}
	}
	private void saveImage() {
		try {
			// TODO let user input filename + number of iterations
			String imageTitle = "Test";
			System.out.println("Saving image...");
			String imageFileName = Painter.saveImage(currentImage, imageTitle);
			Function.record(functionSet, imageFileName);
		} catch (IOException ex) {
			// TODO report error to user in dialog box
			ex.printStackTrace();
		}
	}
	private void resetGame() {
//		System.out.println("Clearing points");
		game.resize(jFrame.getWidth(), jFrame.getHeight());
	}
	public FlameFrame() {
		setDoubleBuffered(true);
		setUpFrame();
		initialize();
		try {
			renderLoop();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	private void setUpFrame() {
//		frame.pack();
		jFrame.getContentPane().add(this);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		jFrame.setVisible(true);
		jFrame.addKeyListener(keyHandler);
		jFrame.addMouseListener(mouseHandler);
		jFrame.addMouseMotionListener(mouseHandler);
		jFrame.addMouseWheelListener(this);
	}
	private void initialize() {
		game = new ChaosGame(jFrame.getWidth(), jFrame.getHeight());
		functionSet = new Function[]{
				new Function(new double[] { 0.5f, 0f, 0f, 0f, 0.5f, 0f }, 1),
				new Function(new double[] { 0.5f, 0f, 0.5f, 0f, 0.5f, 0 }, 1),
				new Function(new double[] { 0.5f, 0f, 0f, 0f, 0.5f, 0.5f }, 1)
				};
		for (Function func : functionSet) {
			func.setBlend(0, 1.0);
//			func.setBlend(1, 0.25);
//			func.setBlend(2, 0.25);
			func.setBlend(3, 0.25);
			func.setBlend(4, 1.0);
		}
	}
	public void renderLoop() throws InterruptedException {
		while (true) {
			long startTime = System.nanoTime();
			this.repaint();
			long timeElapsedMs = (System.nanoTime() - startTime) / 1000;
			long sleepTime = 1000 / targetFPS - timeElapsedMs;
			if (sleepTime > 0) {
				Thread.sleep(sleepTime);
			}
		}
	}
	private static boolean testEquality(java.awt.Point a, java.awt.Point b) {
		return a.x == b.x && a.y == b.y;
	}
	private void iterateChaos(int iterations) {
		java.awt.Point absOrigin = mouseHandler.getOrigin();
		if (zoom != previousZoom ||
			!testEquality(absOrigin, previousOrigin) ||
			game.getWidth() != jFrame.getWidth() ||
			game.getHeight() != jFrame.getHeight()) {
			resetGame();
		}
		previousZoom = zoom;
		previousOrigin = new java.awt.Point(absOrigin.x, absOrigin.y);
		// scale origin by frame dimensions
		Point scaledOrigin = new Point((double)absOrigin.x / jFrame.getWidth(), (double)absOrigin.y / jFrame.getHeight());
		game.run(functionSet, iterations, scaledOrigin, zoom);
	}
	private void updateImage() {
//		System.out.println("Updating image");
		currentImage = Painter.getImage(game);
	}
	public void paintComponent(Graphics g) {
		pollKeys();
		if (!isPaused) {
			iterateChaos(lowIterations);
		}
		updateImage();
		g.drawImage(currentImage, 0, 0, null);
		if (isPaused) {
			g.setColor(Color.white);
			g.drawString("Paused", 15, 15);
		}
	}
	private void pollKeys() {
		if (keyHandler.wasAnyKeyReleased()) {
			// color controls
			if (keyHandler.wasKeyReleased('1')) {
				Painter.setColorOffset(Painter.ColorScheme.BLACK_AND_WHITE);
			} else if (keyHandler.wasKeyReleased('2')) {
				Painter.setColorOffset(Painter.ColorScheme.SHERBET);
			} else if (keyHandler.wasKeyReleased('3')) {
				Painter.setColorOffset(Painter.ColorScheme.INVERSE_SHERBET);
			}
			// other commands
			if (keyHandler.wasKeyReleased(' ')) {
				playPause();
			} else if (keyHandler.wasKeyReleased('\n')) {
				System.out.println(game.toString());
				iterateChaos(highIterations);
				System.out.println(game.toString());
				updateImage();
				saveImage();
			} else if (keyHandler.wasKeyReleased('\b')) {
				resetGame();
			} else if (keyHandler.wasKeyReleased('q')) {
				Painter.toggleBold();
			}
			keyHandler.clear();
		}
	}
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		zoom += 0.01 * e.getWheelRotation();
	}
}
