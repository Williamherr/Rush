package com.example.rush.Model;

public class ClassInfo {
    private String className;
    private String instructor;
    private String description;
    private String createdBy;
    private String classID;

    public ClassInfo(String className, String instructor, String description, String createdBy, String classID) {
        this.className = className;
        this.instructor = instructor;
        this.description = description;
        this.createdBy = createdBy;
        this.classID = classID;
    }

    public ClassInfo() {

    }

    public void setClassID(String classID) {
        this.classID = classID;
    }

    public String getClassID() {
        return classID;
    }

    public String getClassName() {
        return className;
    }

    public String getInstructor() {
        return instructor;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}