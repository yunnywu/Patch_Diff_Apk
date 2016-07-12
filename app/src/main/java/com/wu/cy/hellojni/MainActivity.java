package com.wu.cy.hellojni;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("apk_diff_patch");
    }

    //低版本的apk文件
    private static final String APK_310 = "/sdcard/update_test/310.apk";

    //高版本的diff文件
    private static final String APK_312 = "/sdcard/update_test/312.apk";

    //生成的diff文件
    private static final String APK_DIFF = "/sdcard/update_test/diff.apk";

    //patch 之后生成的文件
    private static final String APK_NEW = "/sdcard/update_test/new.apk";

    @Bind(R.id.tv_md5)
    TextView mTvMd5;
    @Bind(R.id.btn_make_diff)
    Button mBtnMakeDiff;
    @Bind(R.id.btn_patch_apk)
    Button mBtnPatchApk;
    @Bind(R.id.et_file_name)
    EditText mEtFileName;
    @Bind(R.id.btn_generate_md5)
    Button mBtnGenerateMd5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }

    /**
     *  查看文件的MD5 在EditText输入文件名
     */
    @OnClick(R.id.btn_generate_md5)
    public void generateTheFileMD5(){
        String fileName = "/sdcard/update_test/" + mEtFileName.getText().toString().trim();
        mTvMd5.setText(fileToMD5(fileName));
    }


    @OnClick(R.id.btn_make_diff)
    public void makeTheDiffApk(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int result = DiffUtils.genDiff(APK_310, APK_312, APK_DIFF);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, result == 0 ? "Diff success" : "Diff Fail", Toast.LENGTH_LONG).show();
                    }
                });

            }
        }).start();
    }


    @OnClick(R.id.btn_patch_apk)
    public void patchTheDiffApk(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                final int result = PatchUtils.patch(APK_310, APK_NEW, APK_DIFF);
                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           Toast.makeText(MainActivity.this, result == 0 ? "Patch success" : "Patch Fail", Toast.LENGTH_LONG).show();
                       }
                   });

            }
        }).start();

    }


    public static String fileToMD5(String filePath) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            MessageDigest digest = MessageDigest.getInstance("MD5");
            int numRead = 0;
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0)
                    digest.update(buffer, 0, numRead);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (Exception e) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception e) { }
            }
        }
    }
}
