import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;

public class KeyHandler implements KeyListener {
	private static final int NUMBER_OF_KEYS = 256;
	private boolean isAnyKeyReleased = false;
	private boolean[] isKeyReleased = new boolean[NUMBER_OF_KEYS];
	public boolean wasAnyKeyReleased() {
		return isAnyKeyReleased;
	}
	public boolean wasKeyReleased(int key) {
		return isKeyReleased[key];
	}
	@Override
	public void keyTyped(KeyEvent e) {
		// do nothing
	}
	@Override
	public void keyPressed(KeyEvent e) {
//		System.out.format("Key pressed: %d\n", e.getKeyCode());
		if (e.getKeyChar() >= 0 && e.getKeyChar() < NUMBER_OF_KEYS) {
			isKeyReleased[e.getKeyCode()] = false;
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {
//		System.out.format("Key released: %d\n", e.getKeyCode());
		if (e.getKeyChar() >= 0 && e.getKeyChar() < NUMBER_OF_KEYS) {
			isKeyReleased[e.getKeyChar()] = true;
			isAnyKeyReleased = true;
		}
	}
	public void clear() {
		Arrays.fill(isKeyReleased, false);
		isAnyKeyReleased = false;
	}
}
