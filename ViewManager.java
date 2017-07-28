import java.awt.Container;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import javax.swing.event.MouseInputAdapter;

public class ViewManager extends MouseInputAdapter implements MouseWheelListener, MouseMotionListener {
    private Container container;
    private boolean isEnabled = true;
    private double zoom = 100;
    private Position mousePress;
    private Position mousePointer;
    private boolean isMouseInside = true;
    private Position scroll = new Position(0, 0);
    private double rotationAmount = Math.PI / 10.0;
    private double rotation = 0.0;

    public void setParent(Container parent) {
        container = parent;
    }

    public void setEnabled(boolean status) {
        isEnabled = status;
    }

    public Position getScroll() {
        return scroll;
    }

    public double getZoom() {
        return zoom;
    }

    public double getRotation() {
        return rotation;
    }

    private Position getCenter() {
        return new Position(container.getWidth() / 2.0, container.getHeight() / 2.0);
    }

    public void rotate(int turns) {
        Position center = isMouseInside ? mousePointer : getCenter();
        center = center.getRotation(-rotation);
        center = center.getSum(scroll);
        double newRotation = rotation + rotationAmount * turns;
        scroll = scroll.getScale(-1);
        scroll = scroll.getSum(center);
        scroll = scroll.getRotation(rotation - newRotation);
        scroll = scroll.getScale(-1);
        scroll = scroll.getSum(center);
        rotation = newRotation;
    }

    public void mouseDragged(MouseEvent e) {
        Position delta = new Position(e.getX() - mousePress.x, e.getY() - mousePress.y);
        delta = delta.getRotation(-rotation);
        if (isEnabled) {
            scroll = new Position(scroll.x - delta.x, scroll.y - delta.y);
        }
        mousePress = new Position(e.getX(), e.getY());
    }

    public void mousePressed(MouseEvent e) {
        mousePress = new Position(e.getX(), e.getY());
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (isEnabled) {
            double zoomFactor = e.getWheelRotation() > 0 ? 1.1 : 1.0 / 1.1;
            setZoom(zoomFactor);
        }
    }

    public void setZoom(double zoomFactor) {
        double newZoom = zoomFactor * zoom;
        Position position = isMouseInside ? mousePointer : getCenter();
        position = position.getRotation(-rotation);
        Position cart = new Position(scroll.x + position.x, scroll.y + position.y);
        cart = cart.getScale(1 / zoom);
        scroll = new Position(cart.x * (newZoom - zoom) + scroll.x, cart.y * (newZoom - zoom) + scroll.y);
        zoom = newZoom;
    }

    public void mouseMoved(MouseEvent e) {
        mousePointer = new Position(e.getX(), e.getY());
    }

    public void mouseEntered(MouseEvent e) {
        isMouseInside = true;
        mousePointer = new Position(e.getX(), e.getY());
    }

    public void mouseExited(MouseEvent e) {
        isMouseInside = false;
    }
}
