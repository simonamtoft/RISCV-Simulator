/* File: RISCVsimulator.java
 * Authors: Simon Amtoft Pedersen & Marc Sun Bøg
 * The following file simulates the RISC-V instructions set from a binary input file
 */

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.io.*;

public class RISCVsimulator extends Application {


    // Defined here so other methods can manipulate them
    private static Text val1, val2, val3, val4, val5, val6, val7, val8, val9, val10, val11, val12, val13, val14, val15, val16;
    private static Text val17, val18,val19, val20, val21, val22, val23, val24, val25, val26, val27, val28, val29, val30, val31;
    private static GridPane gridReg;
    private static Stage window;
    static processor proc;

    public static void main(String[] args) throws Exception {
        proc = new processor();
        initGridReg();                              // Initialize gui reg values text
        String test = "addlarge";               // Name of test file
        String dir = "tests\\";                 // Directory
        String path = dir+test+".bin";          // Path of binary file
        proc.getInstructions(path);        // Read all instructions from binary file
        proc.reg[2] = 128*1000-4;                    // Initialize sp to last word in memory.
        replaceRegNode(2, ""+proc.reg[2]);

        System.out.println("Machine code \t Basic code");
        while (proc.pc < proc.program.length) {
            proc.printMachineCode();
            proc.executeInstruction();
            replaceRegNode(proc.rd,""+proc.reg[proc.rd]);
        }
        outToBin(proc.reg);
        launch(args);
    }

    // Outputs registers x0 to x31 to file "output.bin"
    private static void outToBin(int[] reg) throws IOException{
        DataOutputStream dos = new DataOutputStream(new FileOutputStream("output.bin"));
        for (int val : reg) {
            dos.writeInt(Integer.reverseBytes(val));
        }
        dos.close();
    }

    // Initialize register GridPane
    private static void initGridReg() {
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

        // Setup register grid
        gridReg = new GridPane();
        gridReg.setPadding(new Insets(10,10,10,10));
        gridReg.setVgap(2);
        gridReg.setHgap(5);
        gridReg.setAlignment(Pos.CENTER);
        gridReg.setMinSize(250,200);

        // Column titles
        Text regHead = new Text("Register");
        Text valHead = new Text("Value");

        // Add Text to GridPane
        gridReg.add(regHead,0,0);
        gridReg.add(valHead,1,0);
        gridReg.add(val1,1,2);
        gridReg.add(val2,1,3);
        gridReg.add(val3,1,4);
        gridReg.add(val4,1,5);
        gridReg.add(val5,1,6);
        gridReg.add(val6,1,7);
        gridReg.add(val7,1,8);
        gridReg.add(val8,1,9);
        gridReg.add(val9,1,10);
        gridReg.add(val10,1,11);
        gridReg.add(val11,1,12);
        gridReg.add(val12,1,13);
        gridReg.add(val13,1,14);
        gridReg.add(val14,1,15);
        gridReg.add(val15,1,16);
        gridReg.add(val16,1,17);
        gridReg.add(val17,1,18);
        gridReg.add(val18,1,19);
        gridReg.add(val19,1,20);
        gridReg.add(val20,1,21);
        gridReg.add(val21,1,22);
        gridReg.add(val22,1,23);
        gridReg.add(val23,1,24);
        gridReg.add(val24,1,25);
        gridReg.add(val25,1,26);
        gridReg.add(val26,1,27);
        gridReg.add(val27,1,28);
        gridReg.add(val28,1,29);
        gridReg.add(val29,1,30);
        gridReg.add(val30,1,31);
        gridReg.add(val31,1,32);
        gridReg.add(new Text("0"),1,1); // x0 doesn't change

        // Write x0 to x31 in the Register column
        for (int i = 0; i < 32; i++) {
            gridReg.add(new Text("x"+i),0,i+1);
        }
    }
    
    // Replace node in register GridPane
    private static void replaceRegNode(int index, String text) {
        switch(index) {
            case 1:
                gridReg.getChildren().remove(val1);
                val1.setText(text);
                gridReg.add(val1,1,2);
                break;
            case 2:
                gridReg.getChildren().remove(val2);
                val2.setText(text);
                gridReg.add(val2,1,3);
                break;
            case 3:
                gridReg.getChildren().remove(val3);
                val3.setText(text);
                gridReg.add(val3,1,4);
                break;
            case 4:
                gridReg.getChildren().remove(val4);
                val4.setText(text);
                gridReg.add(val4,1,5);
                break;
            case 5:
                gridReg.getChildren().remove(val5);
                val5.setText(text);
                gridReg.add(val5,1,6);
                break;
            case 6:
                gridReg.getChildren().remove(val6);
                val6.setText(text);
                gridReg.add(val6,1,7);
                break;
            case 7:
                gridReg.getChildren().remove(val7);
                val7.setText(text);
                gridReg.add(val7,1,8);
                break;
            case 8:
                gridReg.getChildren().remove(val8);
                val8.setText(text);
                gridReg.add(val8,1,9);
                break;
            case 9:
                gridReg.getChildren().remove(val9);
                val9.setText(text);
                gridReg.add(val9,1,10);
                break;
            case 10:
                gridReg.getChildren().remove(val10);
                val10.setText(text);
                gridReg.add(val10,1,11);
                break;
            case 11:
                gridReg.getChildren().remove(val11);
                val11.setText(text);
                gridReg.add(val11,1,12);
                break;
            case 12:
                gridReg.getChildren().remove(val12);
                val12.setText(text);
                gridReg.add(val12,1,13);
                break;
            case 13:
                gridReg.getChildren().remove(val13);
                val13.setText(text);
                gridReg.add(val13,1,14);
                break;
            case 14:
                gridReg.getChildren().remove(val14);
                val14.setText(text);
                gridReg.add(val14,1,15);
                break;
            case 15:
                gridReg.getChildren().remove(val15);
                val15.setText(text);
                gridReg.add(val15,1,16);
                break;
            case 16:
                gridReg.getChildren().remove(val16);
                val16.setText(text);
                gridReg.add(val16,1,17);
                break;
            case 17:
                gridReg.getChildren().remove(val17);
                val17.setText(text);
                gridReg.add(val17,1,18);
                break;
            case 18:
                gridReg.getChildren().remove(val18);
                val18.setText(text);
                gridReg.add(val18,1,19);
                break;
            case 19:
                gridReg.getChildren().remove(val19);
                val19.setText(text);
                gridReg.add(val19,1,20);
                break;
            case 20:
                gridReg.getChildren().remove(val20);
                val20.setText(text);
                gridReg.add(val20,1,21);
                break;
            case 21:
                gridReg.getChildren().remove(val21);
                val21.setText(text);
                gridReg.add(val21,1,22);
                break;
            case 22:
                gridReg.getChildren().remove(val22);
                val22.setText(text);
                gridReg.add(val22,1,23);
                break;
            case 23:
                gridReg.getChildren().remove(val23);
                val23.setText(text);
                gridReg.add(val23,1,24);
                break;
            case 24:
                gridReg.getChildren().remove(val24);
                val24.setText(text);
                gridReg.add(val24,1,25);
                break;
            case 25:
                gridReg.getChildren().remove(val5);
                val25.setText(text);
                gridReg.add(val25,1,26);
                break;
            case 26:
                gridReg.getChildren().remove(val26);
                val26.setText(text);
                gridReg.add(val26,1,27);
                break;
            case 27:
                gridReg.getChildren().remove(val27);
                val27.setText(text);
                gridReg.add(val27,1,28);
                break;
            case 28:
                gridReg.getChildren().remove(val28);
                val28.setText(text);
                gridReg.add(val28,1,29);
                break;
            case 29:
                gridReg.getChildren().remove(val29);
                val29.setText(text);
                gridReg.add(val29,1,30);
                break;
            case 30:
                gridReg.getChildren().remove(val30);
                val30.setText(text);
                gridReg.add(val30,1,31);
                break;
            case 31:
                gridReg.getChildren().remove(val31);
                val31.setText(text);
                gridReg.add(val31,1,32);
                break;
            default:
                break;
        }
    }
    
    // Start GUI application  
    public void start(Stage stage) throws Exception {
        window = new Stage();

        // Setup scene
        Scene scene = new Scene(gridReg);
        window.setTitle("RISC-V simulator");
        window.setScene(scene);
        window.show();
    }
}
