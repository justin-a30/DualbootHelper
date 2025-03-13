package com.david42069.dualboothelper;

// from OneUI Sample app. Credits to everyone who contributed in making the app.

import static androidx.appcompat.util.SeslRoundedCorner.ROUNDED_CORNER_TOP_LEFT;
import static androidx.appcompat.util.SeslRoundedCorner.ROUNDED_CORNER_TOP_RIGHT;

import static dev.oneuiproject.oneui.ktx.ViewKt.semSetRoundedCornerColor;
import static dev.oneuiproject.oneui.ktx.ViewKt.semSetRoundedCorners;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.AppBarLayout;
import com.david42069.dualboothelper.databinding.ActivityAboutBinding;
import com.david42069.dualboothelper.databinding.ActivityAboutContentBinding;

import dev.oneuiproject.oneui.utils.DeviceLayoutUtil;
import dev.oneuiproject.oneui.widget.Toast;
import dev.oneuiproject.oneui.widget.CardItemView;

public class AboutActivity extends AppCompatActivity
        implements View.OnClickListener {
    private long mLastClickTime;

    private ActivityAboutBinding mBinding;
    private ActivityAboutContentBinding mBottomContent;

    private AboutAppBarListener mAppBarListener = new AboutAppBarListener();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());


        if (Build.VERSION.SDK_INT >= 30 && !getWindow().getDecorView().getFitsSystemWindows()) {
            mBinding.getRoot().setOnApplyWindowInsetsListener((v, insets) -> {
                Insets systemBarsInsets = WindowInsetsCompat.toWindowInsetsCompat(insets)
                        .getInsets(WindowInsetsCompat.Type.systemBars());
                mBinding.getRoot().setPadding(systemBarsInsets.left, systemBarsInsets.top,
                        systemBarsInsets.right, systemBarsInsets.bottom);
                return insets;
            });
        }

        mBottomContent = mBinding.aboutBottomContent;

        setSupportActionBar(mBinding.aboutToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mBinding.aboutToolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        resetAppBar(getResources().getConfiguration());
        initContent();
        initOnBackPressed();
    }


    private OnBackPressedCallback mBackPressedCallback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            mBinding.aboutAppBar.setExpanded(true);
        }
    };

    private void initOnBackPressed() {
        getOnBackPressedDispatcher().addCallback(this, mBackPressedCallback);
        mBackPressedCallback.setEnabled(mBinding.aboutAppBar.seslIsCollapsed()
                && getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE
                && !isInMultiWindowMode());
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetAppBar(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sample3_menu_about, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_app_info) {
            Intent intent = new Intent(
                    "android.settings.APPLICATION_DETAILS_SETTINGS",
                    Uri.fromParts("package", BuildConfig.APPLICATION_ID, null));
            intent.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return false;
    }

    @Override
    public boolean isInMultiWindowMode() {
        return Build.VERSION.SDK_INT >= 24 && super.isInMultiWindowMode();
    }

    @SuppressLint("RestrictedApi")
    private void resetAppBar(Configuration config) {
        if (config.orientation != Configuration.ORIENTATION_LANDSCAPE
                && !isInMultiWindowMode() || DeviceLayoutUtil.INSTANCE.isTabletLayoutOrDesktop(this)) {
            mBinding.aboutAppBar.seslSetCustomHeightProportion(true, 0.5f);
            mBinding.aboutAppBar.addOnOffsetChangedListener(mAppBarListener);
            mBinding.aboutAppBar.setExpanded(true, false);
            mBackPressedCallback.setEnabled(true);
            mBinding.aboutSwipeUpContainer.setVisibility(View.VISIBLE);
            ViewGroup.LayoutParams lp = mBinding.aboutSwipeUpContainer.getLayoutParams();
            lp.height = getResources().getDisplayMetrics().heightPixels / 2;
        } else {
            mBinding.aboutAppBar.setExpanded(false, false);
            mBackPressedCallback.setEnabled(false);
            mBinding.aboutAppBar.seslSetCustomHeightProportion(true, 0);
            mBinding.aboutAppBar.removeOnOffsetChangedListener(mAppBarListener);
            mBinding.aboutBottomContainer.setAlpha(1f);
            setBottomContentEnabled(true);
            mBinding.aboutSwipeUpContainer.setVisibility(View.GONE);
        }
    }

    private void initContent() {
        semSetRoundedCorners(mBinding.aboutBottomContent.getRoot(), ROUNDED_CORNER_TOP_LEFT | ROUNDED_CORNER_TOP_RIGHT);
        semSetRoundedCornerColor(mBinding.aboutBottomContent.getRoot(),
                ROUNDED_CORNER_TOP_LEFT | ROUNDED_CORNER_TOP_RIGHT,
                ContextCompat.getColor(this, dev.oneuiproject.oneui.design.R.color.oui_round_and_bgcolor));

        Drawable appIcon = getDrawable(R.drawable.ic_launcher);
        mBinding.aboutHeaderAppIcon.setImageDrawable(appIcon);
        mBinding.aboutBottomAppIcon.setImageDrawable(appIcon);

        mBinding.aboutHeaderAppVersion.setText("Version " + BuildConfig.VERSION_NAME);
        mBinding.aboutBottomAppVersion.setText("Version " + BuildConfig.VERSION_NAME);

        mBinding.aboutHeaderGithub.setOnClickListener(this);
        TooltipCompat.setTooltipText(mBinding.aboutHeaderGithub, "GitHub");
        mBinding.aboutHeaderTelegram.setOnClickListener(this);
        TooltipCompat.setTooltipText(mBinding.aboutHeaderTelegram, "Telegram");

        mBottomContent.aboutBottomDevYann.setOnClickListener(this);
        mBottomContent.aboutBottomDevTribalfs.setOnClickListener(this);
        mBottomContent.aboutBottomDevSalvo.setOnClickListener(this);
        mBottomContent.aboutBottomDevBob.setOnClickListener(this);
        mBottomContent.aboutBottomDevDavid.setOnClickListener(this);

        mBottomContent.aboutBottomOssApache.setOnClickListener(this);
        mBottomContent.aboutBottomOssMit.setOnClickListener(this);

        mBottomContent.aboutTranslate.setOnClickListener(this);
        mBottomContent.aboutDonate.setOnClickListener(this);
        mBottomContent.aboutXda.setOnClickListener(this);
        mBottomContent.aboutBottomRelativeOuip.setOnClickListener(this);
    }

    private void setBottomContentEnabled(boolean enabled) {
        mBinding.aboutHeaderGithub.setEnabled(!enabled);
        mBinding.aboutHeaderTelegram.setEnabled(!enabled);
        mBottomContent.aboutBottomDevYann.setEnabled(enabled);
        mBottomContent.aboutBottomDevTribalfs.setEnabled(enabled);
        mBottomContent.aboutBottomDevSalvo.setEnabled(enabled);
        mBottomContent.aboutBottomOssApache.setEnabled(enabled);
        mBottomContent.aboutBottomOssMit.setEnabled(enabled);
        mBottomContent.aboutTranslate.setEnabled(enabled);
        mBottomContent.aboutDonate.setEnabled(enabled);
        mBottomContent.aboutXda.setEnabled(enabled);
        mBottomContent.aboutBottomRelativeOuip.setEnabled(enabled);
        mBottomContent.aboutBottomDevBob.setEnabled(enabled);
        mBottomContent.aboutBottomDevDavid.setEnabled(enabled);

    }

    @Override
    public void onClick(View v) {
        long uptimeMillis = SystemClock.uptimeMillis();
        if (uptimeMillis - mLastClickTime > 600L) {
            String url = null;
            if (v.getId() == mBinding.aboutHeaderGithub.getId()) {
                url = "https://github.com/justin-a30/DualbootHelper";
            } else if (v.getId() == mBinding.aboutHeaderTelegram.getId()) {
                url = "https://t.me/dualbootsamsung";
            } else if (v.getId() == mBottomContent.aboutBottomDevDavid.getId()) {
                url = "https://github.com/david-42069";
            } else if (v.getId() == mBottomContent.aboutBottomDevBob.getId()) {
                url = "https://github.com/justin-a30";
            } else if (v.getId() == mBottomContent.aboutBottomDevYann.getId()) {
                url = "https://github.com/Yanndroid";
            } else if (v.getId() == mBottomContent.aboutBottomDevTribalfs.getId()) {
                url = "https://github.com/tribalfs";
            } else if (v.getId() == mBottomContent.aboutBottomDevSalvo.getId()) {
                url = "https://github.com/salvogiangri";
            } else if (v.getId() == mBottomContent.aboutBottomGnu.getId()) {
                url = "https://www.gnu.org/licenses/licenses.html";
            } else if (v.getId() == mBottomContent.aboutBottomOssApache.getId()) {
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt";
            } else if (v.getId() == mBottomContent.aboutBottomOssMit.getId()) {
                url = "https://github.com/OneUIProject/sesl/blob/main/LICENSE";
            } else if (v.getId() == mBottomContent.aboutDonate.getId()) {
                url = "https://www.paypal.com/donate/?hosted_button_id=3DTX9DHNH2WYC";
            } else if (v.getId() == mBottomContent.aboutXda.getId()) {
                url = "https://xdaforums.com/t/mod-dualboot-for-any-samsung.4680492";
            } else if (v.getId() == mBottomContent.aboutTranslate.getId()) {
                url = "https://crowdin.com/project/dualboothelpe";
            } else if (v.getId() == mBottomContent.aboutBottomRelativeOuip.getId()) {
                url = "https://github.com/OneUIProject";
            }

            if (url != null) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(
                            this, "No suitable activity found", Toast.LENGTH_SHORT).show();
                }
            }
        }
        mLastClickTime = uptimeMillis;
    }

    private class AboutAppBarListener implements AppBarLayout.OnOffsetChangedListener {
        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            // Handle the SwipeUp anim view
            final int totalScrollRange = appBarLayout.getTotalScrollRange();
            final int abs = Math.abs(verticalOffset);

            if (abs >= totalScrollRange / 2) {
                mBinding.aboutSwipeUpContainer.setAlpha(0f);
                setBottomContentEnabled(true);
            } else if (abs == 0) {
                mBinding.aboutSwipeUpContainer.setAlpha(1f);
                setBottomContentEnabled(false);
            } else {
                float offsetAlpha = (appBarLayout.getY() / totalScrollRange);
                float arrowAlpha = 1 - (offsetAlpha * -3);
                if (arrowAlpha < 0) {
                    arrowAlpha = 0;
                } else if (arrowAlpha > 1) {
                    arrowAlpha = 1;
                }
                mBinding.aboutSwipeUpContainer.setAlpha(arrowAlpha);
            }

            // Handle the bottom part of the UI
            final float alphaRange = mBinding.aboutCtl.getHeight() * 0.143f;
            final float layoutPosition = Math.abs(appBarLayout.getTop());
            float bottomAlpha = (150.0f / alphaRange)
                    * (layoutPosition - (mBinding.aboutCtl.getHeight() * 0.35f));

            if (bottomAlpha < 0) {
                bottomAlpha = 0;
            } else if (bottomAlpha >= 255) {
                bottomAlpha = 255;
            }

            mBinding.aboutBottomContainer.setAlpha(bottomAlpha / 255);

            mBackPressedCallback.setEnabled(appBarLayout.getTotalScrollRange() + verticalOffset == 0);
        }
    }
}