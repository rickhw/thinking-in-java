import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class DragAndDropCircle extends Application {
j
    private double orgSceneX, orgSceneY;
    private double orgTranslateX, orgTranslateY;

    @Override
    public void start(Stage primaryStage) {
        Circle circle = new Circle(50, Color.BLUE);
        circle.setTranslateX(100);
        circle.setTranslateY(100);

        circle.setOnMousePressed(e -> {
            orgSceneX = e.getSceneX();
            orgSceneY = e.getSceneY();
            orgTranslateX = ((Circle) (e.getSource())).getTranslateX();
            orgTranslateY = ((Circle) (e.getSource())).getTranslateY();
        });

        circle.setOnMouseDragged(e -> {
            double offsetX = e.getSceneX() - orgSceneX;
            double offsetY = e.getSceneY() - orgSceneY;
            double newTranslateX = orgTranslateX + offsetX;
            double newTranslateY = orgTranslateY + offsetY;
            ((Circle) (e.getSource())).setTranslateX(newTranslateX);
            ((Circle) (e.getSource())).setTranslateY(newTranslateY);
        });

        Pane pane = new Pane();
        pane.getChildren().add(circle);

        Scene scene = new Scene(pane, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Drag and Drop Circle");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
