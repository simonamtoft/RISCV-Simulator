/* File: guiController.java
 * Authors: Marc Sun Bog & Simon Amtoft Pedersen
 *
 * The following file handles the controls associated with the GUI
 */

package RISCVSimulator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class guiController implements Initializable{
    // UI elements
    public VBox mainVBox;
    public MenuItem menuItemOpen;
    public MenuItem menuItemExit;

    // Buttons
    public Button buttonNext;
    public Button buttonPrevious;
    public Button buttonRun;
    public Button buttonReset;

    //Output
    public TextArea outputArea;

    // Table elements
    public TableView<TableHelper> regTable;
    public TableColumn<TableHelper, String> registerColumn;
    public TableColumn<TableHelper, String> registerValueColumn;
    public TableView<TableHelper> memTable;
    public TableColumn<TableHelper, String> memoryColumn;
    public TableColumn<TableHelper, String> memoryDataColumn;
    public TableView<TableHelper> pcTable;
    public TableColumn<TableHelper, String> pcColumn;
    public TableColumn<TableHelper, String> instructionColumn;

    //Table selection
    private TableView.TableViewSelectionModel<TableHelper> pcSelection;
    private TableView.TableViewSelectionModel<TableHelper> regSelection;
    private TableView.TableViewSelectionModel<TableHelper> memSelection;

    // Controller variables
    private CPU cpu;
    private Instruction[] program;
    private Memory mem = new Memory(1024);

    //History keeping for stepping back and forth
    private ArrayList<int[]> regHistory = new ArrayList<>();
    private ArrayList<Integer> pcHistory = new ArrayList<>();
    private ArrayList<byte[]> memHistory = new ArrayList<>();

    /**
     * Runs in start of guiController.
     * Initializes all the three tables: regTable, memTable and pcTable.
     * @Override
     */
    public void initialize(URL location, ResourceBundle resources) {
        pcColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        instructionColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        pcSelection = pcTable.getSelectionModel();
        registerColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        registerValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        regTable.setItems(initializeRegisterTable());
        regSelection = regTable.getSelectionModel();
        memoryColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        memoryDataColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        memTable.setItems(initializeMemoryTable());
        memSelection = memTable.getSelectionModel();
    }

    /**
     * Following method handles file picking by user in the menu.
     * If file is not picked, buttons are disabled.
     */
    public void chooseFile() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open binary RISC-V code");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File file = fileChooser.showOpenDialog(mainVBox.getScene().getWindow());
        if(file != null){
            // Initialize processor
            program = getInstructions(file);
            cpu = new CPU(mem, program);

            // Initialize register, memory and pc
            memTable.setItems(initializeMemoryTable());
            regTable.setItems(initializeRegisterTable());
            pcTable.setItems(initializePcTable(program));

            // Display default stack pointer value
            replaceTableVal(regTable, 2, String.format("%d", cpu.reg[2]));

            // Default button states
            buttonNext.setDisable(false);
            buttonRun.setDisable(false);
            buttonReset.setDisable(false);
        } else {
            program = null;
            cpu = null;

            // Disable all buttons
            buttonNext.setDisable(true);
            buttonPrevious.setDisable(true);
            buttonRun.setDisable(true);
            buttonReset.setDisable(true);
        }
        // Clear selections
        pcSelection.clearSelection();
        memSelection.clearSelection();
        regSelection.clearSelection();

        // Clear history
        regHistory = new ArrayList<>();
        memHistory = new ArrayList<>();
        pcHistory  = new ArrayList<>();
    }

    /**
     * Handles action when 'next' button is pressed.
     * If a file has been picked, and program is not done, then it executes the next instruction
     */
    public void executeNextInstruction(){
        if(buttonPrevious.isDisabled()) buttonPrevious.setDisable(false);
        int[] tempReg = new int[32];
        System.arraycopy(cpu.reg,0, tempReg,0,32);
        pcHistory.add(cpu.pc);
        regHistory.add(tempReg);

        // Only store copy of memory if the next instruction is sType to avoid too much wasted memory.
        if(program[cpu.pc].sType){
            byte[] tempMem = new byte[mem.getMemory().length];
            System.arraycopy(mem.getMemory(), 0, tempMem, 0, tempMem.length);
            memHistory.add(tempMem);
        }

        cpu.executeInstruction();
        updateNext();
        if(cpu.pc >= program.length){ // Disable press of button if program is done
            buttonRun.setDisable(true);
            buttonNext.setDisable(true);
        }
    }

    public void closeProgram() throws IOException{
        outToBin(cpu.reg);
        System.exit(0);
    }

    public void rewindOnce() {
        if(buttonNext.isDisabled()) buttonNext.setDisable(false);
        if(buttonRun.isDisabled()) buttonRun.setDisable(false);
        // If most recently executed instruction was sType, restore memory
        if(program[cpu.prevPc].sType){
            int addr = cpu.reg[program[cpu.prevPc].rs1] + program[cpu.prevPc].immS;
            System.arraycopy(memHistory.get(memHistory.size()-1), 0, mem.getMemory(), 0, mem.getMemory().length);
            replaceTableVal(memTable, addr >> 2, String.format("0x%08X", mem.getWord(addr & 0xFFFFFF00)));
            memSelection.clearAndSelect(addr >> 2);
        }


        //Revert program counter
        if(pcHistory.size() > 1) cpu.prevPc = pcHistory.get(pcHistory.size() - 2);
        else cpu.prevPc = 0;
        cpu.pc = pcHistory.get(pcHistory.size() - 1);
        pcSelection.clearAndSelect(cpu.prevPc); //Select previous program counter

        //Select program counter from 2 iterations back
        regSelection.clearAndSelect(program[cpu.prevPc].rd);

        //Revert register values
        System.arraycopy(regHistory.get(regHistory.size()-1), 0, cpu.reg, 0, 32);
        replaceTableVal(regTable, program[cpu.pc].rd, String.format("%d", cpu.reg[program[cpu.pc].rd]));

        //Delete from history
        pcHistory.remove(pcHistory.size() - 1);
        regHistory.remove(regHistory.size() - 1);
        if(pcHistory.isEmpty()){
            buttonPrevious.setDisable(true);
            regSelection.clearSelection();
            memSelection.clearSelection();
            pcSelection.clearSelection();
        }
    }

    /**
     * Handles action when 'run' button is pressed.
     * If a file has been picked, and program is not done, then it executes the remaining instructions
     */
    public void executeRestOfProgram() {
        if(program == null || cpu == null || mem == null) return;
        if(cpu.pc >= program.length) return;

        while(cpu.pc < program.length){
            cpu.executeInstruction();
            updateNext();
        }

        // Disable buttons except reset
        buttonNext.setDisable(true);
        buttonPrevious.setDisable(true);
        buttonRun.setDisable(true);

        // Clear selections
        pcSelection.clearSelection();
        memSelection.clearSelection();
        regSelection.clearSelection();

        // Clear history
        regHistory = new ArrayList<>();
        memHistory = new ArrayList<>();
        pcHistory  = new ArrayList<>();
    }

    /**
     * Handles action when 'reset' button is pressed.
     * Initializes memTable, pcTable and regTable from start values.
     */
    public void resetProgram() {
        // Re-enable buttons
        buttonNext.setDisable(false);
        buttonPrevious.setDisable(true);
        buttonRun.setDisable(false);

        // New CPU instance and refreshing data.
        cpu = new CPU(mem, program);
        memTable.setItems(initializeMemoryTable());
        regTable.setItems(initializeRegisterTable());
        replaceTableVal(regTable, 2, String.format("%d", cpu.reg[2]));
        pcTable.setItems(initializePcTable(program));

        // Clear selections
        pcSelection.clearSelection();
        memSelection.clearSelection();
        regSelection.clearSelection();

        // Clear history
        regHistory = new ArrayList<>();
        memHistory = new ArrayList<>();
        pcHistory  = new ArrayList<>();
    }



    /* HELPER METHODS FOR GUI CONTROL */

    //Updates GUI according to next instruction
    private void updateNext() {
        replaceTableVal(regTable, program[cpu.prevPc].rd, String.format("%d", cpu.reg[program[cpu.prevPc].rd]));
        pcSelection.clearAndSelect(cpu.prevPc);
        if(program[cpu.prevPc].noRd){
            if(program[cpu.prevPc].sType){
                int addr = (cpu.reg[program[cpu.prevPc].rs1] + program[cpu.prevPc].immS) & 0xFFFFFFFC;
                //Get word-address by removing byte-offset
                replaceTableVal(memTable, addr >> 2, String.format("0x%08X", mem.getWord(addr)));
                memSelection.clearAndSelect(addr >> 2);
            }
            return;
        }
        regSelection.clearAndSelect(program[cpu.prevPc].rd);
    }

     //This method will replace a value in the given table
    private void replaceTableVal(TableView<TableHelper> table, int index, String val) {
        table.getItems().get(index).setValue(val);
    }

    // This method simulates a console in the GUI. Outputs the input string on next line
    private void consolePrint(String outPrint) {
        outputArea.setText(outputArea.getText()+outPrint+"\n"); // doesn't work when called in main.
    }

    private ObservableList<TableHelper> initializePcTable(Instruction[] program){
        ObservableList<TableHelper> pcTable = FXCollections.observableArrayList();
        for(int i = 0; i < program.length; i++){
            pcTable.add(new TableHelper(String.format("%d", i << 2), String.format("%s", program[i].assemblyString)));
        }
        return pcTable;
    }

    private ObservableList<TableHelper> initializeMemoryTable(){
        ObservableList<TableHelper> memTable = FXCollections.observableArrayList();
        for(int i = 0; i < mem.getMemory().length; i += 4){
            memTable.add(new TableHelper(String.format("0x%04X", i), String.format("0x%08X",0)));
        }
        return memTable;
    }

    private ObservableList<TableHelper> initializeRegisterTable(){
        ObservableList<TableHelper> regTable = FXCollections.observableArrayList();
        for(int i = 0; i < 32; i++){
            regTable.add(new TableHelper("x"+i, "0"));
        }
        return regTable;
    }

    // Adds instructions from binary file to program array
    private Instruction[] getInstructions(File f) throws IOException {
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
