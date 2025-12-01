package server.s;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/server/chat/demo/server/server.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 620, 520);
        scene.getStylesheets().add(getClass().getResource("/server/chat/demo/client/client.css").toExternalForm());

        primaryStage.setTitle("Chat Server");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            ServerController controller = loader.getController();
            controller.shutdown();
            System.exit(0);
        });
        primaryStage.show();
    }

}
