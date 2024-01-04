package com.example.cameraxvideo;

import static com.example.cameraxvideo.util.AnimationUtil.ButtonTextColorChange;
import static com.example.cameraxvideo.util.AnimationUtil.TVStyleFocusAnimation;
import static java.lang.System.exit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalLensFacing;
import androidx.camera.core.ExperimentalZeroShutterLag;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.ZoomState;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.FallbackStrategy;
import androidx.camera.video.FileOutputOptions;
import androidx.camera.video.Quality;
import androidx.camera.video.QualitySelector;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.video.VideoOutput;
import androidx.camera.video.VideoRecordEvent;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;
import androidx.lifecycle.LiveData;

import com.google.common.util.concurrent.ListenableFuture;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    public static final int REQUEST_CODE_PERMISSIONS = 10;
    private static final String[] REQUIRE_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    File outputDirectory;
    Button btn_gallery, btn_change, button;
    SparkButton sparkButton;
    SeekBar seekBar;
    boolean isRecord = false;//是否正在录制
    boolean isPic = false; //是否是拍照模式
    PreviewView previewView;
    VideoCapture<VideoOutput> videoCapture;
    ImageCapture imageCapture;
    Preview preview;
    ExecutorService cameraExecutor;
    private ProcessCameraProvider cameraProvider;
    private long exitTime = 0; //判断退出时间间隔
    private Camera camera;
    private CameraInfo cameraInfo;
    private Recorder recorder;
    private Recording recording;
    private Chronometer chronometer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sparkButton = findViewById(R.id.spark_button);
        previewView = findViewById(R.id.previewView);
        seekBar = findViewById(R.id.seekBar);
        button = findViewById(R.id.button1);
        btn_gallery = findViewById(R.id.btn_open_gallery);
        btn_change = findViewById(R.id.btn_change_mode);
        chronometer = findViewById(R.id.timer);
        chronometer.setVisibility(View.INVISIBLE);
        button.setVisibility(View.INVISIBLE);
        if (!havePermissions()) {
            ActivityCompat.requestPermissions(this, REQUIRE_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }

        button.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {//当选中这个View时做一些你所需要的操作
                    v.setScaleX(1.1f);
                    v.setScaleY(1.1f);
                } else {
                    v.setScaleX(0.9f);
                    v.setScaleY(0.9f);
                }
            }
        });
        btn_gallery.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                TVStyleFocusAnimation(v, hasFocus, 1.0f, 1.2f);
                ButtonTextColorChange(btn_gallery,hasFocus);
            }
        });
        btn_change.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                TVStyleFocusAnimation(v, hasFocus, 1.0f, 1.2f);
                ButtonTextColorChange(btn_change,hasFocus);
            }
        });
        outputDirectory = getOutputDirectory();
        button.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                if (isPic) {
                    takePhoto();
                    Animation animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.dialog_in_anim);
                    animation.setDuration(800);
                    View all = findViewById(R.id.previewView);
                    all.startAnimation(animation);
                } else {
                    Toast.makeText(MainActivity.this, "状态错误", Toast.LENGTH_SHORT).show();
                }
            }
        });
        btn_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckIsRecord(isRecord);
                if (!isPic) {
                    isPic = true;
                    button.setVisibility(View.VISIBLE);
                    sparkButton.setVisibility(View.INVISIBLE);
                } else {
                    isPic = false;
                    button.setVisibility(View.INVISIBLE);
                    sparkButton.setVisibility(View.VISIBLE);
                }
            }
        });
        btn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckIsRecord(isRecord);
                Uri fileUri = Uri.fromFile(outputDirectory);
                Intent intent = new Intent(MainActivity.this, MainActivity2.class);
                intent.putExtra("file_uri", fileUri.toString());
                startActivity(intent);
                overridePendingTransition(R.anim.in2, R.anim.out2);
            }
        });
        sparkButton.setEventListener(new SparkEventListener() {
            @Override
            public void onEvent(ImageView button, boolean buttonState) {
                if (buttonState) {
                    // Button is active
                    takeVideo();
                    chronometer.setVisibility(View.VISIBLE);
                    chronometer.setBase(SystemClock.elapsedRealtime());
                    chronometer.start();
                    isRecord = true;

                } else {
                    // Button is inactive
                    recording.close();
                    chronometer.stop();
                    chronometer.setVisibility(View.INVISIBLE);
                    isRecord = false;
                }
            }

            @Override
            public void onEventAnimationEnd(ImageView button, boolean buttonState) {

            }

            @Override
            public void onEventAnimationStart(ImageView button, boolean buttonState) {

            }
        });
        sparkButton.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {//当选中这个View时做一些你所需要的操作
                    v.setScaleX(1.1f);
                    v.setScaleY(1.1f);
                } else {
                    v.setScaleX(0.9f);
                    v.setScaleY(0.9f);
                }
            }
        });

        Objects.requireNonNull(getSupportActionBar()).hide();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            initCamera();
        } else {
            Toast.makeText(MainActivity.this,"在 onRequestPermissionsResult 退出",Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    //判断权限是否获取
    private boolean havePermissions() {
        for (String permission : REQUIRE_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @SuppressLint("RestrictedApi")
    @OptIn(markerClass = ExperimentalLensFacing.class)
    private void initCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor();
        ///实例化（可以设置许多属性)
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @OptIn(markerClass = ExperimentalZeroShutterLag.class)
            @Override
            public void run() {
                try {
                    cameraProvider = cameraProviderFuture.get();
                    imageCapture = new ImageCapture.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9).build();
                    List<Quality> qualityList = new ArrayList<>();
                    qualityList.add(Quality.UHD);
                    qualityList.add(Quality.FHD);
                    qualityList.add(Quality.HD);
                    qualityList.add(Quality.SD);
                    QualitySelector qualitySelector = QualitySelector.fromOrderedList(qualityList, FallbackStrategy.lowerQualityOrHigherThan(Quality.FHD));
                    recorder = new Recorder.Builder().setExecutor(cameraExecutor).setQualitySelector(qualitySelector).setAspectRatio(AspectRatio.RATIO_16_9).build();
                    videoCapture = VideoCapture.withOutput(recorder);
                    preview = new Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_16_9).build();
                    CameraSelector cameraSelector = new CameraSelector.Builder()
                            .requireLensFacing(CameraSelector.LENS_FACING_EXTERNAL)//外接usb摄像
                            .build();
                    cameraProvider.unbindAll();
                    if (cameraProvider.hasCamera(cameraSelector)) {
                        camera = cameraProvider.bindToLifecycle(MainActivity.this, cameraSelector, preview, imageCapture, videoCapture);
                    } else {
                        camera = cameraProvider.bindToLifecycle(MainActivity.this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture, videoCapture);
                    }
                    CameraControl cameraControl = camera.getCameraControl();
                    // seekbar
                    if (MainActivity.this.isSupportZoom()) {
                        seekBar.setVisibility(View.VISIBLE);
                        float maxZoom = 10 * Objects.requireNonNull(cameraInfo.getZoomState().getValue()).getMaxZoomRatio();
                        float minZoom = 10 * cameraInfo.getZoomState().getValue().getMinZoomRatio();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            seekBar.setMin((int) minZoom);
                        }
                        seekBar.setMax((int) maxZoom);
                        int progress = seekBar.getProgress();
                        if (progress > (int) maxZoom) {
                            seekBar.setProgress((int) maxZoom);
                        } else if (progress < (int) minZoom) {
                            seekBar.setProgress((int) minZoom);
                        }
                        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                cameraControl.setZoomRatio((float) (progress * 0.1));
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });
                    }
                    preview.setSurfaceProvider(previewView.getSurfaceProvider());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (havePermissions()) {
            initCamera();
        } else {
            ActivityCompat.requestPermissions(this, REQUIRE_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraExecutor!=null){
            cameraExecutor.shutdown();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(cameraExecutor!=null){
            cameraExecutor.shutdown();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    @SuppressLint("RestrictedApi")
    private void takeVideo() {
        if (videoCapture != null) {
            String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.SIMPLIFIED_CHINESE).format(System.currentTimeMillis());
            File videoFile = new File(outputDirectory,
                    name + ".mp4");
            FileOutputOptions fileOutputOptions = new FileOutputOptions.Builder(videoFile).build();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                recording = recorder.prepareRecording(this, fileOutputOptions).withAudioEnabled().start(getMainExecutor(), new Consumer<VideoRecordEvent>() {
                    @Override
                    public void accept(VideoRecordEvent videoRecordEvent) {
                        if (videoRecordEvent instanceof VideoRecordEvent.Finalize) {
                            if (((VideoRecordEvent.Finalize) videoRecordEvent).hasError()) {
                                Toast.makeText(MainActivity.this, "视频保存失败", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MainActivity.this, "视频保存成功", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });
            }
        }
    }

    private void takePhoto() {
        if (imageCapture != null) {
            String name = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.SIMPLIFIED_CHINESE).format(System.currentTimeMillis());
            File photoFile = new File(outputDirectory,
                    name + ".jpg");
            // 创建 output option 对象，用以指定照片的输出方式
            ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions
                    .Builder(photoFile)
                    .build();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                imageCapture.takePicture(outputFileOptions, getMainExecutor(), new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Uri savedUri = Uri.fromFile(photoFile);
                        String msg = "照片捕获成功! " + savedUri;
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "照片捕获失败: " + exception.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                });
            }

        }
    }

    public File getOutputDirectory() {
        File mediaDir = new File(getExternalMediaDirs()[0], getString(R.string.app_name));
        boolean isExist = mediaDir.exists() || mediaDir.mkdir();
        return isExist ? mediaDir : null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.my_menu, menu);
        return true;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        CheckIsRecord(isRecord);
        if (item.getItemId() == R.id.action_video) {
            // 处理设置菜单项的点击事件
            isPic = false;
            button.setVisibility(View.INVISIBLE);
            sparkButton.setVisibility(View.VISIBLE);
            //button.setBackgroundResource(R.drawable.circle_shape);
        } else if (item.getItemId() == R.id.action_pic) {
            isPic = true;
            button.setVisibility(View.VISIBLE);
            sparkButton.setVisibility(View.INVISIBLE);
        } else if (item.getItemId() == R.id.action_photos) {
            Uri fileUri = Uri.fromFile(outputDirectory);
            Intent intent = new Intent(MainActivity.this, MainActivity2.class);
            intent.putExtra("file_uri", fileUri.toString());
            startActivity(intent);
            overridePendingTransition(R.anim.in2, R.anim.out2);
        }
        // 其他情况的处理逻辑

        return true;


    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                //弹出提示，可以有多种方式
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
                return true;
            } else {
                exit(0);
            }
        } else if (keyCode == KeyEvent.KEYCODE_MENU && event.getAction() == KeyEvent.ACTION_DOWN) {
            openOptionsMenu();
            Toast.makeText(MainActivity.this, "press menu", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;// 如果未处理该按键事件，则返回false，让系统继续处理该事件
    }

    private boolean isSupportZoom() {
        boolean isSupport = false;
        cameraInfo = camera.getCameraInfo();
        LiveData<ZoomState> zoomStateLiveData = cameraInfo.getZoomState();
        if (zoomStateLiveData.getValue() != null) {
            if (zoomStateLiveData.getValue().getMaxZoomRatio() > 1) {
                //Log.d(TAG, "MaxZoomRatio: "+zoomStateLiveData.getValue().getMaxZoomRatio());
                isSupport = true;
            }
        }
        return isSupport;
    }

    //检查是否在录制视频，如果在录制则关闭
    private void CheckIsRecord(boolean isRecording) {
        if (isRecording) {
            recording.close();
            chronometer.stop();
            chronometer.setVisibility(View.INVISIBLE);
            isRecord = false;
            sparkButton.setChecked(false);
        }
    }
}