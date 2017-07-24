import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.event.MouseInputAdapter;

public class MouseHandler extends MouseInputAdapter implements MouseWheelListener {
	private boolean isEnabled = true;
	private double zoom = 100;
	private double zoomFactor = 1.1;
	private Point mousePt;
	private Point scroll = new Point(0, 0);
	public void setEnabled(boolean status) {
		isEnabled = status;
	}
	public Point getOrigin() {
		return scroll;
	}
	public double getZoom() {
		return zoom;
	}
	public void mouseDragged(MouseEvent e) {
		double dx = e.getX() - mousePt.x;
		double dy = e.getY() - mousePt.y;
		if (isEnabled) {
			scroll = new Point(scroll.x - dx, scroll.y - dy);
		}
		mousePt = new Point(e.getX(), e.getY());
	}
	public void mousePressed(MouseEvent e) {
		mousePt = new Point(e.getX(), e.getY());
	}
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if (isEnabled) {
			double newZoom = e.getWheelRotation() > 0 ? zoomFactor*zoom : zoom/zoomFactor;
			double cartX = (scroll.x + e.getX()) / zoom;
			double cartY = (scroll.y + e.getY()) / zoom;
			double scrollX = cartX*(newZoom-zoom) + scroll.x;
			double scrollY = cartY*(newZoom-zoom) + scroll.y;
			scroll = new Point(scrollX, scrollY);
			zoom = newZoom;
		}
	}
}
