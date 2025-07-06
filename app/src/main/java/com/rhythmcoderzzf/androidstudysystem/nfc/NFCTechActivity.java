package com.rhythmcoderzzf.androidstudysystem.nfc;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import com.rhythmcoderzzf.baselib.BaseActivity;
import com.rhythmcoderzzf.baselib.utils.LogUtil;
import com.rhythmcoderzzf.androidstudysystem.R;

public class NFCTechActivity extends BaseActivity {
    private NfcAdapter mAdapter;
    private TextView mTvTagRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_nfc);
        mTvTagRes = findViewById(R.id.tvTagRes);
        if (!nfcCheck()) {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            final Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
            mTvTagRes.setText(tag.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private boolean nfcCheck() {
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mAdapter == null) {
            toast("device do not support NFC!");
            return false;
        } else {
            if (!mAdapter.isEnabled()) {
                startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
                return false;
            }
        }
        LogUtil.d(TAG, "nfcCheck<< check ok...");
        return true;
    }
}