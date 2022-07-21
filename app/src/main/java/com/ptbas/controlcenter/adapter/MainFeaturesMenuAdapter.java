package com.ptbas.controlcenter.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ptbas.controlcenter.R;
import com.ptbas.controlcenter.model.MainFeatureModel;
import com.ptbas.controlcenter.viewholder.MainFeatureViewHolder;

import java.util.ArrayList;

public class MainFeaturesMenuAdapter extends RecyclerView.Adapter<MainFeatureViewHolder> {

    ArrayList<MainFeatureModel> mainFeatureData;
    Context context;

    public MainFeaturesMenuAdapter(ArrayList<MainFeatureModel> mainFeatureData, Context context) {
        this.mainFeatureData = mainFeatureData;
        this.context = context;
    }

    @NonNull
    @Override
    public MainFeatureViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.cardview_main_feature, parent, false);
        return new MainFeatureViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MainFeatureViewHolder holder, int position) {
        holder.img.setImageResource(mainFeatureData.get(position).getImgName());
        holder.tv1.setText(mainFeatureData.get(position).getHeader());
        holder.tv2.setText(mainFeatureData.get(position).getDesc());
    }

    @Override
    public int getItemCount() {
        return mainFeatureData.size();
    }
}