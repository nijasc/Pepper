package com.buhlergroup.pepper.action.admin;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.selfie.data.SelfieEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SelfieAdapter extends RecyclerView.Adapter<SelfieAdapter.SelfieHolder> {

    private static final int THUMB_SIZE = 240;
    private static final LruCache<String, Bitmap> THUMB_CACHE =
            new LruCache<String, Bitmap>((int) (Runtime.getRuntime().maxMemory() / 1024 / 8)) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    return value.getByteCount() / 1024;
                }
            };
    private static final Set<SelfieAdapter> INSTANCES =
            Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final List<SelfieEntity> items = new ArrayList<>();
    private final Set<String> raffleLinkedIds = new HashSet<>();
    private final ExecutorService decodeExecutor = Executors.newFixedThreadPool(2);
    private final OnSelfieClick onClick;
    private File imagesDir;

    public SelfieAdapter(@NonNull OnSelfieClick onClick) {
        this.onClick = onClick;
        INSTANCES.add(this);
    }

    public static void shutdownAll() {
        for (SelfieAdapter adapter : INSTANCES) {
            adapter.shutdown();
        }
        INSTANCES.clear();
    }

    @Nullable
    public static Bitmap decodeThumb(@Nullable File file, int reqSize) {
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

    public void shutdown() {
        decodeExecutor.shutdownNow();
        INSTANCES.remove(this);
    }

    @SuppressLint("NotifyDataSetChanged")
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull SelfieHolder holder, int position) {
        SelfieEntity selfie = items.get(position);
        holder.number.setText("#" + selfie.number);
        holder.star.setVisibility(selfie.favorite ? View.VISIBLE : View.GONE);
        holder.raffle.setVisibility(raffleLinkedIds.contains(selfie.id) ? View.VISIBLE : View.GONE);
        bindThumb(holder.image, selfie);
        holder.itemView.setOnClickListener(v -> onClick.onClick(selfie));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private void bindThumb(ImageView image, SelfieEntity selfie) {
        String id = selfie.id;
        image.setTag(id);
        Bitmap cached = THUMB_CACHE.get(id);
        if (cached != null) {
            image.setImageBitmap(cached);
            return;
        }
        image.setImageBitmap(null);
        File file = new File(imagesDir, selfie.filename);
        decodeExecutor.execute(() -> {
            Bitmap bitmap = decodeThumb(file, THUMB_SIZE);
            if (bitmap == null) {
                return;
            }
            THUMB_CACHE.put(id, bitmap);
            image.post(() -> {
                if (id.equals(image.getTag())) {
                    image.setImageBitmap(bitmap);
                }
            });
        });
    }

    public interface OnSelfieClick {
        void onClick(SelfieEntity selfie);
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
