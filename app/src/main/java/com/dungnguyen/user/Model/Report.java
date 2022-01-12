package com.dungnguyen.user.Model;

public class Report {
    private String sender;
    private String oderReport;
    private String reason;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getOderReport() {
        return oderReport;
    }

    public void setOderReport(String oderReport) {
        this.oderReport = oderReport;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Report(String sender, String oderReport, String reason) {
        this.sender = sender;
        this.oderReport = oderReport;
        this.reason = reason;
    }
}
