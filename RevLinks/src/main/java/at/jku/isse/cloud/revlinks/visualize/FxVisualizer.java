package at.jku.isse.cloud.revlinks.visualize;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;

import at.jku.isse.cloud.artifact.DSConnection;
import at.jku.sea.cloud.Project;
import at.jku.sea.cloud.exceptions.ToolDoesNotExistException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class FxVisualizer extends Application {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FxVisualizer.class);
	
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
		DSConnection conn = createConnection();
		if(conn == null) {
			Platform.exit();
			return;
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
	
	private DSConnection createConnection() {
		if(arguments != null && arguments.length > 0) {
			if(arguments.length == 4) {
				try {
					LOGGER.info("Using provided credentials: user=" + arguments[0] + ",tool id=" + arguments[2] + ",workspace=" + arguments[3]);
					try {
						int toolId = Integer.parseInt(arguments[2]);
						return new DSConnection(arguments[0], arguments[1], toolId, arguments[3]);
					} catch(NumberFormatException e) {
						LOGGER.error("Tool artifact ID is not a number: {}. Expected arguments: <username> <password> <tool artifact id> <workspace>");
					}
				} catch(ResourceAccessException e) {
					LOGGER.error("Failed to connect to DesignSpace Server!", e);
				} catch(ToolDoesNotExistException e) {
					LOGGER.error("Specified tool with id = {} does not exist!", arguments[2], e);
				}
			} else {
				LOGGER.error("Too {} arguments. Expected arguments: <username> <password> <tool artifact id> <workspace>", 
						(arguments.length < 4 ? "few" : "many"));
			}
		} else {
			LOGGER.info("No arguments passed. Using default credentials: user=" + DEFAULT_USER + ",workspace=" + DEFAULT_WORKSPACE);
			try {
				return new DSConnection(DEFAULT_USER, DEFAULT_PASSWORD, DEFAULT_WORKSPACE);
			} catch(ResourceAccessException e) {
				LOGGER.error("Failed to connect to DesignSpace Server!", e);
			}
		}
		return null;
	}
}