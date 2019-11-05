package com.example.iyeeun.qr_termproject;


public class UserInform {
    public String uid;
    public String name;
    public String phonenumber;
    public String message;

    UserInform(){
        uid = "";
        name = "";
        phonenumber = "";
    }
    UserInform(String id, String n, String ph){
        uid = id;
        name = n;
        phonenumber = ph;
    }

    public UserInform stringToUser(String json){

        String temp = json.replace("\"", "");
        temp = temp.replace("[","" );
        temp = temp.replace("]","");
        temp = temp.replace("{", "");
        temp = temp.replace("}", "");
        temp = temp.replace(",", ":");
        temp = temp.trim();

        String[] split = temp.split(":");
        for(int i = 0 ; i < split.length ; ++i){
        }

        uid = split[1];
        name = split[3];
        phonenumber = split[5];
        message = split[7];

        return this;
    }
}

