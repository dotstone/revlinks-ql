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
	
	private static final String DEFAULT_USER = "RL User";
	private static final String DEFAULT_PASSWORD = "default password";
	private static final String DEFAULT_WORKSPACE = "RL workspace";

	private static String[] arguments;
	
    public static void main(String[] args) {
    	arguments = args;
        launch(args);
    }
    
	@Override
    public void start(Stage primaryStage) {		
		DSConnection conn;
		if(arguments != null && arguments.length == 4) {
			conn = new DSConnection(arguments[0], arguments[1], Integer.parseInt(arguments[2]), arguments[3]);
		} else {
			conn = new DSConnection(DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_WORKSPACE);
		}
		
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