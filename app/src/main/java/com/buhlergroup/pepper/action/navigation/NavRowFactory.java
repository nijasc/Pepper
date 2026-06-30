package com.buhlergroup.pepper.action.navigation;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.buhlergroup.pepper.R;

/**
 * Builds the programmatic list rows, action pills, and empty-state labels used by
 * {@link NavigationView}. Pure view construction/styling — holds no navigation state.
 * Click wiring is delegated back to the caller via {@link View.OnClickListener}.
 */
class NavRowFactory {

    private final Context context;

    NavRowFactory(Context context) {
        this.context = context;
    }

    LinearLayout row(String label) {
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rowParams.topMargin = dp(6);
        row.setLayoutParams(rowParams);

        TextView text = new TextView(context);
        text.setText(label);
        text.setTextColor(ContextCompat.getColor(context, R.color.white));
        text.setTextSize(18);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        text.setLayoutParams(textParams);
        row.addView(text);
        return row;
    }

    TextView pill(String text, int bgRes, View.OnClickListener onClick) {
        TextView pill = new TextView(context);
        pill.setText(text);
        pill.setTextColor(ContextCompat.getColor(context, R.color.white));
        pill.setTextSize(15);
        pill.setGravity(Gravity.CENTER);
        pill.setBackgroundResource(bgRes);
        pill.setPadding(dp(20), dp(10), dp(20), dp(10));
        pill.setClickable(true);
        pill.setFocusable(true);
        pill.setOnClickListener(onClick);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMarginStart(dp(8));
        pill.setLayoutParams(params);
        return pill;
    }

    TextView emptyLabel(int resId) {
        TextView text = new TextView(context);
        text.setText(resId);
        text.setTextColor(0xCCFFFFFF);
        text.setTextSize(16);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(6);
        text.setLayoutParams(params);
        return text;
    }

    private int dp(int value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }
}
