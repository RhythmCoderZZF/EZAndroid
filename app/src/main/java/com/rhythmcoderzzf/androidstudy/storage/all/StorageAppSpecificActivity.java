package com.rhythmcoderzzf.androidstudy.storage.all;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.rhythmcoderzzf.androidstudy.R;
import com.rhythmcoderzzf.baselib.BaseActivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

public class StorageAppSpecificActivity extends BaseActivity implements View.OnClickListener {
    private File mFile;
    private String mFileName = "AppSpecificInternalFile";

    private Button mBtnCreateFile;
    private Button mBtnStorageFile;
    private Button mBtnAccessFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_app_spcific);
        mBtnCreateFile = findViewById(R.id.btnCreateFile);
        mBtnCreateFile.setOnClickListener(this);
        mBtnStorageFile = findViewById(R.id.btnStoreFile);
        mBtnStorageFile.setOnClickListener(this);
        mBtnAccessFile = findViewById(R.id.btnAccessFile);
        mBtnAccessFile.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        final int i = v.getId();
        if (i == R.id.btnCreateFile) {
            File file = new File(getFilesDir(), mFileName);
            //get an array containing the names of all files within the filesDir directory by calling fileList()
            toast(getFilesDir().toString() + ":" + Arrays.stream(fileList()).collect(Collectors.joining()));
        } else if (i == R.id.btnStoreFile) {
            String filename = mFileName;
            String fileContents = "Hello world!";
            try (FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE)) {
                fos.write(fileContents.getBytes());
                toast("write content:" + fileContents);
            } catch (Exception e) {
                e.printStackTrace();
                toast(e.getMessage());
            }
        } else if (i == R.id.btnAccessFile) {
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(openFileInput(mFileName), StandardCharsets.UTF_8))) {
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line).append('\n');
                    line = reader.readLine();
                }
            } catch (Exception e) {
                e.printStackTrace();
                toast(e.getMessage());
            } finally {
                toast("read from file:" + stringBuilder);
            }
        }
    }
}