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
import javafx.stage.Stage;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main extends Application {

    static Instruction[] programInst;  // Array of all program instructions
    static Memory memory = new Memory(512) ;


    public static void main(String[] args) throws IOException {
        CPU cpu = new CPU();
        guiController gui = new guiController();


        String test = "addlarge";               // Name of test file
        String dir = "tests\\";                 // Directory
        String path = dir+test+".bin";          // Path of binary file
        cpu.reg[2] = 128*1000-4;                    // Initialize sp to last word in memory.
        programInst = cpu.getInstructions(path);



        // gui.replaceRegVal(5,5);

        System.out.println("Basic code");


        while (cpu.pc < programInst.length) {
            cpu.executeInstruction(programInst, memory);
           // gui.replaceRegNode(programInst[cpu.pc-1].rd,""+cpu.reg[cpu.pc-1]);
        }
        outToBin(cpu.reg);
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

    /**
     *  @output results in binary file 'output.bin'
     */
    private static void outToBin(int[] reg) throws IOException {
        DataOutputStream dos = new DataOutputStream(new FileOutputStream("output.bin"));
        for (int val : reg) {
            dos.writeInt(Integer.reverseBytes(val));
        }
        dos.close();
    }

}
