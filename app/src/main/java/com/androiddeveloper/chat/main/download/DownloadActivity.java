package com.androiddeveloper.chat.main.download;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;

import com.alibaba.fastjson.JSON;
import com.androiddeveloper.chat.R;
import com.androiddeveloper.chat.main.LatestInfoResponse;
import com.androiddeveloper.chat.utils.FilePathUtil;
import com.androiddeveloper.chat.utils.http.CallBackUtil;
import com.androiddeveloper.chat.utils.http.OkhttpUtil;

import java.io.File;

import okhttp3.Call;

public class DownloadActivity extends AppCompatActivity {
    private PackageInfo packageInfo;
    private LatestInfoResponse latestInfoResponse;

    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        init();
        runUpdate();
    }

    private void init() {
        progressBar = findViewById(R.id.progressBar);

        //获取包名
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //获取更新信息
        Intent intent = getIntent();
        String json = intent.getStringExtra("LatestInfoResponse");
        latestInfoResponse = JSON.parseObject(json, LatestInfoResponse.class);
    }

    /**
     * 执行更新
     */
    private void runUpdate() {
        //文件名举例  1-1.0.apk
        File apkFile = new File(FilePathUtil.getApkDownloadFolder(this) + "/"
                + latestInfoResponse.getVersionCode() + "-" + latestInfoResponse.getVersionName() + ".apk");
        OkhttpUtil.downloadFile(latestInfoResponse.getApkDownloadUrl(),
                new CallBackUtil.CallBackFile(apkFile.getParent(), apkFile.getName()) {
                    @Override
                    public void onProgress(float progress, long total) {
                        super.onProgress(progress, total);
                        progressBar.setProgress((int) (progress));
                    }

                    @Override
                    public void onFailure(Call call, Exception e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(File file) {
                        installApk(apkFile);
                    }
                });
    }

    /**
     * 执行安装apk
     *
     * @param apkFile
     */
    private void installApk(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri uri = FileProvider.getUriForFile(DownloadActivity.this,
                    packageInfo.packageName + ".fileprovider", apkFile);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile),
                    "application/vnd.android.package-archive");
        }
        startActivity(intent);
    }
}