package com.hm.okhttpdemo.widget;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hm.okhttpdemo.R;

/**
 * Created by dumingwei on 2017/3/27.
 */
public class HmProgressDialog extends ProgressDialog {

    private ProgressBar progressBar;
    private TextView textTitle;
    private TextView textMessage;
    private TextView textPercent;
    private TextView textCancel;
    private String title;
    private String message;
    private int max;
    private int progress;

    private HmProgressDialog(Context context, String title, String message) {
        this(context, 0);
        this.title = title;
        this.message = message;
    }

    public HmProgressDialog(Context context, int theme) {
        super(context, theme);
    }

    public static HmProgressDialog show(Context context, String title, String message) {
        HmProgressDialog dialog = new HmProgressDialog(context, title, message);
        dialog.show();
        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.hm_progress_dialog, null);
        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        textTitle = (TextView) view.findViewById(R.id.text_title);
        textMessage = (TextView) view.findViewById(R.id.text_message);
        textPercent = (TextView) view.findViewById(R.id.text_percent);
        textCancel = (TextView) view.findViewById(R.id.text_cancel);
        if (!TextUtils.isEmpty(title)) {
            textTitle.setText(title);
        }
        if (!TextUtils.isEmpty(message)) {
            textMessage.setText(message);
        }
        setContentView(view);
    }

    public void setCancelClickListener(View.OnClickListener cancelClickListener) {
        textCancel.setOnClickListener(cancelClickListener);
    }

    public void setMax(int max) {
        this.max = max;
        progressBar.setMax(max);
    }

    public void setProgress(int progress) {
        this.progress = progress;
        progressBar.setProgress(progress);
        textPercent.setText(String.format("%d%%", progress * 100 / max));
    }

    public void setTitle(String title) {
        this.title = title;
        textTitle.setText(title);
    }

    public void setMessage(String message) {
        this.message = message;
        textMessage.setText(message);
    }
}
