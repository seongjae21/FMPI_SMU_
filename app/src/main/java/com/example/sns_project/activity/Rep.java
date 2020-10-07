package com.example.sns_project.activity;

import java.io.Serializable;
import java.util.Date;

public class Rep implements Serializable {

    private String repId;
    private String publisher;
    private String contents;

    private Date date;

    public Rep() {
    }

    public Rep(String repId, String publisher, String contents) {
        this.repId = repId;
        this.publisher = publisher;
        this.contents = contents;
    }

    public Rep(String publisher,String contents){
        this.publisher = publisher;
        this.contents = contents;
    }

    public String getRepId() {
        return repId;
    }

    public void setRepId(String repId) {
        this.repId = repId;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "Rep{" +
                "repId='" + repId + '\'' +
                ", title='" + publisher + '\'' +
                ", contents='" + contents + '\'' +
                '}';
    }
}
