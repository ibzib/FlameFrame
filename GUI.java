import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.locks.Lock;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

public class GUI extends JComponent implements MouseWheelListener {
	private static final long serialVersionUID = 1L;
	MouseHandler mouseHandler;
	private JFrame jFrame;
	private int targetFPS = 15;
	private boolean isPaused = false;
	private boolean isBlackAndWhite = false;
	private double zoom = 0.22;
	private ChaosGame game;
	private BufferedImage currentImage;
	private int lowIterations = 1000;
	private int highIterations = 100000000;
	private Function[] functionSet;
	private java.awt.Point previousOrigin = new java.awt.Point(0, 0);
	private AbstractAction playPauseAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			isPaused = !isPaused;
			if (isPaused) {
				mouseHandler.setEnabled(false);
				System.out.println("User paused simulation");				
			} else {
				mouseHandler.setEnabled(true);
				System.out.println("User unpaused simulation");
			}			
		}
	};
	private AbstractAction saveImageAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			iterateChaos(highIterations);
			updateImage();
			saveImage();
		}
	};
	private void saveImage() {
		try {
			// TODO let user input filename + number of iterations
			System.out.println("Saving image...");
			Painter.saveImage(currentImage, "STest");
		} catch (IOException ex) {
			// TODO report error to user in dialog box
			ex.printStackTrace();
		}
	}
//	private AbstractAction runHighIterationsAction = new AbstractAction() {
//		private static final long serialVersionUID = 1L;
//		@Override
//		public void actionPerformed(ActionEvent e) {
//			if (isPaused) {
//				System.out.format("Running %d iterations...\n", highIterations);
//				updateImage(highIterations);
//				System.out.format("Finished running %d iterations.\n", highIterations);
//			}
//		}
//	};
	private void resetGame() {
//		System.out.println("Clearing points");
		game.resize(jFrame.getWidth(), jFrame.getHeight());
	}
	private AbstractAction clearPointsAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println("User cleared screen");
			resetGame();
		}
	};
	private AbstractAction setBlackAndWhiteAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			isBlackAndWhite = true;
		}
	};
	private AbstractAction firstColorAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			Painter.setColorOffset(Painter.ColorScheme.SHERBET);
		}
	};
	private AbstractAction secondColorAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			Painter.setColorOffset(Painter.ColorScheme.TWO);
		}
	};
	private AbstractAction thirdColorAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			Painter.setColorOffset(Painter.ColorScheme.THREE);
		}
	};
	private AbstractAction fourthColorAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			Painter.setColorOffset(Painter.ColorScheme.FOUR);
		}
	};
	private AbstractAction fifthColorAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			Painter.setColorOffset(Painter.ColorScheme.FIVE);
		}
	};
	private AbstractAction sixthColorAction = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			Painter.setColorOffset(Painter.ColorScheme.SIX);
		}
	};
	public GUI() {
		setDoubleBuffered(true);
		setUpFrame();
//		Painter.setOffset(new double[]{1.0/3, 2.0/3, 0.0});
		setInputs();
		initialize();
		try {
			renderLoop();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private void setUpFrame() {
		jFrame = new JFrame("FlameFrame");
//		frame.pack();
		jFrame.getContentPane().add(this);
		jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		jFrame.setVisible(true);
		mouseHandler = new MouseHandler();
		jFrame.addMouseListener(mouseHandler);
		jFrame.addMouseMotionListener(mouseHandler);
		jFrame.addMouseWheelListener(this);
	}
	private void setInputs() {
		getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "playPauseAction");
		getActionMap().put("playPauseAction", playPauseAction);
		getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "saveImageAction");
		getActionMap().put("saveImageAction", saveImageAction);
		getInputMap().put(KeyStroke.getKeyStroke("BACK_SPACE"), "clearPointsAction");
		getInputMap().put(KeyStroke.getKeyStroke("DELETE"), "clearPointsAction");
		getActionMap().put("clearPointsAction", clearPointsAction);
		getInputMap().put(KeyStroke.getKeyStroke("1"), "firstColorAction");
		getActionMap().put("firstColorAction", firstColorAction);
		getInputMap().put(KeyStroke.getKeyStroke("2"), "secondColorAction");
		getActionMap().put("secondColorAction", secondColorAction);
		getInputMap().put(KeyStroke.getKeyStroke("3"), "thirdColorAction");
		getActionMap().put("thirdColorAction", thirdColorAction);
		getInputMap().put(KeyStroke.getKeyStroke("4"), "fourthColorAction");
		getActionMap().put("fourthColorAction", fourthColorAction);
		getInputMap().put(KeyStroke.getKeyStroke("5"), "fifthColorAction");
		getActionMap().put("fifthColorAction", fifthColorAction);
		getInputMap().put(KeyStroke.getKeyStroke("6"), "sixthColorAction");
		getActionMap().put("sixthColorAction", sixthColorAction);
		getInputMap().put(KeyStroke.getKeyStroke("7"), "seventhColorAction");
		getActionMap().put("seventhColorAction", setBlackAndWhiteAction);
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
			func.setBlend(3, 1.0);
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
		if (iterations > lowIterations) {
			System.out.format("%d + %d iterations...\n", game.iterationsRun, iterations);
		}
		java.awt.Point absOrigin = mouseHandler.getOrigin();
		if (!testEquality(absOrigin, previousOrigin) ||
				game.getWidth() != jFrame.getWidth() ||
				game.getHeight() != jFrame.getHeight()) {
			resetGame();
		}
		previousOrigin = new java.awt.Point(absOrigin.x, absOrigin.y);
		// scale origin by frame dimensions
		Point scaledOrigin = new Point((double)absOrigin.x / jFrame.getWidth(), (double)absOrigin.y / jFrame.getHeight());
		game.run(functionSet, iterations, scaledOrigin, zoom);
		if (iterations > lowIterations) {
			System.out.format("reached %d iterations.\n", game.iterationsRun);
		}
	}
	private void updateImage() {
//		System.out.println("Updating image");
		currentImage = Painter.getImage(game, isBlackAndWhite);
	}
	public void paintComponent(Graphics g) {
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
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		int notches = e.getWheelRotation();
		zoom += 0.01 * notches;
		resetGame();
	}
}
