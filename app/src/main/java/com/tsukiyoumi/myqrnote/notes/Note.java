package com.tsukiyoumi.myqrnote.notes;

public class Note {

    private long id;
    private String content;
    private String time;
    private String loc;
    private int tag;

    public Note() {
    }

    public Note(String content, String time, String loc, int tag) {
        this.content = content;
        this.time = time;
        this.loc = loc;
        this.tag = tag;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public String getTime() {
        return time;
    }

    public String getLoc() {
        return loc;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setLoc(String loc) {
        this.loc = loc;
    }

//    @Override
//    public String toString() {
//        return content + "\n" + time.substring(5,16) + " "+ id;
//    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }
}

