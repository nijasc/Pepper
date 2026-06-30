package com.buhlergroup.pepper.action.admin;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.buhlergroup.pepper.R;

import java.util.List;

/**
 * Stateless builders for the admin panels' hand-rolled view toolkit
 * (cards, row params, styled spinners/adapters, field labels, dp/color helpers).
 *
 * <p>Extracted from {@code ModelPanelController} so sibling admin controllers
 * can share the same styling. All methods are stateless and take the
 * {@link Context} they need.
 */
final class AdminViewFactory {

    private AdminViewFactory() {
    }

    static <T> ArrayAdapter<T> whiteAdapter(Context ctx, List<T> items) {
        ArrayAdapter<T> adapter = new ArrayAdapter<T>(
                ctx, android.R.layout.simple_spinner_item, items) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                view.setTextColor(color(ctx, R.color.text_primary));
                view.setTextSize(16);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                view.setTextColor(color(ctx, R.color.text_primary));
                view.setTextSize(16);
                view.setPadding(dp(ctx, 16), dp(ctx, 14), dp(ctx, 16), dp(ctx, 14));
                view.setBackgroundColor(color(ctx, R.color.admin_card));
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        return adapter;
    }

    static Spinner styledSpinner(Context ctx) {
        Spinner spinner = new Spinner(ctx);
        spinner.setPopupBackgroundDrawable(new ColorDrawable(color(ctx, R.color.admin_card)));
        return spinner;
    }

    static TextView fieldLabel(Context ctx, int textRes) {
        TextView label = new TextView(ctx);
        label.setText(textRes);
        label.setTextColor(color(ctx, R.color.text_secondary));
        label.setTextSize(12);
        label.setAllCaps(true);
        LinearLayout.LayoutParams params = rowParams();
        params.topMargin = dp(ctx, 10);
        label.setLayoutParams(params);
        return label;
    }

    static LinearLayout card(Context ctx) {
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bg_admin_card));
        card.setPadding(dp(ctx, 16), dp(ctx, 14), dp(ctx, 16), dp(ctx, 16));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dp(ctx, 12);
        card.setLayoutParams(params);
        return card;
    }

    static LinearLayout.LayoutParams rowParams() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    static int color(Context ctx, int colorRes) {
        return ContextCompat.getColor(ctx, colorRes);
    }

    static int dp(Context ctx, int value) {
        float density = ctx.getResources().getDisplayMetrics().density;
        return Math.round(value * density);
    }
}
