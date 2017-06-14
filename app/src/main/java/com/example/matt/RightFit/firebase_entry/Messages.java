package com.example.matt.RightFit.firebase_entry;

import java.util.Date;

/**
 * Created by Matt on 3/2/2017.
 */
public class Messages {

    private String  message, //Content of message
                    receiver, //Receiver of Message
                    sender; //Sender of Message
    //public Boolean  online; //If the user is online
    //TODO: add date, use class Calendar
    private Date     date; //Date of the message
    private String stringDate;

    //public int      status; //status of the Message
    //public static final int STATUS_SENDING = 0;
    //public static final int STATUS_SENT = 1;
    //public static final int STATUS_FAILED = 2;

    //TODO: add the user's profile picture
    //public String photoURL;

    public Messages()
    {
        message= null;
        receiver= null;
        sender= null;
        //status=0;
        date=null;
    }
    //constructor
    public Messages(String msg, Date date, String sender, String receiver) {
        this.message = msg;
        this.date = date;
        this.sender = sender;
        this.receiver = receiver;
    }


    public String getMessage(){return message;}
    public void setMessage(String message){this.message = message;}
    //public void setStatus(int status) {this.status = status;}
    //public int getStatus(){return status;}
    public String getReceiver() {    return receiver;}
    public void setReceiver(String receiver){ this.receiver=receiver;}
    public String getSender() {return sender;}
    public void setSender(String sender){ this.sender=sender;}
    public Date getDate() {return date;}
    public void setDate(Date date) {this.date = date;}
    public void setStringDate(String date) {this.stringDate = date;}



}
