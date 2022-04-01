package com.hm.okhttpdemo.activity;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.gson.Gson;
import com.hm.okhttpdemo.R;
import com.hm.okhttpdemo.model.NowWeatherBean;
import com.hm.okhttpdemo.utils.NetWorkUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;
import okio.BufferedSink;
import okio.BufferedSource;

public class MainActivity extends AppCompatActivity {

    private OkHttpClient client;
    private static Gson gson = new Gson();
    private static final String TAG = "MainActivity";
    private static String imgUrl = "http://img4.cache.netease.com/photo/0026/2015-05-19/APVC513454A40026.jpg";
    private static String url = "http://api.k780.com:88/?app=weather.today&weaid=1&appkey=10003&sign=b59bc3ef6191eb9f747dd4e83c99f2a4&format=json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initOkHttpClient();
    }

    @OnClick(R.id.button)
    public void onClick() {
        DownloadActivity.launch(this);
    }

    private void initOkHttpClient() {
        File cacheDir = getFilesDir();
        Log.i(TAG, "initOkHttpClient: cacheDir = " + cacheDir.getAbsolutePath());
        int size = 100 * 1024 * 1024;
        Cache cache = new Cache(cacheDir, size);
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .cache(cache)
                .addInterceptor(new CacheInterceptor())
                .build();
    }

    public void asynGet(View view) {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //并不是在主线程
                // Log.e(TAG, "current Thread:"+Thread.currentThread().getName());
                if (response.isSuccessful()) {
                    Headers headers = response.headers();
                    CacheControl cacheControl = response.cacheControl();
                    Log.e(TAG, "CacheControl.maxAgeSeconds=" + cacheControl.maxAgeSeconds());
                    for (int i = 0; i < headers.size(); i++) {
                        Log.e(TAG, "head=" + headers.name(i) + ",value=" + headers.value(i));
                    }
                    //Gson 解析结果
                    NowWeatherBean bean = gson.fromJson(response.body().charStream(), NowWeatherBean.class);
                    NowWeatherBean.ResultBean resultBean = bean.getResult();
                    Log.e(TAG, resultBean.getCitynm());
                    Log.e(TAG, resultBean.getDays());
                    Log.e(TAG, response.body().string());
                }
            }
        });
    }

    private void postString() {

        MediaType mediaType = MediaType.parse("text/x-markdown; charset=utf-8");
        String postBody = ""
                + "Releases\n"
                + "--------\n"
                + "\n"
                + " * _1.0_ May 6, 2013\n"
                + " * _1.1_ June 15, 2013\n"
                + " * _1.2_ August 11, 2013\n";
        Request request = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .post(RequestBody.create(mediaType, postBody))
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    private void postStream() {

        final MediaType mediaType = MediaType.parse("text/x-markdown; charset=utf-8");
        RequestBody requestBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public void writeTo(BufferedSink sink) throws IOException {
                sink.writeUtf8("Numbers\n");
                sink.writeUtf8("-------\n");
            }
        };
        Request request = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .post(requestBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    private void postJson(String json) {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        Request request = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .post(RequestBody.create(mediaType, json))
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    private void postJson(File file) {
        MediaType mediaType = MediaType.parse("text/x-markdown; charset=utf-8");
        Request request = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .post(RequestBody.create(mediaType, file))
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    private void postFormData() {

        RequestBody body = new FormBody.Builder()
                .add("platform", "android")
                .add("value", "dmw")
                .build();
        Request request = new Request.Builder()
                .url("https://api.github.com/markdown/raw")
                .post(body)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    private void postMultipart(File file) {
        MediaType mediaType = MediaType.parse("application/octet-stream");
        RequestBody fileBody = RequestBody.create(mediaType, file);
        MediaType textType = MediaType.parse("text/plain");
        String content = "上传的文本";
        RequestBody textBody = RequestBody.create(textType, content);
        //创建表单实体
        RequestBody requestBody = new MultipartBody
                .Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file[]", "test.png", fileBody)
                .addFormDataPart("text", "text", textBody)
                .addFormDataPart("key", "123")
                .build();
        Request request = new Request.Builder()
                .url("address")
                .post(requestBody)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });

    }

    /**
     * 取消请求
     */
    private void cancelCall() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        Request request = new Request.Builder()
                .url("")
                .build();
        final Call call = client.newCall(request);
        service.schedule(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "cancelCall");
                call.cancel();
            }
        }, 1, TimeUnit.SECONDS);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    private void preCallConfiguration() {
        Request request = new Request.Builder()
                .url("http://httpbin.org/delay/1")
                .build();
        OkHttpClient clientCopy = client.newBuilder()
                .readTimeout(50, TimeUnit.SECONDS)
                .build();
        Call call = clientCopy.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    private void handleAuthentication() {
        final String credential = Credentials.basic("jesse", "password1");
        Request request = new Request.Builder()
                .url("http://httpbin.org/delay/1")
                .build();
        OkHttpClient clientCopy = client.newBuilder()
                .authenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        if (responseCount(response) >= 3) {
                            return null; // If we've failed 3 times, give up.
                        } else {
                            return response
                                    .request()
                                    .newBuilder()
                                    .header("Authorization", credential)
                                    .build();
                        }
                    }
                })
                .build();
        Call call = clientCopy.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });
    }

    private int responseCount(Response response) {
        int result = 1;
        while ((response = response.priorResponse()) != null) {
            result++;
        }
        return result;
    }

    private void testDownload() {
        CacheControl cacheControl = new CacheControl.Builder()
                .noCache()
                .build();
        Request request = new Request
                .Builder()
                .url(imgUrl)
                .cacheControl(cacheControl)
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (saveImg(response)) {
                    Log.e(TAG, "下载图片成功");
                }
            }
        });
    }

    private boolean saveImg(Response response) {
        //图片下载时保存的地址
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "dumingwei.jpg");
        InputStream in = response.body().byteStream();
        FileOutputStream out = null;
        BufferedOutputStream bo = null;
        try {
            out = new FileOutputStream(file);
            bo = new BufferedOutputStream(out);
            int b;
            while ((b = in.read()) != -1) {
                bo.write(b);
            }
            bo.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }

                if (bo != null) {
                    bo.close();
                }
                response.body().close();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
    }

    private static class CacheInterceptor implements Interceptor {

        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            //没有网络就读取本地缓存的数据
            if (!NetWorkUtil.isConnected()) {
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_CACHE)
                        .build();
            } else {
                request = request.newBuilder()
                        .cacheControl(CacheControl.FORCE_NETWORK)
                        .build();
            }
            //请求结果
            Response originalResponse = chain.proceed(request);
            BufferedSource source = originalResponse.body().source();
            source.request(Long.MAX_VALUE);//不加这句打印不出来
            Response response;
            if (NetWorkUtil.isConnected()) {
                //有网的时候读接口上的@Headers里的配置，你可以在这里进行统一的设置(注掉部分)
                String cacheControl = request.cacheControl().toString();
                response = originalResponse.newBuilder()
                        .header("Cache-Control", cacheControl)//这是从接口的注解@Headers上读到的head信息
                        .removeHeader("Pragma")
                        .build();
                return response;
            } else {
                //没网络的时候保存6分钟
                int maxAge = 60 * 60;
                response = originalResponse.newBuilder()
                        //only-if-cached:(仅为请求标头)请求:告知缓存者,我希望内容来自缓存，我并不关心被缓存响应,是否是新鲜的.
                        .header("Cache-Control", "public, only-if-cached, max-age=" + maxAge)
                        //移除pragma消息头，移除它的原因是因为pragma也是控制缓存的一个消息头属性
                        .removeHeader("Pragma")
                        .build();
                return response;
            }
        }
    }
}
