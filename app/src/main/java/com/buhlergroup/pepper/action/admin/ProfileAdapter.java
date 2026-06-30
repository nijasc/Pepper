package com.buhlergroup.pepper.action.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.action.profile.data.ProfileEntity;

import java.util.ArrayList;
import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileHolder> {

    private final List<ProfileEntity> items = new ArrayList<>();
    private final OnProfileClick onClick;
    private String activeId = "";

    public ProfileAdapter(@NonNull OnProfileClick onClick) {
        this.onClick = onClick;
    }

    public void setData(List<ProfileEntity> data, @Nullable String activeId) {
        this.activeId = activeId == null ? "" : activeId;
        items.clear();
        items.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProfileHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_profile, parent, false);
        return new ProfileHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileHolder holder, int position) {
        ProfileEntity profile = items.get(position);
        holder.name.setText(profile.name);
        holder.sub.setText(profile.builtin
                ? holder.itemView.getContext().getString(R.string.profile_builtin_label)
                : "");
        holder.active.setVisibility(profile.id.equals(activeId) ? View.VISIBLE : View.GONE);
        holder.itemView.setOnClickListener(v -> onClick.onClick(profile));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnProfileClick {
        void onClick(ProfileEntity profile);
    }

    static class ProfileHolder extends RecyclerView.ViewHolder {
        final TextView name;
        final TextView sub;
        final TextView active;

        ProfileHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.itemProfileName);
            sub = itemView.findViewById(R.id.itemProfileSub);
            active = itemView.findViewById(R.id.itemProfileActive);
        }
    }
}
