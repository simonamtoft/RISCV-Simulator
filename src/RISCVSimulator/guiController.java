/* File: guiController.java
 * Authors: Marc Sun Bog & Simon Amtoft Pedersen
 *
 * The following file handles the controls associated with the GUI
 */

package RISCVSimulator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URL;
import java.util.ResourceBundle;

public class guiController implements Initializable{
    // UI elements
    public VBox mainVBox;
    public MenuItem menuItemOpen;
    public MenuItem menuItemExit;
    public Button buttonNext;
    public Button buttonPrevious;
    public Button buttonRun;
    public TableColumn pcColumn;
    public TableColumn instructionColumn;
    public TextArea outputArea;

    // Register TableView variables
    public TableView<tableHelper> regTable;
    public TableColumn<tableHelper, String> regNameCol;
    public TableColumn<tableHelper, Integer> regValCol;

    // Program variables
    private static CPU cpu;
    private static Instruction[] program;   // Array of all program instructions
    private static Memory mem;              // Memory

    /**
     * Runs in start of guiController.
     * Uses method initializeRegTable() to initialize regTable's two columns with reg x0 to x31.
     * @Override
     */
    public void initialize(URL location, ResourceBundle resources) {
        // Setup columns of register TableView
        regNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        regValCol.setCellValueFactory(new PropertyValueFactory<>("value"));

        // Load data
        regTable.setItems(initializeRegTable());
    }

    /**
     * Following method handles file picking by user in the menu.
     * @throws IOException
     */
    public void chooseFile() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open binary RISC-V code");
        File file = fileChooser.showOpenDialog(mainVBox.getScene().getWindow());
        if(file != null){
            program = getInstructions(file);
            mem = new Memory(102400);
            cpu = new CPU(mem,program);
            replaceRegVal(2,cpu.reg[2]);
        } else {
            program = null;
            mem = null;
            cpu = null;
        }
    }

    /**
     * Handles action when 'next' button is pressed.
     * If a file has been picked, and program is not done, then it executes the next instruction
     */
    public void nextButton(){
        if (program == null || cpu == null || mem == null) {
            consolePrint("program null");
            return;
        }
        if(cpu.pc >= program.length) {
            consolePrint("program finished");
            return;
        }
        cpu.executeInstruction();
        updateNext();
    }

    public void previousButton() {
    }


    /**
     * Handles action when 'run' button is pressed.
     * If a file has been picked, and program is not done, then it executes the remaining instructions
     */
    public void runButton() {
        if (program == null || cpu == null || mem == null) {
            consolePrint("program null");
            return;
        }

        while (cpu.pc < program.length) {
            cpu.executeInstruction();
            updateNext();
        }
    }

    /**
     * Updates GUI according to next instruction
     */
    private void updateNext() {
        replaceRegVal(cpu.program[cpu.pc].rd,cpu.reg[cpu.program[cpu.pc].rd]);
        replaceRegVal(2,cpu.reg[2]);
        consolePrint(program[cpu.pc].toAssemblyString());
    }

    /**
     * This method will replace a value in the register table
     */
    private void replaceRegVal(int index, int val) {
        regTable.getItems().get(index).setValue(val);
    }

    /**
     * This method simulates a console in the GUI.
     * Outputs the input string on next line
     */
    private void consolePrint(String outPrint) {
        outputArea.setText(outputArea.getText()+outPrint+"\n"); // doesn't work when called in main.
    }

    // Closes program
    public void closeProgram() throws IOException{
        outToBin(cpu.reg);
        System.exit(0);
    }



    // Adds instructions from binary file to program array
    private static Instruction[] getInstructions(File f) throws IOException {
        DataInputStream dis = new DataInputStream(new FileInputStream(f));
        int len = (int) f.length()/4;                       // Number of instructions
        Instruction[] programInst = new Instruction[len];   // Instruction array
        for(int i = 0; i < len; i++){
            programInst[i] = new Instruction(Integer.reverseBytes(dis.readInt()));
        }
        dis.close();
        return programInst;
    }


    /**
     * This method initializes the TableView 'regTable' variable with registers x0 to x31 and value 0
     * @return ObservableList<tableHelper>
     */
    public ObservableList<tableHelper> initializeRegTable() {
        ObservableList<tableHelper> registers = FXCollections.observableArrayList();
        registers.add(new tableHelper("x0",0));
        registers.add(new tableHelper("x1",0));
        registers.add(new tableHelper("x2",0));
        registers.add(new tableHelper("x3",0));
        registers.add(new tableHelper("x4",0));
        registers.add(new tableHelper("x5",0));
        registers.add(new tableHelper("x6",0));
        registers.add(new tableHelper("x7",0));
        registers.add(new tableHelper("x8",0));
        registers.add(new tableHelper("x9",0));
        registers.add(new tableHelper("x10",0));
        registers.add(new tableHelper("x11",0));
        registers.add(new tableHelper("x12",0));
        registers.add(new tableHelper("x13",0));
        registers.add(new tableHelper("x14",0));
        registers.add(new tableHelper("x15",0));
        registers.add(new tableHelper("x16",0));
        registers.add(new tableHelper("x17",0));
        registers.add(new tableHelper("x18",0));
        registers.add(new tableHelper("x19",0));
        registers.add(new tableHelper("x20",0));
        registers.add(new tableHelper("x21",0));
        registers.add(new tableHelper("x22",0));
        registers.add(new tableHelper("x23",0));
        registers.add(new tableHelper("x24",0));
        registers.add(new tableHelper("x25",0));
        registers.add(new tableHelper("x26",0));
        registers.add(new tableHelper("x27",0));
        registers.add(new tableHelper("x28",0));
        registers.add(new tableHelper("x29",0));
        registers.add(new tableHelper("x30",0));
        registers.add(new tableHelper("x31",0));
        return registers;
    }

    /**
     *  Method reads content in registers x0 to x31 and outputs in a binary file.
     *  @output binary file 'output.bin'
     */
    private static void outToBin(int[] reg) throws IOException {
        DataOutputStream dos = new DataOutputStream(new FileOutputStream("output.bin"));
        for (int val : reg) {
            dos.writeInt(Integer.reverseBytes(val));
        }
        dos.close();
    }
}
