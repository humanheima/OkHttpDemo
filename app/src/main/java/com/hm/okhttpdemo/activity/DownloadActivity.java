package com.hm.okhttpdemo.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.hm.okhttpdemo.R;
import com.hm.okhttpdemo.model.DownLoadObserver;
import com.hm.okhttpdemo.model.DownloadInfo;
import com.hm.okhttpdemo.utils.DownloadManager;
import com.hm.okhttpdemo.widget.HmProgressDialog;

import java.io.File;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 断点续传下载
 */
public class DownloadActivity extends AppCompatActivity {

    private static final String TAG = "DownloadActivity";
    private String wifiUrl = "http://140.207.247.205/imtt.dd.qq.com/16891/DF6B2FB4A4628C2870C710046C231348.apk?mkey=58d4b294acc7802a&f=8e5d&c=0&fsname=com.snda.wifilocating_4.1.88_3108.apk&csr=1bbd&p=.apk";
    private HmProgressDialog progressDialog;

    public static void launch(Context context) {
        Intent starter = new Intent(context, DownloadActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_start})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start:
                start();
                break;
            default:
                break;
        }
    }

    private void start() {
        progressDialog = HmProgressDialog.show(this, "下载", "正在下载，请稍后...");
        progressDialog.setCancelClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.dismiss();
                cancel();
            }
        });
        progressDialog.show();
        DownloadManager.getInstance()
                .downLoad(wifiUrl, new DownLoadObserver() {

                    @Override
                    public void onNext(DownloadInfo value) {
                        super.onNext(value);
                        progressDialog.setMax((int) value.getTotal());
                        progressDialog.setProgress((int) value.getProgress());
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        Log.e(TAG, e.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        if (downloadInfo != null) {
                            progressDialog.setMessage("下载完成");
                            installApk(new File(downloadInfo.getSaveLoaction()));
                        }
                    }
                });
    }

    private void cancel() {
        DownloadManager.getInstance().cancel(wifiUrl);
    }

    private void installApk(File file) {
        Log.e(TAG, file.getAbsolutePath());
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setType("application/vnd.android.package-archive");
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
