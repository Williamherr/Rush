package com.example.rush;

public class ClassInfo {
    private String name;
    private String instructor;
    private String description;
    //Add a structure to store Students that are in this class

    public ClassInfo(String name, String instructor, String description) {
        this.name = name;
        this.instructor = instructor;
        this.description = description;
    }

    public String getClassName() {
        return name;
    }

    public String getInstructor() {
        return instructor;
    }

    public String getDescription() {
        return description;
    }
}