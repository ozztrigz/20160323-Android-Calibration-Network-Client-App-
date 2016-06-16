package ca.on.tradeport.calibrationnetwork;


public class Instrument {


    public String instrumentID;
    public String asset;
    public String serial;
    public String name;
    public String description;
    public String model;
    public String category;
    public String manufacturer;
    public int calType;


    public Instrument(String instrumentID, String asset, String serial, String name, String description, String model, String category, String manufacturer) {

        this.instrumentID = instrumentID;
        this.asset = asset;
        this.serial = serial;
        this.name = name;
        this.description = description;
        this.model = model;
        this.category = category;
        this.manufacturer = manufacturer;


    }

    public Instrument(String instrumentID, String asset, String serial, String name, String description, String model, String category, String manufacturer, int calType) {

        this.instrumentID = instrumentID;
        this.asset = asset;
        this.serial = serial;
        this.name = name;
        this.description = description;
        this.model = model;
        this.category = category;
        this.manufacturer = manufacturer;
        this.calType = calType;


    }


    public int getCalType() {
        return calType;
    }

    public void setCalType(int calType) {
        this.calType = calType;
    }

    public String getInstrumentID() {
        return instrumentID;
    }

    public void setInstrumentID(String instrumentID) {
        this.instrumentID = instrumentID;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
}
