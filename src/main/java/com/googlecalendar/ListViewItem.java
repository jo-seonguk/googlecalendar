package com.googlecalendar;


import java.util.ArrayList;

// 리스트 뷰 아이템 데이터에 대한 클래스 정의
public class ListViewItem extends ArrayList<ListViewItem> {
    int id;
    String name;
    String date;
    String al;
    String map;

    public ListViewItem(){

    }
    public ListViewItem(int id, String name, String date, String al, String map) {
        this.id = id;
        this.name = name;
        this.date = date;
        this.al = al;
        this.map = map;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }

    public String  getAl() {
        return al;
    }
    public void setAl(String  al) {
        this.al = al;
    }

    public String getMap() { return map; }
    public void setMap(String map) { this.map = map; }

}
