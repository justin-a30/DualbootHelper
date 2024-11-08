package com.david42069.dualboothelper.fragment;

// from OneUI Sample app. Credits to everyone who contributed in making the app.

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SeslSeekBar;
import androidx.fragment.app.FragmentActivity;

import com.david42069.dualboothelper.R;

import dev.oneuiproject.oneui.layout.DrawerLayout;
import dev.oneuiproject.oneui.utils.SeekBarUtils;
import dev.oneuiproject.oneui.widget.HapticSeekBar;
import com.david42069.dualboothelper.base.BaseFragment;

public class SeekBarFragment extends BaseFragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        HapticSeekBar seekBar_1 = view.findViewById(R.id.fragment_seekbar_1);
        SeekBarUtils.showTickMark(seekBar_1, true);

        SeslSeekBar seekBar_2 = view.findViewById(R.id.fragment_seekbar_2);
        seekBar_2.setOverlapPointForDualColor(70);
        SeekBarUtils.showOverlapPreview(seekBar_2, true);
    }

    @Override
    public int getLayoutResId() {
        return R.layout.sample3_fragment_seek_bar;
    }

    @Override
    public int getIconResId() {
        return R.drawable.drawer_page_icon_seekbar;
    }

    @Override
    public CharSequence getTitle() {
        return "SeekBar";
    }

    @Override
    public boolean isAppBarEnabled() {
        return false;
    }

}
