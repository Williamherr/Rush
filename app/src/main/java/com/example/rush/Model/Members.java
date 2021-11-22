package com.example.rush.Model;

import java.util.ArrayList;

public class Members {

    private ArrayList<Member> members;

    public Members(){
        members = new ArrayList<>();
    }

    public Members(ArrayList<Member> members) {
        this.members = members;
    }

    public ArrayList<Member> getAllMembers() {
        return members;
    }

    public Member getOtherMember(String uid) {
        Member user = null;
        for (int i = 0; i < members.size(); i++) {

            if (!uid.equals(members.get(i).getUid())) {
                return members.get(i);
            }else {
                user = members.get(i);
            }
        }
        return user;
    }



    public Member getMember(int index) {
        return members.get(index);
    }


    public void addMembers(Member member) {
        this.members.add(member);
    }


    public String getAllMembersName() {
        String allMembers = "";
        for (Member member : this.members) {
            allMembers += member.getName() + " ";
        }
        return allMembers;
    }


}
