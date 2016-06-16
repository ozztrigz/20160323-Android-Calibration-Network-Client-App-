package ca.on.tradeport.calibrationnetwork;


public class Task {
    public String taskID;
    public String wo;
    public String taskType;
    public String status;
    public String model;
    public String serial;
    public String manufacturer;
    public String certificate;

    public Task(String taskID, String wo, String taskType, String status, String model, String serial, String manufacturer, String certificate){
        this.taskID = taskID;
        this.wo = wo;
        this.taskType = taskType;
        this.status = status;
        this.model = model;
        this.serial = serial;
        this.manufacturer = manufacturer;
        this.certificate = certificate;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public String getWo() {
        return wo;
    }

    public void setWo(String wo) {
        this.wo = wo;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }
}
