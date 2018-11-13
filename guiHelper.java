import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class guiHelper extends Application {

    // Defined here so other methods can manipulate them
    private Text val1, val2, val3, val4, val5, val6, val7, val8, val9, val10, val11, val12, val13, val14, val15, val16;
    private Text val17, val18,val19, val20, val21, val22, val23, val24, val25, val26, val27, val28, val29, val30, val31;
    private GridPane gridPane;

    public void start(Stage stage) throws IOException {

        // Setup GridPane
        gridPane = new GridPane();
        gridPane.setPadding(new Insets(10,10,10,10));
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setMinSize(300,200);

        // Column titles
        Text regHead = new Text("Register");
        Text valHead = new Text("Value");

        // Define Text elements for all x0 to x31 values:
        val1 = new Text("0");
        val2 = new Text("0");
        val3 = new Text("0");
        val4 = new Text("0");
        val5 = new Text("0");
        val6 = new Text("0");
        val7 = new Text("0");
        val8 = new Text("0");
        val9 = new Text("0");
        val10 = new Text("0");
        val11 = new Text("0");
        val12 = new Text("0");
        val13 = new Text("0");
        val14 = new Text("0");
        val15 = new Text("0");
        val16 = new Text("0");
        val17 = new Text("0");
        val18 = new Text("0");
        val19 = new Text("0");
        val20 = new Text("0");
        val21 = new Text("0");
        val22 = new Text("0");
        val23 = new Text("0");
        val24 = new Text("0");
        val25 = new Text("0");
        val26 = new Text("0");
        val27 = new Text("0");
        val28 = new Text("0");
        val29 = new Text("0");
        val30 = new Text("0");
        val31 = new Text("0");

        // Add Text to GridPane
        gridPane.add(regHead,0,0);
        gridPane.add(valHead,1,0);
        gridPane.add(val1,1,2);
        gridPane.add(val2,1,3);
        gridPane.add(val3,1,4);
        gridPane.add(val4,1,5);
        gridPane.add(val5,1,6);
        gridPane.add(val6,1,7);
        gridPane.add(val7,1,8);
        gridPane.add(val8,1,9);
        gridPane.add(val9,1,10);
        gridPane.add(val10,1,11);
        gridPane.add(val11,1,12);
        gridPane.add(val12,1,13);
        gridPane.add(val13,1,14);
        gridPane.add(val14,1,15);
        gridPane.add(val15,1,16);
        gridPane.add(val16,1,17);
        gridPane.add(val17,1,18);
        gridPane.add(val18,1,19);
        gridPane.add(val19,1,20);
        gridPane.add(val20,1,21);
        gridPane.add(val21,1,22);
        gridPane.add(val22,1,23);
        gridPane.add(val23,1,24);
        gridPane.add(val24,1,25);
        gridPane.add(val25,1,26);
        gridPane.add(val26,1,27);
        gridPane.add(val27,1,28);
        gridPane.add(val28,1,29);
        gridPane.add(val29,1,30);
        gridPane.add(val30,1,31);
        gridPane.add(val31,1,32);
        gridPane.add(new Text("0"),1,1); // x0 doesn't change

        // Write x0 to x31 in the Register column
        for (int i = 0; i < 32; i++) {
            gridPane.add(new Text("x"+i),0,i+1);
        }

        // Setup scene
        stage.setTitle("RISC-V simulator");
        Scene scene = new Scene(gridPane);
        stage.setScene(scene);
        stage.show();
    }

    public void replaceNode(int index, String text) {
        switch(index) {
            case 1:
                gridPane.getChildren().remove(val1);
                val1 = new Text(text);
                gridPane.add(val1,1,2);
                break;
            case 2:
                gridPane.getChildren().remove(val2);
                val1 = new Text(text);
                gridPane.add(val1,1,3);
                break;
            case 3:
                gridPane.getChildren().remove(val3);
                val1 = new Text(text);
                gridPane.add(val1,1,4);
                break;
            case 4:
                gridPane.getChildren().remove(val4);
                val1 = new Text(text);
                gridPane.add(val1,1,5);
                break;
            case 5:
                gridPane.getChildren().remove(val5);
                val1 = new Text(text);
                gridPane.add(val1,1,6);
                break;
            case 6:
                gridPane.getChildren().remove(val6);
                val1 = new Text(text);
                gridPane.add(val1,1,7);
                break;
            case 7:
                gridPane.getChildren().remove(val7);
                val1 = new Text(text);
                gridPane.add(val1,1,8);
                break;
            case 8:
                gridPane.getChildren().remove(val8);
                val1 = new Text(text);
                gridPane.add(val1,1,9);
                break;
            case 9:
                gridPane.getChildren().remove(val9);
                val1 = new Text(text);
                gridPane.add(val1,1,10);
                break;
            case 10:
                gridPane.getChildren().remove(val10);
                val1 = new Text(text);
                gridPane.add(val1,1,11);
                break;
            case 11:
                gridPane.getChildren().remove(val11);
                val1 = new Text(text);
                gridPane.add(val1,1,12);
                break;
            case 12:
                gridPane.getChildren().remove(val12);
                val1 = new Text(text);
                gridPane.add(val1,1,13);
                break;
            case 13:
                gridPane.getChildren().remove(val13);
                val1 = new Text(text);
                gridPane.add(val1,1,14);
                break;
            case 14:
                gridPane.getChildren().remove(val14);
                val1 = new Text(text);
                gridPane.add(val1,1,15);
                break;
            case 15:
                gridPane.getChildren().remove(val15);
                val1 = new Text(text);
                gridPane.add(val1,1,16);
                break;
            case 16:
                gridPane.getChildren().remove(val16);
                val1 = new Text(text);
                gridPane.add(val1,1,17);
                break;
            case 17:
                gridPane.getChildren().remove(val17);
                val1 = new Text(text);
                gridPane.add(val1,1,18);
                break;
            case 18:
                gridPane.getChildren().remove(val18);
                val1 = new Text(text);
                gridPane.add(val1,1,19);
                break;
            case 19:
                gridPane.getChildren().remove(val19);
                val1 = new Text(text);
                gridPane.add(val1,1,20);
                break;
            case 20:
                gridPane.getChildren().remove(val20);
                val1 = new Text(text);
                gridPane.add(val1,1,21);
                break;
            case 21:
                gridPane.getChildren().remove(val21);
                val1 = new Text(text);
                gridPane.add(val1,1,22);
                break;
            case 22:
                gridPane.getChildren().remove(val22);
                val1 = new Text(text);
                gridPane.add(val1,1,23);
                break;
            case 23:
                gridPane.getChildren().remove(val23);
                val1 = new Text(text);
                gridPane.add(val1,1,24);
                break;
            case 24:
                gridPane.getChildren().remove(val24);
                val1 = new Text(text);
                gridPane.add(val1,1,25);
                break;
            case 25:
                gridPane.getChildren().remove(val5);
                val1 = new Text(text);
                gridPane.add(val1,1,26);
                break;
            case 26:
                gridPane.getChildren().remove(val26);
                val1 = new Text(text);
                gridPane.add(val1,1,27);
                break;
            case 27:
                gridPane.getChildren().remove(val27);
                val1 = new Text(text);
                gridPane.add(val1,1,28);
                break;
            case 28:
                gridPane.getChildren().remove(val28);
                val1 = new Text(text);
                gridPane.add(val1,1,29);
                break;
            case 29:
                gridPane.getChildren().remove(val29);
                val1 = new Text(text);
                gridPane.add(val1,1,30);
                break;
            case 30:
                gridPane.getChildren().remove(val30);
                val1 = new Text(text);
                gridPane.add(val1,1,31);
                break;
            case 31:
                gridPane.getChildren().remove(val31);
                val1 = new Text(text);
                gridPane.add(val1,1,32);
                break;

        }
    }

}
