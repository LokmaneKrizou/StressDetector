package com.choosemuse.example.libmuse.utils;

// Created by Lokmane Krizou on 4/28/2018.
public class TaskListModle {

    boolean isSelected;
    String taskName;
    String taskTitle;
    //now create constructor and getter setter method using shortcut like command+n for mac & Alt+Insert for window.


    public TaskListModle(boolean isSelected, String taskName,String taskTitle) {
        this.isSelected = isSelected;
        this.taskName = taskName;
        this.taskTitle= taskTitle;
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
    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }
}
