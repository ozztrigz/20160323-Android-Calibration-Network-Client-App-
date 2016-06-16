package ca.on.tradeport.calibrationnetwork;

/**
 * Created by webmaster on 16-03-04.
 */
public class Settings {


    public String label;
    public String value;

    public Settings (String label, String value){

        this.label = label;
        this.value = value;

    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
