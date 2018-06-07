package com.choosemuse.example.libmuse.utils;



public class TaskModle {

    boolean isSelected;
    String taskName;

    //now create constructor and getter setter method using shortcut like command+n for mac & Alt+Insert for window.


    public TaskModle(boolean isSelected, String taskName) {
        this.isSelected = isSelected;
        this.taskName = taskName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }
}
