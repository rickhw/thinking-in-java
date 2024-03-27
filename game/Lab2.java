import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Lab2 extends JPanel implements MouseListener, MouseMotionListener {
    private Circle circle1;
    private Circle circle2;
    private int mouseX, mouseY;
    private Circle selectedCircle = null;

    public Lab2() {
        setPreferredSize(new Dimension(400, 300));
        addMouseListener(this);
        addMouseMotionListener(this);

        circle1 = new Circle(100, 100, 50);
        circle2 = new Circle(250, 150, 50);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        circle1.draw(g2d);
        circle2.draw(g2d);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        if (circle1.contains(mouseX, mouseY)) {
            selectedCircle = circle1;
        } else if (circle2.contains(mouseX, mouseY)) {
            selectedCircle = circle2;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (selectedCircle != null) {
            selectedCircle.moveBy(e.getX() - mouseX, e.getY() - mouseY);
            mouseX = e.getX();
            mouseY = e.getY();
            repaint();
            checkCollision();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        selectedCircle = null;
    }

    // Unused MouseListener and MouseMotionListener methods
    @Override
    public void mouseClicked(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseMoved(MouseEvent e) {}

    private void checkCollision() {
        if (circle1.intersects(circle2)) {
            // Handle collision by reversing the direction of movement for both circles
            circle1.reverseDirection();
            circle2.reverseDirection();

            System.out.println("collision!!!");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Drag and Drop Circles");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new Lab2());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    private class Circle {
        private int x, y, radius;
        private int dx = 1; // Movement direction along x-axis
        private int dy = 1; // Movement direction along y-axis

        public Circle(int x, int y, int radius) {
            this.x = x;
            this.y = y;
            this.radius = radius;
        }

        public void draw(Graphics2D g2d) {
            g2d.setColor(Color.BLUE);
            g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        }

        public boolean contains(int px, int py) {
            int dx = x - px;
            int dy = y - py;
            return dx * dx + dy * dy <= radius * radius;
        }

        public void moveBy(int dx, int dy) {
            x += dx;
            y += dy;
        }

        public boolean intersects(Circle other) {
            int dx = x - other.x;
            int dy = y - other.y;
            int distanceSquared = dx * dx + dy * dy;
            int radiiSquared = (radius + other.radius) * (radius + other.radius);
            return distanceSquared <= radiiSquared;
        }

        public void reverseDirection() {
            dx = -dx;
            dy = -dy;
        }
    }
}
