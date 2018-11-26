/* File: Main.java
 * Authors: Marc Sun Bøg & Simon Amtoft Pedersen
 *
 * The following file is the main file for the RISCV-Simulator of the RV32I instructions.
 * The file starts the GUI application, and loads Layout.fxml. 
 */
package RISCVSimulator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * This method starts the GUI
     * @Override method in Application
     */
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader loader = new FXMLLoader(getClass().getResource("Layout.fxml"));
        Parent root = loader.load();
        guiController controller = loader.getController();
        controller.setStage(primaryStage);
        primaryStage.setTitle("RV32I Simulator");
        primaryStage.setScene(new Scene(root, 800, 500));
        primaryStage.show();
    }
}
