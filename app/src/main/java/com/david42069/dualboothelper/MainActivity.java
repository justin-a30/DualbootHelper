package com.david42069.dualboothelper;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import dev.oneuiproject.oneui.layout.ToolbarLayout;
import dev.oneuiproject.oneui.utils.ActivityUtils;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.topjohnwu.superuser.Shell;

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileReader;
import android.view.View;
import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import androidx.appcompat.app.AlertDialog;

import android.os.CountDownTimer;
import android.view.LayoutInflater;

import dev.oneuiproject.oneui.widget.CardView;

public class MainActivity extends AppCompatActivity {

    private static String getStatusFilePath(Context context) {
        return new File(context.getFilesDir(), "status.txt").getPath();
    }

    private static String getSlotAFilePath(Context context) {
        return new File(context.getFilesDir(), "slota.txt").getPath();
    }

    private static String getSlotBFilePath(Context context) {
        return new File(context.getFilesDir(), "slotb.txt").getPath();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Shell.getShell(shell -> {});
        deleteFilesIfExist();
        cp(R.raw.parted, "parted");
        cp(R.raw.jq, "jq");
        ToolbarLayout toolbarLayout = findViewById(R.id.home);
        updateStatusCardView();
        updateSlotCardView(R.id.slota_txt, getSlotAFilePath(this));
        updateSlotCardView(R.id.slotb_txt, getSlotBFilePath(this));

        setupCardViewWithConfirmation(R.id.reboot_a, R.string.reboot_a, "R.raw.switcha");
        setupCardViewWithConfirmation(R.id.reboot_b, R.string.reboot_b, "R.raw.switchb");
        setupCardViewWithConfirmation(R.id.rec_a, R.string.recovery_a, "R.raw.switchar");
        setupCardViewWithConfirmation(R.id.rec_b, R.string.recovery_b, "R.raw.switchbr");
        setupCardViewWithConfirmation(R.id.bootloader, R.string.dl_mode, "R.raw.download");
        setupCardViewWithConfirmation(R.id.poweroff, R.string.poweroff, "R.raw.shutdown");
    }

    private void deleteFilesIfExist() {
        // Define the file paths using the current context
        String[] filePaths = {
                getStatusFilePath(this),
                getSlotAFilePath(this),
                getSlotBFilePath(this)
        };

        for (String path : filePaths) {
            File file = new File(path);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    Log.d("MainActivity", "Deleted file: " + path);
                } else {
                    Log.e("MainActivity", "Failed to delete file: " + path);
                }
            }
        }
    }

    private void cp(int resourceId, String fileName) {
        try (InputStream in = getResources().openRawResource(resourceId);
             OutputStream out = new FileOutputStream(new File(getFilesDir(), fileName))) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }
        } catch (IOException e) {
            Log.e("FileCopyError", "Error copying file " + fileName, e);
        }
    }

    private void updateStatusCardView() {
        Shell.cmd(getResources().openRawResource(R.raw.updatedata)).exec();
        File statusFile = new File(getStatusFilePath(this));
        String textToDisplay;

        if (statusFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(statusFile))) {
                StringBuilder statusText = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    line = line.replace("##NOT_INSTALLED##", getString(R.string.not_installed))
                            .replace("##INSTALLED_V5##", getString(R.string.installed_v5))
                            .replace("##INSTALLED_V4##", getString(R.string.installed_v4))
                            .replace("##UNAVAILABLE##", getString(R.string.unavailable))
                            .replace("##SUPER_PARTITION##", getString(R.string.super_partition))
                            .replace("##NORMAL_NAMING##", getString(R.string.normal_naming))
                            .replace("##CAPS_NAMING##", getString(R.string.caps_naming))
                            .replace("##UFS_SDA##", getString(R.string.ufs_sda))
                            .replace("##EMMC_SDC##", getString(R.string.emmc_sdc))
                            .replace("##EMMC_MMCBLK0##", getString(R.string.emmc_mmcblk0));

                    // Format each line as a list item with a bullet point
                    if (!line.trim().isEmpty()) {
                        statusText.append("- ").append(line.trim()).append("\n");
                    }
                }

                // Remove any trailing newline, if present
                if (statusText.length() > 0 && statusText.charAt(statusText.length() - 1) == '\n') {
                    statusText.setLength(statusText.length() - 1);
                }

                textToDisplay = statusText.toString().trim().isEmpty() ? getString(R.string.sudo_access) : statusText.toString();
            } catch (IOException e) {
                Log.e("MainActivity", "Error reading status.txt", e);
                textToDisplay = getString(R.string.sudo_access);  // Placeholder if reading fails
            }
        } else {
            textToDisplay = getString(R.string.sudo_access);  // Placeholder if file does not exist
        }

        CardView statusCardView = findViewById(R.id.status);
        statusCardView.setSummaryText(textToDisplay);
    }

    private void updateSlotCardView(int cardViewId, String filePath) {
        File slotFile = new File(filePath);
        String textToDisplay;

        if (slotFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(slotFile))) {
                StringBuilder slotText = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    slotText.append(line).append(" ");
                }

                textToDisplay = slotText.toString().trim().isEmpty() ? getString(R.string.unavailable) : slotText.toString();
            } catch (IOException e) {
                Log.e("MainActivity", "Error reading " + filePath, e);
                textToDisplay = getString(R.string.unavailable);  // Placeholder if reading fails
            }
        } else {
            textToDisplay = getString(R.string.unavailable);  // Placeholder if file does not exist
        }

        CardView slotCardView = findViewById(cardViewId);
        slotCardView.setSummaryText(textToDisplay);
    }

    private void setupCardViewWithConfirmation(int cardViewId, int promptResId, String scriptFile) {
        CardView cardView = findViewById(cardViewId);
        cardView.setOnClickListener(v -> showConfirmationDialog(promptResId, scriptFile));
    }

    public void executescriptstr(String scriptFile){
        new CountDownTimer(100,100){
            @Override
            public void onTick(long p1){

            }
            @Override
            public void onFinish(){
                executeShellCommand(scriptFile);
            }
        }.start();
    }
    
    private void showConfirmationDialog(int promptResId, String scriptFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String title = getString(promptResId) + "?";
        String message = getString(R.string.dialog_confirm);
        String positiveButton = getString(R.string.dialog_yes);
        String negativeButton = getString(R.string.dialog_no);

        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton(positiveButton, (dialog, which) -> {
                    showLoadingDialog();
                    executescriptstr(scriptFile);
                })
                .setNegativeButton(negativeButton, null);

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.loading_dialog, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private void executeShellCommand(String scriptFile) {
        executorService.execute(() -> {
            try {
                // Run the shell command in a background thread
                Shell.cmd(getResources().openRawResource(getResources().getIdentifier(scriptFile.replace("R.raw.", ""), "raw", getPackageName()))).exec();
            } catch (Exception e) {
                Log.e("MainActivity", "Error executing shell command", e);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_app_info) {
            ActivityUtils.startPopOverActivity(this,
                    new Intent(this, com.david42069.dualboothelper.AboutActivity.class),
                    null,
                    ActivityUtils.POP_OVER_POSITION_RIGHT | ActivityUtils.POP_OVER_POSITION_TOP);
            return true;
        }
        return false;
    }
}