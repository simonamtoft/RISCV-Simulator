package RISCVSimulator;

import javafx.beans.property.SimpleStringProperty;

public class tableHelper {
    private SimpleStringProperty name;
    private Integer value;

    public tableHelper(String name, int value) {
        this.name = new SimpleStringProperty(name);
        this.value = value;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }
}
