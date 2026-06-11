package com.buhlergroup.pepper.action.admin;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.selfie.data.SelfieEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelfieAdapter extends RecyclerView.Adapter<SelfieAdapter.SelfieHolder> {

    public interface OnSelfieClick {
        void onClick(SelfieEntity selfie);
    }

    private final List<SelfieEntity> items = new ArrayList<>();
    private final Set<String> raffleLinkedIds = new HashSet<>();
    private final OnSelfieClick onClick;
    private File imagesDir;

    public SelfieAdapter(OnSelfieClick onClick) {
        this.onClick = onClick;
    }

    public void setData(List<SelfieEntity> data, File imagesDir, Set<String> linkedSelfieIds) {
        this.imagesDir = imagesDir;
        items.clear();
        items.addAll(data);
        raffleLinkedIds.clear();
        raffleLinkedIds.addAll(linkedSelfieIds != null ? linkedSelfieIds : Collections.emptySet());
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SelfieHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selfie, parent, false);
        return new SelfieHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SelfieHolder holder, int position) {
        SelfieEntity selfie = items.get(position);
        holder.number.setText("#" + selfie.number);
        holder.star.setVisibility(selfie.favorite ? View.VISIBLE : View.GONE);
        holder.raffle.setVisibility(raffleLinkedIds.contains(selfie.id) ? View.VISIBLE : View.GONE);
        holder.image.setImageBitmap(decodeThumb(new File(imagesDir, selfie.filename), 240));
        holder.itemView.setOnClickListener(v -> onClick.onClick(selfie));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static Bitmap decodeThumb(File file, int reqSize) {
        if (file == null || !file.exists()) {
            return null;
        }
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getPath(), bounds);
        int sample = 1;
        int largest = Math.max(bounds.outWidth, bounds.outHeight);
        while (largest / sample > reqSize) {
            sample *= 2;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sample;
        return BitmapFactory.decodeFile(file.getPath(), options);
    }

    static class SelfieHolder extends RecyclerView.ViewHolder {
        final ImageView image;
        final TextView number;
        final TextView star;
        final ImageView raffle;

        SelfieHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.itemSelfieImage);
            number = itemView.findViewById(R.id.itemSelfieNumber);
            star = itemView.findViewById(R.id.itemSelfieStar);
            raffle = itemView.findViewById(R.id.itemSelfieRaffle);
        }
    }
}
