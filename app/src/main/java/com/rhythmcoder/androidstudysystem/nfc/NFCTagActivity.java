package com.rhythmcoder.androidstudysystem.nfc;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.widget.TextView;

import com.rhythmcoder.baselib.BaseActivity;
import com.rhythmcoder.baselib.utils.LogUtil;
import com.rhythmcoder.androidstudysystem.R;

import java.util.Arrays;

public class NFCTagActivity extends BaseActivity {
    private NfcAdapter mAdapter;
    private PendingIntent mPendingIntent;
    private IntentFilter[] mIntentFilters;
    private String[][] mTechList;
    private TextView mTvTagRes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_nfc);
        if (!nfcCheck()) {
            finish();
        }
        mTvTagRes = findViewById(R.id.tvTagRes);
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        //intentFilter过滤----ndef
        IntentFilter ndefFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndefFilter.addDataType("*/*");
        } catch (Exception e) {
            e.printStackTrace();
        }
        //intentFilter过滤----非ndef
        IntentFilter techFilter = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        //intentFilter过滤器列表
        mTechList = new String[][]{new String[]{NfcA.class.getName()}, new String[]{NfcB.class.getName()}, new String[]{NfcF.class.getName()}, new String[]{NfcV.class.getName()}, new String[]{Ndef.class.getName()}, new String[]{NdefFormatable.class.getName()}, new String[]{IsoDep.class.getName()}, new String[]{MifareClassic.class.getName()}, new String[]{MifareUltralight.class.getName()},};
        mIntentFilters = new IntentFilter[]{ndefFilter, techFilter};
    }

    @Override
    protected void onResume() {
        super.onResume();
        //开启前台调度,当该Activity在前台时，识别到的NFC TAG直接传递过来
        mAdapter.enableForegroundDispatch(this, mPendingIntent, mIntentFilters, mTechList);
        try {
            String action = getIntent().getAction();
            LogUtil.d(TAG, "action: " + action);
            //判断NFC Tag属于哪种类型
            if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
                readNdef();
            } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action) || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
                Tag tag = getIntent().getParcelableExtra(NfcAdapter.EXTRA_TAG);
                LogUtil.d(TAG, "tech|tag tag: " + tag);
                mTvTagRes.setText(readTechTag(tag));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //关闭前台调度
        mAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //获取到NFC标签调度系统封装的Tag信息
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
        LogUtil.d(TAG, "nfc feature check ok...");
        return true;
    }

    /**
     * 读取属于Ndef Tag的信息
     *
     * @return
     */
    private String readNdef() {
        Parcelable[] rawMsgs = getIntent().getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        // 解析NDEF消息
        NdefMessage[] msgs = null;
        if (rawMsgs != null) {
            msgs = new NdefMessage[rawMsgs.length];
            for (int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
            }
        }
        // 处理NDEF消息
        if (msgs != null && msgs.length > 0) {
            // 读取NDEF消息内容
            return new String(msgs[0].getRecords()[0].getPayload());
        }
        return null;
    }

    /**
     * 读取属于Tech的Tag信息
     *
     * @param tag
     * @return
     */
    private String readTechTag(Tag tag) {
        String nfcA = readNfcA(tag);
        if (nfcA != null) {
            return nfcA;
        }
        String mifareClassic = readMifareClassic(tag);
        if (mifareClassic != null) {
            return mifareClassic;
        }
        return null;
    }

    private String readNfcA(Tag tag) {
        NfcA nfcA = NfcA.get(tag);
        LogUtil.d(TAG, "readNfcA<< nfcA:" + nfcA);
        try {
            nfcA.connect();
            byte[] command = new byte[]{(byte) 0x00, (byte) 0xB0, (byte) 0x00, (byte) 0x00, (byte) 0x00};
            byte[] result = nfcA.transceive(command);
            LogUtil.d(TAG, "rawData:" + Arrays.toString(result));
            return new String(result);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG, e.getMessage());
        } finally {
            try {
                nfcA.close();
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.e(TAG, e.getMessage());
            }
        }
        return null;
    }

    private String readMifareClassic(Tag tag) {
        MifareClassic mifareClassic = MifareClassic.get(tag);
        LogUtil.d(TAG, "readNfcA<< mifareClassic:" + mifareClassic);
        try {
            mifareClassic.connect();
            boolean isAuthenticated = mifareClassic.authenticateSectorWithKeyA(0, MifareClassic.KEY_DEFAULT);
            // 读取数据块
            byte[] data = mifareClassic.readBlock(0);
            LogUtil.d(TAG, "readMifareClassic<< rawData:" + Arrays.toString(data));
            return new String(data);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG, e.getMessage());
        } finally {
            try {
                mifareClassic.close();
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.e(TAG, e.getMessage());
            }
        }
        return null;
    }
}