/* File: guiController.java
 * Authors: Marc Sun Bøg & Simon Amtoft Pedersen
 *
 * The following file handles the flow of the entire program. 
 * The methods in this file are mostly GUI methods, which uses methods from other files.
 */

package RISCVSimulator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class guiController implements Initializable{
    // CONSTANTS
    private static final int BYTES_PR_PAGE = 256; 	// 64 words
    private static final int MEMORY_SIZE = 10485760; 	// 10MiB memory
    
    // Keeping track of memory table
    private int tableRootAddress = 0;
    
    // FXML ELEMENTS
    private Stage primaryStage;
    
    // UI elements
    public VBox mainVBox;
    public MenuItem menuItemOpen;
    public MenuItem menuItemExit;
    
    // Input
    public Button buttonNext;
    public Button buttonPrevious;
    public Button buttonRun;
    public Button buttonReset;
    public Button buttonNextTable;
    public Button buttonPreviousTable;
    public TextField textFieldAddr;
    
    // Output
    public TextArea textFieldConsole;
    
    // Table elements
    public TableView<TableHelper> registerTable;
    public TableColumn<TableHelper, String> registerColumn;
    public TableColumn<TableHelper, String> registerValueColumn;
    public TableView<TableHelper> memoryTable;
    public TableColumn<TableHelper, String> memoryColumn;
    public TableColumn<TableHelper, String> memoryDataColumn;
    public TableView<TableHelper> programTable;
    public TableColumn<TableHelper, String> programColumn;
    public TableColumn<TableHelper, String> programInstructionColumn;
    
    //Table selection
    private TableView.TableViewSelectionModel<TableHelper> pcSelection;
    private TableView.TableViewSelectionModel<TableHelper> regSelection;
    private TableView.TableViewSelectionModel<TableHelper> memSelection;
    
    // Controller variables
    private CPU cpu;
    private Instruction[] program;
    private Memory mem = new Memory(MEMORY_SIZE);
    
    // History keeping for stepping back and forth
    private ArrayList<int[]> regHistory = new ArrayList<>();
    private ArrayList<Integer> pcHistory = new ArrayList<>();
    private ArrayList<byte[]> memHistory = new ArrayList<>();

    /**
     * Runs in start of guiController.
     * Initializes registerTable, memoryTable and programTable.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Instruction table 
        programColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        programInstructionColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        pcSelection = programTable.getSelectionModel();
        
	// Register table
        registerColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        registerValueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        regSelection = registerTable.getSelectionModel();
        
	// Memory table
        memoryColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        memoryDataColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        memSelection = memoryTable.getSelectionModel();
    }

    /**
     * Displays file chooser when Ctrl+O is asserted or when Open... button is pressed.
     * If file is not picked, buttons are disabled.
     * @throws IOException Throws exception if file is busy
     */
    public void chooseFile() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open binary RISC-V code");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        File file = fileChooser.showOpenDialog(primaryStage);
        if(file != null){
            // Initialize processor
            program = getInstructions(file);
            cpu = new CPU(mem, program);
            
	    // Initialize pc, mem and register tables
            programTable.setItems(initializePcTable(program));
            memoryTable.setItems(initializeMemoryTable(0));
            registerTable.setItems(initializeRegisterTable());
            
	    // Display default stack pointer value
            replaceTableVal(registerTable, 2, String.format("%d", cpu.reg[2]));
            
	    // Default button states
            buttonNext.setDisable(false);
            buttonRun.setDisable(false);
            buttonReset.setDisable(false);
            textFieldAddr.setDisable(false);
            if(BYTES_PR_PAGE < MEMORY_SIZE) buttonNextTable.setDisable(false);
            textFieldConsole.setText("");
            primaryStage.setTitle("RV32I Simulator - "+file.getName());
        } else {
            program = null;
            cpu = null;
            textFieldConsole.setText("No file chosen.");
            
            // Disable all buttons
            buttonNext.setDisable(true);
            buttonPrevious.setDisable(true);
            buttonRun.setDisable(true);
            buttonReset.setDisable(true);
            buttonNextTable.setDisable(true);
            buttonPreviousTable.setDisable(true);
            textFieldAddr.setDisable(true); 
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
     * Displays file chooser when Ctrl+S is asserted or when Save button is pressed.
     * Does nothing if no file is picked.
     * @throws IOException Throws exception if file is busy
     */
    public void saveRegisters() throws IOException{
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Store register values as binary file");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Binary result files (*.res)",".res"));
        File file = fileChooser.showSaveDialog(primaryStage);
        if(file != null){
            if(cpu == null){
                textFieldConsole.setText("ERROR: CPU not initialized");
                return;
            }
            outToBin(file, cpu.reg);
        }
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

    /**
     * Handles action when 'previous' button is pressed.
     * Reverts differences caused by previously executed instruction
     */
    public void rewindOnce() {
        if(buttonNext.isDisabled()) buttonNext.setDisable(false);
        if(buttonRun.isDisabled()) buttonRun.setDisable(false);
        // If most recently executed instruction was sType, restore memory
        if(program[cpu.prevPc].sType){
            System.arraycopy(memHistory.get(memHistory.size()-1), 0, mem.getMemory(), 0, MEMORY_SIZE);
            memHistory.remove(memHistory.size() - 1);
            updateMemoryTable();
        }
        //Revert program counter
        if(pcHistory.size() > 1) cpu.prevPc = pcHistory.get(pcHistory.size() - 2);
        else cpu.prevPc = 0;
        cpu.pc = pcHistory.get(pcHistory.size() - 1);
        pcSelection.clearAndSelect(cpu.prevPc); //Select previous program counter
        regSelection.clearAndSelect(program[cpu.prevPc].rd);
        
	//Revert register values
        System.arraycopy(regHistory.get(regHistory.size()-1), 0, cpu.reg, 0, 32);
        replaceTableVal(registerTable, program[cpu.pc].rd, String.format("%d", cpu.reg[program[cpu.pc].rd]));
        
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
     * Handles action when 'Run' button is pressed.
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
     * Refreshes data, clears histories and resets selection.
     */
    public void resetProgram() {
        // New CPU instance and refreshing data.
        cpu = new CPU(mem, program);
        memoryTable.setItems(initializeMemoryTable(tableRootAddress = 0));
        registerTable.setItems(initializeRegisterTable());
        replaceTableVal(registerTable, 2, String.format("%d", cpu.reg[2]));
        programTable.setItems(initializePcTable(program));
        
	// Clear selections
        pcSelection.clearSelection();
        memSelection.clearSelection();
        regSelection.clearSelection();

        // Re-enable buttons
        buttonNext.setDisable(false);
        buttonPrevious.setDisable(true);
        buttonRun.setDisable(false);
        setMemoryButtonStates();

        // Clear history
        regHistory = new ArrayList<>();
        memHistory = new ArrayList<>();
        pcHistory  = new ArrayList<>();
        textFieldConsole.setText("");
    }

	// Exits application when Ctrl+Q is asserted or Exit button is pressed.
    public void closeProgram() {
        System.exit(0);
    }


    /* HELPER METHODS FOR GUI CONTROL */


    /**
     * Updates TableView with results from executed instruction
     */
    private void updateNext() {
        replaceTableVal(registerTable, program[cpu.prevPc].rd, String.format("%d", cpu.reg[program[cpu.prevPc].rd]));
        pcSelection.clearAndSelect(cpu.prevPc);
        pcSelection.getTableView().scrollTo(cpu.prevPc);
        if(program[cpu.prevPc].noRd){
            if(program[cpu.prevPc].sType) updateMemoryTable();
            if(program[cpu.prevPc].ecall) {
                switch(cpu.reg[10]){
                    case 1:
                        consolePrint(String.format("%d", cpu.reg[11]));
                        break;
                    case 4:
                        consolePrint(mem.getString(cpu.reg[11]));
                        break;
                    case 11:
                        consolePrint(String.format("%c", cpu.reg[11]));
                        break;
                }
            }
            return;
        }
        regSelection.clearAndSelect(program[cpu.prevPc].rd);
        regSelection.getTableView().scrollTo(program[cpu.prevPc].rd);
    }

    /**
     * Changes memory table view from tableRootAddress to tableRootAddress - BYTES_PR_PAGE
     * Disables corresponding button if needed.
     */
    public void previousMemoryTable() {
        tableRootAddress -= BYTES_PR_PAGE;
        memSelection.clearSelection();
        memoryTable.setItems(initializeMemoryTable(tableRootAddress));
        setMemoryButtonStates();
    }
	
    /**
     * Changes memory table view from tableRootAddress to tableRootAddress + BYTES_PR_PAGE
     * Disables corresponding button if needed.
     */
    public void nextMemoryTable() {
        tableRootAddress += BYTES_PR_PAGE;
        memSelection.clearSelection();
        memoryTable.setItems(initializeMemoryTable(tableRootAddress));
        setMemoryButtonStates();
    }

    /**
     * Attempts to parse textFieldAddr input as hexadecimal number. 
	 * If no exception caught, change table view to said address.
     */
    public void gotoAddress() {
        int destAddr, addrOffset;
        try{
            destAddr = Integer.parseInt(textFieldAddr.getText(), 16);
            textFieldAddr.setText("");
        } catch (NumberFormatException e){
            textFieldConsole.setText("Failed to parse 32bit hexadecimal address (without 0x-prefix)");
            return;
        }
        if(destAddr > MEMORY_SIZE-1){
            textFieldConsole.setText("Address exceeds memory ("+(MEMORY_SIZE-1)+" Bytes)");
            return;
        }
        tableRootAddress = BYTES_PR_PAGE * (destAddr / BYTES_PR_PAGE);
        addrOffset = destAddr - tableRootAddress;
        memoryTable.setItems(initializeMemoryTable(tableRootAddress));
        memSelection.clearAndSelect(addrOffset >> 2);
        memSelection.getTableView().scrollTo(addrOffset >> 2);
        setMemoryButtonStates();
    }

    /**
     * Gets address from previously executed instruction and updates table view accordingly
     */
    private void updateMemoryTable(){
        int addr = (cpu.reg[program[cpu.prevPc].rs1] + program[cpu.prevPc].imm) & 0xFFFFFFFC; // Remove byte offset
        int addrOffset;
        // Check if requested address is in same block as tableRootAddress
        if( addr / BYTES_PR_PAGE == tableRootAddress / BYTES_PR_PAGE){
            // Relative address compared to tableRootAddress
            addrOffset = addr - tableRootAddress;
            replaceTableVal(memoryTable, addrOffset >> 2, String.format("0x%08X", mem.getWord(addr)));
            memSelection.clearAndSelect(addrOffset >> 2);
            memSelection.getTableView().scrollTo(addrOffset >> 2);
        } else {
            tableRootAddress = BYTES_PR_PAGE * (addr / BYTES_PR_PAGE);
            addrOffset = addr - tableRootAddress;
            memoryTable.setItems(initializeMemoryTable(tableRootAddress));
            memSelection.clearAndSelect(addrOffset >> 2);
            memSelection.getTableView().scrollTo(addrOffset >> 2);
        }
        setMemoryButtonStates();
    }

    /**
     * Disables/Enables previous/next buttons depending on tableRootAddress
     */
    private void setMemoryButtonStates(){
        if(tableRootAddress == 0){
            buttonPreviousTable.setDisable(true);
            buttonNextTable.setDisable(false);
        } else if(tableRootAddress == MEMORY_SIZE-BYTES_PR_PAGE){
            buttonPreviousTable.setDisable(false);
            buttonNextTable.setDisable(true);
        } else {
            buttonPreviousTable.setDisable(false);
            buttonNextTable.setDisable(false);
        }
    }

    /**
     * Replaces value at given index in given table.
     * @param table: Target TableView object
     * @param index: Target index of table
     * @param val: Value to insert at index
     */
    private void replaceTableVal(TableView<TableHelper> table, int index, String val) {
        table.getItems().get(index).setValue(val);
    }

    /**
     * Simulates console output by appending outPrint to what is already there.
     * @param outPrint: String to append
     */
    private void consolePrint(String outPrint) {
        textFieldConsole.setText(textFieldConsole.getText()+outPrint);
    }

    /**
     * Sets up program table.
     * @param program: An array of Instruction objects.
     * @return Returns a new ObservableList with Program Counter and Parsed Instruction
     */
    private ObservableList<TableHelper> initializePcTable(Instruction[] program){
        ObservableList<TableHelper> pcTable = FXCollections.observableArrayList();
        for(int i = 0; i < program.length; i++){
            pcTable.add(new TableHelper(String.format("%d", i << 2), String.format("%s", program[i].assemblyString)));
        }
        return pcTable;
    }

    /**
     * Sets up memory table
     * @param startAddr: Start address that will be displayed as index 0 in resulting list
     * @return Returns a new ObservableList consisting of (up to) BYTES_PR_PAGE / 4 rows.
     */
    private ObservableList<TableHelper> initializeMemoryTable(int startAddr){
        ObservableList<TableHelper> memTable = FXCollections.observableArrayList();
        for(int addrOffset = 0; addrOffset < BYTES_PR_PAGE; addrOffset += 4){
            if(startAddr+addrOffset == MEMORY_SIZE) break;
            memTable.add(new TableHelper(String.format("0x%06X", startAddr + addrOffset), String.format("0x%08X", mem.getWord(startAddr+addrOffset))));
        }
        return memTable;
    }

    /**
     * Sets up register table
     * @return Returns a new ObservableList consisting of 32 registers with value 0.
     */
    private ObservableList<TableHelper> initializeRegisterTable(){
        ObservableList<TableHelper> regTable = FXCollections.observableArrayList();
        for(int i = 0; i < 32; i++){
            regTable.add(new TableHelper("x"+i, "0"));
        }
        return regTable;
    }

    /**
     * Adds instructions from binary file to program array
     * @param f: A RISC-V binary file
     * @return Array of parsed instructions
     * @throws IOException Throws exception if file is busy
     */
    private Instruction[] getInstructions(File f) throws IOException {
        DataInputStream dis = new DataInputStream(new FileInputStream(f));
        int len = (int) f.length()/4;                       // Number of instructions
        Instruction[] programInst = new Instruction[len];   // Instruction array
        for(int i = 0; i < len; i++){
            int data = Integer.reverseBytes(dis.readInt());
            programInst[i] = new Instruction(data);
            mem.storeWord(i*4, data);
        }
        dis.close();
        return programInst;
    }

    /**
     * Reads content in registers x0 to x31 and outputs to file
     * @param file: Save destination
     * @param reg: Array of integers to output
     * @throws IOException Throws exception if file is busy.
     */
    private static void outToBin(File file, int[] reg) throws IOException {
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
        for (int val : reg) {
            dos.writeInt(Integer.reverseBytes(val));
        }
        dos.close();
    }
	
	// Used to pass stage from main
    void setStage(Stage stage){
        this.primaryStage = stage;
    }
}
