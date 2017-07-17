import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

public class GUI extends JComponent {
	private static final long serialVersionUID = 1L;
	MouseHandler mouseHandler;
	private JFrame frame;
	private int targetFPS = 15;
	private boolean isPaused;
	private Image currentImage;
	private int lowIterations = 5000;
	private int highIterations = 10000000;
	private AbstractAction playPause = new AbstractAction() {
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
	private AbstractAction runHighIterations = new AbstractAction() {
		private static final long serialVersionUID = 1L;
		@Override
		public void actionPerformed(ActionEvent e) {
			if (isPaused) {
				System.out.format("Running %d iterations...\n", highIterations);
				updateImage(highIterations);
			}
		}
	};
	public GUI() {
		setDoubleBuffered(true);
		frame = new JFrame("FlameFrame");
//		frame.pack();
		frame.getContentPane().add(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);
		mouseHandler = new MouseHandler();
		frame.addMouseListener(mouseHandler);
		frame.addMouseMotionListener(mouseHandler);
//		Painter.setOffset(new double[]{1.0/3, 2.0/3, 0.0});
		isPaused = false;
		getInputMap().put(KeyStroke.getKeyStroke("SPACE"), "playPause");
		getActionMap().put("playPause", playPause);
		getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "runHighIterations");
		getActionMap().put("runHighIterations", runHighIterations);
		try {
			renderLoop();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
//		try {
//			
//			String imageName = String.format("shifted");
//			imageName = Painter.paint(plot, imageName);
//			// Function.record(spgasket, imageName);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	private void updateImage(int iterations) {
		double[] blend = new double[Function.variationsCount()];
		blend[0] = 1;
		blend[3] = 1;
		Function[] spgasket = { new Function(blend, new double[] { 0.5f, 0f, 0f, 0f, 0.5f, 0f }, 1),
				new Function(blend, new double[] { 0.5f, 0f, 0.5f, 0f, 0.5f, 0 }, 1),
				new Function(blend, new double[] { 0.5f, 0f, 0f, 0f, 0.5f, 0.5f }, 1) };
		int width = frame.getWidth();
		int height = frame.getHeight();
		double zoom = 0.22;
		java.awt.Point p = mouseHandler.getOrigin();
		Point origin = new Point((double)p.x / frame.getWidth(), (double)p.y / frame.getHeight());
//		System.out.format("(%f, %f)\n", origin.x, origin.y);
		int[][] plot = ChaosGame.run(spgasket, iterations, origin, zoom, width, height);
		currentImage = Painter.getImage(plot);
	}
	public void paintComponent(Graphics g) {
		if (!isPaused) {
			updateImage(lowIterations);
		}
		g.drawImage(currentImage, 0, 0, null);
		g.setColor(Color.white);
	}
}
