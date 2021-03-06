package com.hm.okhttpdemo.utils;

import android.os.Environment;
import android.util.Log;

import com.hm.okhttpdemo.App;
import com.hm.okhttpdemo.model.DownLoadObserver;
import com.hm.okhttpdemo.model.DownloadInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by dumingwei on 2017/3/23.
 */
public class DownloadManager {

    private static final String TAG = "DownloadManager";
    private static volatile DownloadManager instance;
    private HashMap<String, Call> downCalls;
    private OkHttpClient mClient;
    private File file;

    private DownloadManager() {
        downCalls = new HashMap<>();
        mClient = new OkHttpClient
                .Builder()
                .build();
    }

    public static DownloadManager getInstance() {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager();
                }
            }
        }
        return instance;
    }

    public void downLoad(String url, DownLoadObserver downLoadObserver) {
        Observable.just(url)
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(String s) throws Exception {
                        return !downCalls.containsKey(s);
                    }
                })
                .flatMap(new Function<String, ObservableSource<DownloadInfo>>() {
                    @Override
                    public ObservableSource<DownloadInfo> apply(String s) throws Exception {
                        return Observable.just(createDownloadInfo(s));
                    }
                })
                .map(new Function<DownloadInfo, DownloadInfo>() {
                    @Override
                    public DownloadInfo apply(DownloadInfo downloadInfo) throws Exception {
                        return getRealFileName(downloadInfo);
                    }
                })
                .flatMap(new Function<DownloadInfo, ObservableSource<DownloadInfo>>() {
                    @Override
                    public ObservableSource<DownloadInfo> apply(DownloadInfo downloadInfo) throws Exception {
                        return Observable.create(new DownloadSubscriber(downloadInfo));
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(downLoadObserver);
    }

    public void cancel(String url) {
        Call call = downCalls.get(url);
        if (call != null) {
            call.cancel();
        }
        downCalls.remove(url);
    }

    private DownloadInfo createDownloadInfo(String url) {
        DownloadInfo downloadInfo = new DownloadInfo(url);
        //先请求一次获取要下载内容的长度
        long contentLength = getContentLength(url);
        downloadInfo.setTotal(contentLength);
        String fileName = "download.apk";
        downloadInfo.setFileName(fileName);
        return downloadInfo;

    }

    private DownloadInfo getRealFileName(DownloadInfo downloadInfo) {
        String fileName = downloadInfo.getFileName();
        long downloadLength = 0;
        long contentLength = downloadInfo.getTotal();
        File file = new File(App.getInstance().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName);
        downloadInfo.setSaveLoaction(file.getAbsolutePath());
        if (file.exists()) {
            //找到了文件,代表已经下载过,则获取其长度
            downloadLength = file.length();
        }
        //之前下载过,需要重新来一个文件
        int i = 1;
        while (downloadLength >= contentLength) {
            int dotIndex = fileName.lastIndexOf(".");
            String fileNameAnother;
            if (dotIndex == -1) {
                fileNameAnother = fileName + "(" + i + ")";
            } else {
                fileNameAnother = fileName.substring(0, dotIndex)
                        + "(" + i + ")" + fileName.substring(dotIndex);
            }
            File newFile = new File(App.getInstance().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileNameAnother);
            file = newFile;
            downloadLength = newFile.length();
            i++;
        }
        downloadInfo.setProgress(downloadLength);
        downloadInfo.setFileName(file.getName());
        return downloadInfo;
    }

    /**
     * 先请求一次获取要下载的文件的长度
     *
     * @param url 下载地址
     * @return 要下载文件的长度
     */
    private long getContentLength(String url) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        try {
            Response response = mClient.newCall(request).execute();
            if (response != null && response.isSuccessful()) {
                long contentLength = response.body().contentLength();
                response.close();
                return contentLength == 0 ? DownloadInfo.TOTAL_ERROR : contentLength;
            }
        } catch (IOException e) {
            Log.e(TAG, "getContentLength exception" + e.getMessage());
        }
        return DownloadInfo.TOTAL_ERROR;
    }

    private class DownloadSubscriber implements ObservableOnSubscribe<DownloadInfo> {

        private DownloadInfo downloadInfo;

        public DownloadSubscriber(DownloadInfo downloadInfo) {
            this.downloadInfo = downloadInfo;
        }

        @Override
        public void subscribe(ObservableEmitter<DownloadInfo> e) throws Exception {
            String url = downloadInfo.getUrl();
            long downloadLength = downloadInfo.getProgress();
            long currentLength = downloadInfo.getTotal();
            //初始进度信息
            e.onNext(downloadInfo);
            Request request = new Request.Builder()
                    //确定下载的范围,添加此头,则服务器就可以跳过已经下载好的部分
                    .addHeader("RANGE", "bytes=" + downloadLength + "-" + currentLength)
                    .url(url)
                    .build();
            Call call = mClient.newCall(request);
            downCalls.put(url, call);
            Response response = call.execute();
            File file = new File(App.getInstance().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), downloadInfo.getFileName());
            InputStream is = null;
            FileOutputStream fos = null;
            try {
                is = response.body().byteStream();
                fos = new FileOutputStream(file, true);
                byte[] buffer = new byte[2048];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    downloadLength += len;
                    downloadInfo.setProgress(downloadLength);
                    e.onNext(downloadInfo);
                }
                fos.flush();
                downCalls.remove(url);
                IOUtil.closeAll(is, fos);
                e.onComplete();
            } catch (IOException exception) {
                Log.e(TAG, exception.getMessage());
                e.onError(exception);
            }
        }
    }
}
