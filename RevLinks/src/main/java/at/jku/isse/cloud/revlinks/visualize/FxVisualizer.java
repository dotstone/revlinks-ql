package at.jku.isse.cloud.revlinks.visualize;

import at.jku.isse.cloud.artifact.DSConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class FxVisualizer extends Application {
	
    public static void main(String[] args) {
        launch(args);
    }
    
	@Override
    public void start(Stage primaryStage) {
		DSConnection conn = new DSConnection("dos", "mepwd", "my workspace", "some other package");
		
		LinkQuery linkQuery = new LinkQuery(conn);
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("JFxVisualizer.fxml"));
            Parent root = (Parent)fxmlLoader.load();
            FxController controller = fxmlLoader.<FxController>getController();
            controller.setLinkVisualize(linkQuery);       
            
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Link Visualizer");
            primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
            primaryStage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}