package com.example.rush;

public class ClassInfo {
    private String className;
    private String instructor;
    private String description;
    private String createdBy;
    //Add a structure to store Students that are in this class

    public ClassInfo(String className, String instructor, String description, String createdBy) {
        this.className = className;
        this.instructor = instructor;
        this.description = description;
        this.createdBy = createdBy;
    }

    public ClassInfo() {

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