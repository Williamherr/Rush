package com.example.rush.View.fragments;

public class ClassInfo {
    private String className;
    private String instructor;
    private String description;
    private String createdBy;
    private String docID;
    //Add a structure to store Students that are in this class

    public ClassInfo(String className, String instructor, String description, String createdBy) {
        this.className = className;
        this.instructor = instructor;
        this.description = description;
        this.createdBy = createdBy;
    }

    public ClassInfo() {

    }

    public void setDocID(String docID) {
        this.docID = docID;
    }

    public String getDocID() {
        return docID;
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