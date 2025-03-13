package com.david42069.dualboothelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;

import com.topjohnwu.superuser.Shell;

import dev.oneuiproject.oneui.dialog.ProgressDialog;
import dev.oneuiproject.oneui.layout.ToolbarLayout;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {

    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static Handler mainHandler = new Handler(Looper.getMainLooper());

    private ProgressDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ToolbarLayout toolbarLayout = findViewById(R.id.settings);
        toolbarLayout.setNavigationButtonAsBack();
        // I suspect that you forget to add this
        Shell.getShell(shell -> {});

        mLoadingDialog = new ProgressDialog(this);
        mLoadingDialog.setProgressStyle(ProgressDialog.STYLE_CIRCLE);
        mLoadingDialog.setCancelable(false);

        // Load the preferences fragment
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        private SharedPreferences sharedPreferences;
        private Context mContext;

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            mContext = getContext();
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            getView().setBackgroundColor(getResources().getColor(R.color.oui_background_color, mContext.getTheme()));
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences_activity, rootKey);

            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
            registerPreferenceChangeListener();

            // Find the SwitchPreferenceCompat by key
            SwitchPreferenceCompat twrpSwitchPreference = findPreference("twrp_theme");
            SwitchPreferenceCompat editTextSwitchPreference = findPreference("customizeslotname");
            EditTextPreference slotAPreference = findPreference("slotakey");
            EditTextPreference slotBPreference = findPreference("slotbkey");

            if (twrpSwitchPreference != null) {
                // Check for root access before enabling the switch
                if (!MainActivity.RootChecker.isRootAvailable()) {
                    Log.e("SettingsFragment", "Root access not found. Disabling theme switch.");
                    twrpSwitchPreference.setEnabled(false); // Grey out the switch
                    twrpSwitchPreference.setSummary(getString(R.string.sudo_access)); // Optional: Inform user
                } else {
                    Log.i("SettingsFragment", "Root access available. Enabling theme switch.");
                    twrpSwitchPreference.setEnabled(true);
                    twrpSwitchPreference.setSummary(getString(R.string.twrp_desc)); // Clear any disabled summary if previously set
                }

                // Set the onPreferenceChangeListener
                twrpSwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean isEnabled = (boolean) newValue;

                    // Show loading dialog
                    ((SettingsActivity) requireActivity()).mLoadingDialog.show();

                    if (isEnabled) {
                        executorService.execute(() -> {
                            try {
                                // Run the shell command in a background thread
                                Shell.Result result = Shell.cmd(getResources().openRawResource(R.raw.twrpon)).exec();
                                if (!result.isSuccess()) {
                                    Log.e("MainActivity", "Error executing shell command");
                                } else {
                                    Log.i("MainActivity", "Shell command executed successfully");
                                }
                            } catch (Exception e) {
                                Log.e("MainActivity", "Error executing shell command", e);
                            } finally {
                                // Dismiss loading dialog on the main thread
                                mainHandler.post(() -> ((SettingsActivity) requireActivity()).mLoadingDialog.dismiss());
                            }
                        });
                    } else {
                        File folder = new File("/sdcard/TWRP/theme");
                        if (folder.exists()) {
                            executorService.execute(() -> {
                                try {
                                    // Run the shell command in a background thread
                                    Shell.cmd("rm -rf /sdcard/TWRP/theme").exec();
                                } catch (Exception e) {
                                    Log.e("MainActivity", "Error executing shell command", e);
                                } finally {
                                    // Dismiss loading dialog on the main thread
                                    mainHandler.post(() -> ((SettingsActivity) requireActivity()).mLoadingDialog.dismiss());
                                }
                            });
                        } else {
                            Log.e("ShellCommand", "Directory does not exist.");
                            // Dismiss loading dialog on the main thread
                            mainHandler.post(() -> ((SettingsActivity) requireActivity()).mLoadingDialog.dismiss());
                        }
                    }

                    return true;
                });
            }

            // Switch stuff
            if (editTextSwitchPreference != null) {
                boolean isEditTextSwitchOn = editTextSwitchPreference.isChecked();
                updateEditTextPreferences(isEditTextSwitchOn, slotAPreference, slotBPreference);

                editTextSwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    boolean isEditTextEnabled = (boolean) newValue;
                    updateEditTextPreferences(isEditTextEnabled, slotAPreference, slotBPreference);
                    if (!isEditTextEnabled) {
                        clearEditTextPreferences(slotAPreference, slotBPreference);
                    }
                    return true;
                });
            }
        }

        private void updateEditTextPreferences(boolean isEditTextEnabled, EditTextPreference slotAPreference, EditTextPreference slotBPreference) {
            if (slotAPreference != null) {
                slotAPreference.setEnabled(isEditTextEnabled);
            }
            if (slotBPreference != null) {
                slotBPreference.setEnabled(isEditTextEnabled);
            }
        }

        private void clearEditTextPreferences(EditTextPreference slotAPreference, EditTextPreference slotBPreference) {
            if (slotAPreference != null) {
                sharedPreferences.edit().remove(slotAPreference.getKey()).apply();
            }
            if (slotBPreference != null) {
                sharedPreferences.edit().remove(slotBPreference.getKey()).apply();
            }
        }

        private void registerPreferenceChangeListener() {
            sharedPreferences.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
                if ("slotakey".equals(key)) {
                    handlePreferenceChange("slotakey");
                } else if ("slotbkey".equals(key)) {
                    handlePreferenceChange("slotbkey");
                }
            });
        }

        private void handlePreferenceChange(String key) {
            String value = sharedPreferences.getString(key, "");
            if (value == null || value.trim().isEmpty()) {
                sharedPreferences.edit().remove(key).apply();
            }
        }
    }
}