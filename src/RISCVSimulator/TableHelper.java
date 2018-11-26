/* File: TableHelper.java
 * Authors: Marc Sun Bøg & Simon Amtoft Pedersen
 * 
 * This file defines an object, which is used to make 2 column TableViews
 */

package RISCVSimulator;

import javafx.beans.property.SimpleStringProperty;

public class TableHelper {
    private SimpleStringProperty name;
    private SimpleStringProperty value;

    public TableHelper(String name, String value){
        this.name = new SimpleStringProperty(name);
        this.value = new SimpleStringProperty(value);
    }

    public String getName(){
        return name.get();
    }

    public String getValue(){
        return value.get();
    }

    public void setName(String name){
        this.name.set(name);
    }

    public void setValue(String value){
        this.value.set(value);
    }

    public SimpleStringProperty nameProperty(){
        return name;
    }

    public SimpleStringProperty valueProperty(){
        return value;
    }
}
