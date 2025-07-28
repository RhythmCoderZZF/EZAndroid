package com.rhythmcoderzzf.androidstudysystem.media.mediaselector;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rhythmcoderzzf.androidstudysystem.R;
import com.rhythmcoderzzf.androidstudysystem.databinding.ActivityMediaSelectorBinding;
import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.ezandroid.media.EZMediaPlayer;

import java.util.List;

public class MediaSelectorActivity extends BaseActivity<ActivityMediaSelectorBinding> implements View.OnClickListener {
    private EZMediaPlayer ezMediaPlayer;

    @Override
    protected ActivityMediaSelectorBinding inflateViewBinding(@NonNull LayoutInflater layoutInflater) {
        return ActivityMediaSelectorBinding.inflate(layoutInflater);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding.btnChoosePicture.setOnClickListener(this);
        mBinding.btnChooseVideo.setOnClickListener(this);
        ezMediaPlayer = new EZMediaPlayer.Builder(this).setListener(new EZMediaPlayer.MediaSelectionListener() {
            @Override
            protected void onImagesSelected(List<Bitmap> bitmaps) {
                if (bitmaps != null) {
                    for (int i = 0; i < bitmaps.size(); i++) {
                        if (i == 0) {
                            mBinding.img1.setImageBitmap(bitmaps.get(i));
                        } else if (i == 1) {
                            mBinding.img2.setImageBitmap(bitmaps.get(i));
                        } else if (i == 2) {
                            mBinding.img3.setImageBitmap(bitmaps.get(i));
                        }
                    }
                }
            }

            @Override
            protected void onVideoSelected(Uri videoUri) {

            }
        }).build();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_choose_picture:
                ezMediaPlayer.chooseImages(5);
                break;
            case R.id.btn_choose_video:
                ezMediaPlayer.chooseVideo();
                break;
        }
    }
}