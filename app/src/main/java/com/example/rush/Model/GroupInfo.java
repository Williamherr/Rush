package com.example.rush.Model;

//import java.util.ArrayList;

public class GroupInfo {
    private String groupName;
    private String instructor;
    private String description;
    private String createdBy;
    private String docID;
   // private String groupID;
  //  private ArrayList<Member> studentsInGroup;
    //Add a structure to store Students that are in this group

    public GroupInfo(String groupName, String instructor, String description, String createdBy) {
        this.groupName = groupName;
        this.instructor = instructor;
        this.description = description;
        this.createdBy = createdBy;
       // this.groupID = groupID;
    }

    public GroupInfo() {

    }

   // public void addStudent(Member m) {
//        studentsInGroup.add(m);
  //  }

  //  public void removeStudents() {
   //     studentsInGroup.clear();
 //   }

  //  public void setGroupID(String groupID) {
     //   this.groupID = groupID;
   // }

   // public String getGroupID() {
     //   return groupID;
   // }
   public void setDocID(String docID) {
       this.docID = docID;
   }

    public String getDocID() {
        return docID;
    }
    public String getGroupName() {
        return groupName;
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
