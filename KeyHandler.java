import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;

public class KeyHandler implements KeyListener {
	private static final int NUMBER_OF_KEYS = 256;
	private boolean isAnyKeyReleased = false;
	private boolean[] isKeyReleased = new boolean[NUMBER_OF_KEYS];
	private boolean isEnabled = true;
	public void setEnabled(boolean status) {
		isEnabled = status;
	}
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
		if (isEnabled && e.getKeyChar() >= 0 && e.getKeyChar() < NUMBER_OF_KEYS) {
//			System.out.format("Key pressed: %d\n", e.getKeyCode());
			isKeyReleased[e.getKeyCode()] = false;
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {
		if (isEnabled && e.getKeyChar() >= 0 && e.getKeyChar() < NUMBER_OF_KEYS) {
//			System.out.format("Key released: %d\n", e.getKeyCode());
			isKeyReleased[e.getKeyChar()] = true;
			isAnyKeyReleased = true;
		}
	}
	public void clear() {
		Arrays.fill(isKeyReleased, false);
		isAnyKeyReleased = false;
	}
}
