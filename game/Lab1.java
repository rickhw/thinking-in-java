import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Lab1 extends JPanel implements MouseListener, MouseMotionListener {
    private int circleX = 100;
    private int circleY = 100;
    private int circleRadius = 50;
    private int mouseX, mouseY;
    private boolean dragging = false;

    public Lab1() {
        setPreferredSize(new Dimension(400, 300));
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.BLUE);
        g2d.fillOval(circleX - circleRadius, circleY - circleRadius, circleRadius * 2, circleRadius * 2);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        if (Math.pow(mouseX - circleX, 2) + Math.pow(mouseY - circleY, 2) <= Math.pow(circleRadius, 2)) {
            dragging = true;
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (dragging) {
            int deltaX = e.getX() - mouseX;
            int deltaY = e.getY() - mouseY;
            circleX += deltaX;
            circleY += deltaY;
            mouseX = e.getX();
            mouseY = e.getY();
            repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        dragging = false;
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Drag and Drop Circle");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(new Lab1());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
