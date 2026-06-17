package com.buhlergroup.pepper.action.admin;

import android.view.View;
import android.widget.TextView;

import com.buhlergroup.pepper.R;
import com.buhlergroup.pepper.lang.SupportedLanguage;

final class LanguagePanelController {

    private final PanelNavigator panelNav;
    private final TextView langCurrent;

    LanguagePanelController(View root, PanelNavigator panelNav) {
        this.panelNav = panelNav;
        this.langCurrent = root.findViewById(R.id.adminLangCurrent);
        root.findViewById(R.id.adminLangDe).setOnClickListener(v -> setLanguage(SupportedLanguage.GERMAN));
        root.findViewById(R.id.adminLangEn).setOnClickListener(v -> setLanguage(SupportedLanguage.ENGLISH));
        root.findViewById(R.id.adminLangIt).setOnClickListener(v -> setLanguage(SupportedLanguage.ITALIAN));
        root.findViewById(R.id.adminLangEs).setOnClickListener(v -> setLanguage(SupportedLanguage.SPANISH));
        root.findViewById(R.id.adminLangFr).setOnClickListener(v -> setLanguage(SupportedLanguage.FRENCH));
    }

    void showLanguage() {
        updateLanguageLabel();
        panelNav.show(PanelNavigator.PANEL_LANG);
    }

    private void setLanguage(SupportedLanguage language) {
        AdminController.get().setLanguage(language);
        updateLanguageLabel();
    }

    private void updateLanguageLabel() {
        SupportedLanguage current = AdminController.get().getCurrentLanguage();
        langCurrent.setText(current != null ? current.getDisplayName() : "");
    }
}
