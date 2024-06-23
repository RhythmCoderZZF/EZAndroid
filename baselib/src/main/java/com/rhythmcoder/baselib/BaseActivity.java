package com.rhythmcoder.baselib;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.rhythmcoder.baselib.cmd.CmdUtil;
import com.rhythmcoder.baselib.utils.Constants;
import com.rhythmcoder.baselib.utils.LogUtil;

/**
 * Author:create by RhythmCoderZZF
 * Date:2023/12/16
 * Description:
 */
public class BaseActivity extends AppCompatActivity {
    protected static String TAG = "";
    private String mInfo;
    private Dialog mDialogInfo;

    {
        TAG = this.getClass().getSimpleName();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.d(TAG, "activity onCreate<<");
        ActionBar actionBar = getSupportActionBar();
        String label = getIntent().getStringExtra(Constants.INTENT_TITLE);
        mInfo = getIntent().getStringExtra(Constants.INTENT_INFO);
        if (!TextUtils.isEmpty(label)) {
            actionBar.setTitle(label);
        }
        mInfo = mInfo == null ? "" : mInfo;
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mInfo.isEmpty()) {
            menu.findItem(R.id.info).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int itemId = item.getItemId();
        if (itemId == R.id.info) {
            if (mDialogInfo == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                mDialogInfo = builder.setTitle(getString(R.string.info)).setIcon(ContextCompat.getDrawable(this, R.drawable.baseline_info_24)).setMessage(Html.fromHtml(mInfo, Html.FROM_HTML_MODE_COMPACT)).setPositiveButton(R.string.ok, (dialog, which) -> dialog.dismiss()).create();
            }
            mDialogInfo.show();
        } else if (itemId == android.R.id.home) {
            finish();
        } else if (itemId == R.id.cmd) {
            CmdUtil.connectCmdAndShowWindow(this);
        }
        return true;
    }

    protected void initSimpleProjectListView(RecyclerView rv, ListAdapter adapter) {
        rv.setLayoutManager(new StaggeredGridLayoutManager(2, RecyclerView.VERTICAL));
        rv.setAdapter(adapter);
    }

    protected void toast(String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BaseActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.d(TAG, "activity onResume<<");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.d(TAG, "activity onPause<<");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogUtil.d(TAG, "activity onNewIntent<<");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CmdUtil.disConnectCmd(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        CmdUtil.onActivityResult(requestCode);
    }
}
