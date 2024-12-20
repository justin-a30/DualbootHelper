package com.david42069.dualboothelper;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.preference.EditTextPreference;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreferenceCompat;
import com.topjohnwu.superuser.Shell;

import dev.oneuiproject.oneui.dialog.ProgressDialog;
import dev.oneuiproject.oneui.layout.ToolbarLayout;

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
            SwitchPreferenceCompat switchPreference = findPreference("twrp_theme");

            if (switchPreference != null) {
                // Check for root access before enabling the switch
                if (!MainActivity.RootChecker.isRootAvailable()) {
                    Log.e("SettingsFragment", "Root access not found. Disabling theme switch.");
                    switchPreference.setEnabled(false); // Grey out the switch
                    switchPreference.setSummary(getString(R.string.sudo_access)); // Optional: Inform user
                } else {
                    Log.i("SettingsFragment", "Root access available. Enabling theme switch.");
                    switchPreference.setEnabled(true);
                    switchPreference.setSummary(getString(R.string.twrp_desc)); // Clear any disabled summary if previously set
                }

                // Set the onPreferenceChangeListener
                switchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
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
        }

        private void registerPreferenceChangeListener() {
            sharedPreferences.registerOnSharedPreferenceChangeListener((sharedPreferences, key) -> {
                if ("slotakey".equals(key)) {
                    handlePreferenceChange("slotakey", "slota.txt");
                } else if ("slotbkey".equals(key)) {
                    handlePreferenceChange("slotbkey", "slotb.txt");
                }
            });
        }

        private void handlePreferenceChange(String key, String fileName) {
            String value = sharedPreferences.getString(key, "");
            if (value == null || value.trim().isEmpty()) {
                // Reset to the default value from the file or fallback
                String defaultValue = readValueFromFile(fileName);
                if (defaultValue == null || defaultValue.isEmpty()) {
                    defaultValue = getString(R.string.unavailable);
                }
                sharedPreferences.edit().putString(key, defaultValue).apply();
            }
        }

        private String readValueFromFile(String fileName) {
            File file = new File(requireContext().getFilesDir(), fileName);
            if (file.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    return reader.readLine();
                } catch (IOException e) {
                    Log.e("SettingsFragment", "Error reading file: " + fileName, e);
                }
            }
            return null;
        }
    }
}
