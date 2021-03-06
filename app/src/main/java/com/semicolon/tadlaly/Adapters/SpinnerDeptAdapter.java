package com.semicolon.tadlaly.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.semicolon.tadlaly.Models.Spinner_DeptModel;
import com.semicolon.tadlaly.R;

import java.util.List;

public class SpinnerDeptAdapter extends ArrayAdapter {
    private Context context;
    private List<Spinner_DeptModel> spinner_deptModelList;
    public SpinnerDeptAdapter(@NonNull Context context, int resource, @NonNull List<Spinner_DeptModel> spinner_deptModelList) {
        super(context, resource, spinner_deptModelList);
        this.context=context;
        this.spinner_deptModelList=spinner_deptModelList;
    }

    @Override
    public int getCount() {
        return spinner_deptModelList.size();
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getView(position,convertView,parent);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view=null;

        if (view==null)
        {
            view= LayoutInflater.from(context).inflate(R.layout.spinner_item,parent,false);
        }
        TextView title = view.findViewById(R.id.title);
        Spinner_DeptModel model = spinner_deptModelList.get(position);
        title.setText(model.getName());
        return view;
    }

}
