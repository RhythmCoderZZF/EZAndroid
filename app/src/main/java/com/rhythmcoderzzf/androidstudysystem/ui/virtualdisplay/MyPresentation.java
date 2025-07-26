package com.rhythmcoderzzf.androidstudysystem.ui.virtualdisplay;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;

import com.rhythmcoderzzf.androidstudysystem.R;

public class MyPresentation extends Presentation {
    public MyPresentation(Context outerContext, Display display) {
        super(outerContext, display);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_ui_virtual_display);
    }
}
