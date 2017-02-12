package at.jku.isse.cloud.revlinks.visualize;

import at.jku.isse.cloud.artifact.DSConnection;
import at.jku.sea.cloud.Project;
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
		DSConnection conn = new DSConnection("dos", "mepwd", "my workspace");
		Project project = conn.getProjects().iterator().next();
		
		LinkQuery linkQuery = new LinkQuery(conn, project);
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("JFxVisualizer.fxml"));
            Parent root = (Parent)fxmlLoader.load();
            FxController controller = fxmlLoader.<FxController>getController();
            controller.initInterface(linkQuery, conn);       
            
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