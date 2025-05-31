/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package carta_navegacion;
//Hola caracola
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 *
 * @author jose
 */
public class CartaNavegacionApp extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDocument.fxml"));
        Parent root = loader.load();
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/resources/logo.png")));
        Scene scene = new Scene(root);
        stage.setTitle("Proyecto IPC");
        stage.setResizable(false);
        stage.setScene(scene);
        
        FXMLDocumentController controller = loader.getController();
        stage.setOnCloseRequest(event -> {
            if (controller.sesionIniciada.get()) {
                if (controller.currentUser != null) {
                    // Guardar sesión en BD
                    controller.currentUser.addSession(controller.hits.get(), controller.faults.get());
                    // Reiniciar contadores
                    controller.hits.set(0);
                    controller.faults.set(0);
                }
                // Logout interno
                controller.currentUser = null;
                controller.sesionIniciada.set(false);
            }
        });

        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
