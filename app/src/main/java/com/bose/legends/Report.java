package com.bose.legends;

import com.google.firebase.Timestamp;

public class Report
{
    private String assignedTo, groupID, message, reason, reportedBy, reportedUser;
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

    @Override
    public String toString()
    {
        return "Report{" +
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
