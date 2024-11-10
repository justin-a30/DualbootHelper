package com.david42069.dualboothelper;

import io.github.oneuiproject.sesl.preference.PreferenceFragment;
import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.DropDownPreference;
import androidx.preference.PreferenceManager;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.content.Context;
import com.topjohnwu.superuser.Shell;


public class PreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fragment, rootKey); // Link to your preferences XML file

        // Set up actions based on selection
        PreferenceManager.getDefaultSharedPreferences(requireContext())
                .registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
                    if ("slot_a_actions".equals(key) || "slot_b_actions".equals(key) || "misc_actions".equals(key)) {
                        String action = sharedPreferences.getString(key, "");
                        showConfirmationDialog(action);
                    }
                });
    }

    private void onPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("slot_a_actions".equals(key) || "slot_b_actions".equals(key) || "misc_actions".equals(key)) {
            String action = sharedPreferences.getString(key, "");
            showConfirmationDialog(action);
        }
    }

    private void showConfirmationDialog(String action) {
        Activity activity = requireActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        String title = getString(R.string.dialog_confirm_title);
        String message = getString(R.string.dialog_confirm);
        String positiveButton = getString(R.string.dialog_yes);
        String negativeButton = getString(R.string.dialog_no);

        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButton, (dialog, which) -> {
                    showLoadingDialog();
                    executeAction(action);
                })
                .setNegativeButton(negativeButton, null);

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showLoadingDialog() {
        Activity activity = requireActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);
        LayoutInflater inflater = activity.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.loading_dialog, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
    
    private void executeAction(String action) {
        String scriptFile = "";

        // Assign corresponding script based on action
        switch (action) {
            case "switcha":
                scriptFile = "R.raw.switcha";
                break;
            case "switchar":
                scriptFile = "R.raw.switchar";
                break;
            case "switchb":
                scriptFile = "R.raw.switchb";
                break;
            case "switchbr":
                scriptFile = "R.raw.switchbr";
                break;
            case "download":
                scriptFile = "R.raw.download";
                break;
            case "shuwdown":
                scriptFile = "R.raw.shutdown";
                break;
        }

        // Execute the shell command based on the selected action
        Shell.cmd(getResources().openRawResource(getResources().getIdentifier(scriptFile.replace("R.raw.", ""), "raw", getActivity().getPackageName()))).exec();
    }

}
