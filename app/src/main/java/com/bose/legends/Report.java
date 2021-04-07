package com.bose.legends;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.firebase.Timestamp;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public class Report
{
    private String assignedTo, groupID, message, reason, reportedBy, reportedUser, reportID;
    @JsonIgnore
    private Timestamp time;

    public Report()
    {}

    public Report(String groupID, String message, String reason, String reportedBy, String reportedUser)
    {
        this.assignedTo = "none";
        this.groupID = groupID;
        this.message = message;
        this.reason = reason;
        this.reportedBy = reportedBy;
        this.reportedUser = reportedUser;
        this.time = Timestamp.now();
    }

    public Report(Report report)
    {
        this.assignedTo = report.getAssignedTo();
        this.groupID = report.getGroupID();
        this.message = report.getMessage();
        this.reason = report.getReason();
        this.reportedBy = report.getReportedBy();
        this.reportedUser = report.getReportedUser();
        this.reportID = report.getReportID();
        this.time = report.getTime();
    }

    public String getAssignedTo()
    {
        return assignedTo;
    }

    public void setAssignedTo(String assignedTo)
    {
        this.assignedTo = assignedTo;
    }

    public String getGroupID()
    {
        return groupID;
    }

    public void setGroupID(String groupID)
    {
        this.groupID = groupID;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getReason()
    {
        return reason;
    }

    public void setReason(String reason)
    {
        this.reason = reason;
    }

    public String getReportedBy()
    {
        return reportedBy;
    }

    public void setReportedBy(String reportedBy)
    {
        this.reportedBy = reportedBy;
    }

    public String getReportedUser()
    {
        return reportedUser;
    }

    public void setReportedUser(String reportedUser)
    {
        this.reportedUser = reportedUser;
    }

    public void setTime(Timestamp actionTime)
    {
        this.time = actionTime;
    }

    public Timestamp getTime()
    {
        return time;
    }

    public String getReportID()
    {
        return reportID;
    }

    public void setReportID(String reportID)
    {
        this.reportID = reportID;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Report report = (Report) o;

        return Objects.equals(reportID, report.reportID);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(assignedTo, groupID, message, reason, reportedBy, reportedUser, reportID, time);
    }

    @Override
    public String toString()
    {
        Date date = time.toDate();
        DateFormat format = SimpleDateFormat.getDateInstance(DateFormat.LONG);

        return "Report{" +
                "reportID='" + reportID + '\'' +
                "assignedTo='" + assignedTo + '\'' +
                ", groupID='" + groupID + '\'' +
                ", message='" + message + '\'' +
                ", reason='" + reason + '\'' +
                ", reportedBy='" + reportedBy + '\'' +
                ", reportedUser='" + reportedUser + '\'' +
                ", time='" + format.format(date) + '\'' +
                '}';
    }
}
