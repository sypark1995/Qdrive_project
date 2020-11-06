package com.giosis.util.qdrive.main;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;

public class NavigationListItem {


    private Drawable icon;
    private String title;
    private ArrayList<String> child_list;

    ArrayList<String> getChild_list() {
        return child_list;
    }

    void setChild_list(ArrayList<String> child_list) {
        this.child_list = child_list;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}