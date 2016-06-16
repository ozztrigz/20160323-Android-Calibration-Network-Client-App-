package ca.on.tradeport.calibrationnetwork;


public class Calibration {

    public String calID;
    public String taskID;
    public String certificate;
    public String calDate;
    public String calDueDate;
    public String inSpec;
    public String returnedInSpec;
    public String calProcedure;
    public String temperature;
    public String humidity;
    public String report;
    public String years;


    public Calibration(String taskID, String certificate, String calDate, String calDueDate, String inSpec, String returnedInSpec, String calProcedure, String temperature, String humidity, String report, String years, String calID) {

        this.calID = calID;
        this.taskID = taskID;
        this.certificate = certificate;
        this.calDate = calDate;
        this.calDueDate = calDueDate;
        this.inSpec = inSpec;
        this.returnedInSpec = returnedInSpec;
        this.calProcedure = calProcedure;
        this.temperature = temperature;
        this.humidity = humidity;
        this.report = report;
        this.years = years;
    }

    public Calibration(String certificate, String calID, String taskID) {

        this.certificate = certificate;
        this.calID = calID;
        this.taskID = taskID;

    }


    public String getCalID() {
        return calID;
    }

    public void setCalID(String calID){
        this.calID = calID;
    }

    public String getTaskID() {
        return taskID;
    }

    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getCalDate() {
        return calDate;
    }

    public void setCalDate(String calDate) {
        this.calDate = calDate;
    }

    public String getCalDueDate() {
        return calDueDate;
    }

    public void setCalDueDate(String calDueDate) {
        this.calDueDate = calDueDate;
    }

    public String getInSpec() {
        return inSpec;
    }

    public void setInSpec(String inSpec) {
        this.inSpec = inSpec;
    }

    public String getReturnedInSpec() {
        return returnedInSpec;
    }

    public void setReturnedInSpec(String returnedInSpec) {
        this.returnedInSpec = returnedInSpec;
    }

    public String getCalProcedure() {
        return calProcedure;
    }

    public void setCalProcedure(String calProcedure) {
        this.calProcedure = calProcedure;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(String humidity) {
        this.humidity = humidity;
    }

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getYears() {
        return years;
    }

    public void setYears(String years) {
        this.years = years;
    }
}
