/* File: Main.java
 * Authors: Marc Sun Bog & Simon Amtoft Pedersen
 *
 * The following file is the main file for the RISCV-Simulator of the RV32I instructions.
 */
package RISCVSimulator;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) throws IOException {
        launch(args);
    }

    /**
     * This method starts the GUI
     * @Override method in Application
     */
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("Layout.fxml"));
        primaryStage.setTitle("RV32I Simulator");
        primaryStage.setScene(new Scene(root, 800, 500));
        primaryStage.show();
    }
}
