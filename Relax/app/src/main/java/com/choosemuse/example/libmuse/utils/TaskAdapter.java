package com.choosemuse.example.libmuse.utils;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.choosemuse.example.libmuse.R;

import java.util.List;

// Created by Lokmane Krizou on 4/28/2018.
public class TaskAdapter extends BaseAdapter {
    Activity activity;
    List<TaskListModle> task;
    LayoutInflater inflater;

    //short to create constructer using command+n for mac & Alt+Insert for window


    public TaskAdapter(Activity activity) {
        this.activity = activity;
    }

    public TaskAdapter(Activity activity, List<TaskListModle> task) {
        this.activity   = activity;
        this.task = task;

        inflater        = activity.getLayoutInflater();
    }


    @Override
    public int getCount() {
        return task.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

     ViewHolder holder = null;

        if (view == null){

           // view = inflater.inflate(R.layout.task_list_item, viewGroup, false);

//            holder = new ViewHolder();
//            holder.taskTitle=(TextView)view.findViewById(R.id.task_title);
//            holder.taskName = (TextView)view.findViewById(R.id.task_name);
//            holder.taskCheckBox = (ImageView) view.findViewById(R.id.task_check_box);

            view.setTag(holder);
        }else
            holder = (ViewHolder)view.getTag();

        TaskListModle model = task.get(i);

        holder.taskName.setText(model.getTaskName());
        holder.taskTitle.setText(model.getTaskTitle());
//
//        if (model.isSelected())
//            holder.taskCheckBox.setBackgroundResource(R.drawable.checked);
//
//        else
//            holder.taskCheckBox.setBackgroundResource(R.drawable.unchecked);

        return view;

    }

    public void updateRecords(List<TaskListModle> task){
        this.task = task;

        notifyDataSetChanged();
    }

    class ViewHolder{

        TextView taskTitle;
        TextView taskName;
        ImageView taskCheckBox;

    }
}