package com.bose.legends;

import com.google.firebase.Timestamp;

import java.util.Objects;

public class Report
{
    private String assignedTo, groupID, message, reason, reportedBy, reportedUser, reportID;
    private Timestamp time;

    public Report()
    {
        this.time = Timestamp.now();
        this.assignedTo = "none";
    }

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

    public void setTime(Timestamp time)
    {
        this.time = time;
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
        return "Report{" +
                "reportID='" + reportID + '\'' +
                "assignedTo='" + assignedTo + '\'' +
                ", groupID='" + groupID + '\'' +
                ", message='" + message + '\'' +
                ", reason='" + reason + '\'' +
                ", reportedBy='" + reportedBy + '\'' +
                ", reportedUser='" + reportedUser + '\'' +
                ", time=" + time +
                '}';
    }
}
