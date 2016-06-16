package ca.on.tradeport.calibrationnetwork;


public class Job {

    public String jobID;
    public String description;
    public String status;
    public String PO;
    public String SO;
    public String customer;
    public String dateCreated;
    public String notes;

    public Job(String jobID, String description, String status, String PO, String SO, String customer, String dateCreated, String notes) {

        this.jobID = jobID;
        this.description = description;
        this.status = status;
        this.PO = PO;
        this.SO = SO;
        this.customer = customer;
        this.dateCreated = dateCreated;
        this.notes = notes;

    }

    public String getJobID() {
        return jobID;
    }

    public void setJobID(String jobID) {
        this.jobID = jobID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPO() {
        return PO;
    }

    public void setPO(String PO) {
        this.PO = PO;
    }

    public String getSO() {
        return SO;
    }

    public void setSO(String SO) {
        this.SO = SO;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
