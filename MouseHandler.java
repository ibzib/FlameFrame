import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputAdapter;
import java.awt.Point;

public class MouseHandler extends MouseInputAdapter {
	private boolean isEnabled = true;
	private Point mousePt;
	private Point origin = new Point(0, 0);
	public void setEnabled(boolean status) {
		isEnabled = status;
	}
	public Point getOrigin() {
		return origin;
	}
	public void mouseDragged(MouseEvent e) {
		int dx = e.getX() - mousePt.x;
		int dy = e.getY() - mousePt.y;
		if (isEnabled) {
			origin.setLocation(origin.x + dx, origin.y + dy);
		}
		mousePt = e.getPoint();
	}
	public void mousePressed(MouseEvent e) {
		mousePt = e.getPoint();
	}
}
