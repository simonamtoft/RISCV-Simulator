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

import java.net.URL;
import java.util.ResourceBundle;

public class guiController implements Initializable{
    // not used for anything yet..
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

    static int dummy;

    public guiController() {
    }

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

    public void nextButton() {
        consolePrint("Put 'next' button code here");
        dummy++;
        regTable.getItems().get(2).setValue(dummy);

    }

    public void previousButton() {
        System.out.println("Put 'previous' button code here");
    }

    public void runButton() {
        System.out.println("Put 'run' button code here");
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
     * This method will replace a value in the register table
     */
    public void replaceRegVal(int index, int val) {
        regTable.getItems().get(index).setValue(val); // doesn't work when called in main.
    }

    /**
     * This method simulates a console in the GUI
     * @output the input string on next line
     */

    public void consolePrint(String outPrint) {
        outputArea.setText(outputArea.getText()+outPrint+"\n"); // doesn't work when called in main.
    }
}
