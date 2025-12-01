package client.c;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientMain extends Application {

    /**
     * The main method to launch the JavaFX application.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load the FXML file using the class loader
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getClassLoader().getResource("server/chat/demo/client/client.fxml"));
        Parent root = loader.load();

        // Create the scene with the loaded FXML content
        Scene scene = new Scene(root, 620, 520);
        
        // Load and apply the CSS styles
        String css = getClass().getClassLoader().getResource("server/chat/demo/client/client.css").toExternalForm();
        scene.getStylesheets().add(css);

        primaryStage.setTitle("Chat Client");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            ClientController controller = loader.getController();
            controller.shutdown();
            System.exit(0);
        });
        primaryStage.show();
    }
}
