package com.example.cameraxvideo;

import static com.example.cameraxvideo.MainActivity2.sharedPreferences;
import static com.example.cameraxvideo.util.AnimationUtil.ButtonTextColorChange;
import static com.example.cameraxvideo.util.AnimationUtil.TVStyleFocusAnimation;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.icu.text.DecimalFormat;
import android.icu.text.SimpleDateFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import java.io.File;
import java.util.Date;
import java.util.Objects;

public class VideoPreviewActivity extends AppCompatActivity {
    private SharedPreferences.Editor editor;
    private VideoView videoView;
    private Button btn_delete_video;
    private Button btn_rename_video;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);
        Objects.requireNonNull(getSupportActionBar()).hide();
        videoView = findViewById(R.id.videoView);
        btn_delete_video = findViewById(R.id.btn_delete_video);
        btn_rename_video = findViewById(R.id.btn_rename_video);
        Button btn_info = findViewById(R.id.btn_info1);
        SparkButton btn_like = findViewById(R.id.like_video_button);
        editor=sharedPreferences.edit();
        //加载指定的视频文件
        Intent intent = getIntent();
        String videoFileUri = intent.getStringExtra("video");
        int pos = intent.getIntExtra("pos", 0);
        Uri fileUri = Uri.parse(videoFileUri);
        //根据Uri解析出文件的绝对路径
        File videoFile = new File(Objects.requireNonNull(fileUri.getPath()));
        String fileID= String.valueOf(videoFile.lastModified());
        videoView.setVideoPath(videoFile.toString());
        btn_like.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                TVStyleFocusAnimation(v, hasFocus, 1.0f, 1.2f);
            }
        });
        btn_info.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                TVStyleFocusAnimation(v, hasFocus, 1.0f, 1.2f);
            }
        });
        btn_delete_video.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                TVStyleFocusAnimation(v, hasFocus, 1.0f, 1.1f);
                ButtonTextColorChange(btn_delete_video,hasFocus);
            }
        });
        btn_rename_video.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                TVStyleFocusAnimation(v, hasFocus, 1.0f, 1.1f);
                ButtonTextColorChange(btn_rename_video,hasFocus);
            }
        });
        if(sharedPreferences.getBoolean(fileID,false)){
            btn_like.setChecked(true);
        }
        btn_like.setEventListener(new SparkEventListener() {
            @Override
            public void onEvent(ImageView button, boolean buttonState) {
                if(buttonState){
                    editor.putBoolean(fileID,true);
                }else {
                    editor.remove(fileID);
                }
            }

            @Override
            public void onEventAnimationEnd(ImageView button, boolean buttonState) {

            }

            @Override
            public void onEventAnimationStart(ImageView button, boolean buttonState) {

            }
        });



        //创建MediaController对象
        MediaController mediaController = new MediaController(this);

        //VideoView与MediaController建立关联
        videoView.setMediaController(mediaController);

        //让VideoView获取焦点
        //videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
            }
        });
        btn_delete_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(VideoPreviewActivity.this);
                builder.setTitle("确认删除");
                builder.setMessage("您确定要删除这个视频吗？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean isDeleted = videoFile.delete();
                        if (isDeleted) {
                            //mainActivity.updateDataList(pos);
                            Intent backintent = new Intent();
                            backintent.putExtra("pos_delete", pos);
                            editor.remove(fileID);
                            setResult(RESULT_OK, backintent);
                            finish();
                        } else {
                            Toast.makeText(VideoPreviewActivity.this, "照片删除失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 取消删除操作
                        dialog.cancel();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).requestFocus();

            }
        });
        btn_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(VideoPreviewActivity.this);
                StringBuilder sb = new StringBuilder();
                double fileSizeInKB = videoFile.length() * 1.0 / 1024;
                DecimalFormat decimalFormat = new DecimalFormat("#.00");
                String filePath = videoFile.getPath();
                String fileName = videoFile.getName();
                Date lastModifiedDate = new Date(videoFile.lastModified());
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String lastModifiedString = formatter.format(lastModifiedDate);
                sb.append("视频名字：  ").append(fileName).append("\n\n视频路径：  ").append(filePath).append("\n\n视频大小： ").append(decimalFormat.format(fileSizeInKB)).append(" KB").append("\n\n视频日期：  ").append(lastModifiedString);
                alert.setMessage(sb.toString());
                alert.setTitle("视频信息：");
                alert.setPositiveButton("确定", null);
                final AlertDialog dialog = alert.create();
                dialog.show();//展示dialog
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).requestFocus();
            }
        });
        btn_rename_video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldFileName = videoFile.getName();
                AlertDialog.Builder alert = new AlertDialog.Builder(VideoPreviewActivity.this);
                alert.setTitle("确认修改名字");
                LinearLayout layout = new LinearLayout(VideoPreviewActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                EditText editText = new EditText(VideoPreviewActivity.this);
                editText.setText(oldFileName);
                layout.addView(editText);
                // 将自定义视图设置为 AlertDialog 的内容
                alert.setView(layout);
                editText.setSelection(0, oldFileName.indexOf('.'));
                editText.setSelected(true);


                // 添加按钮和监听器
                alert.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 获取EditText的输入内容
                        String newFileName = editText.getText().toString();
                        // 检查新的文件名是否与原文件名相同，如果不同则执行重命名操作
                        if (!newFileName.equals(oldFileName)) {
                            File newFile = new File(videoFile.getParentFile(), newFileName);
                            if (newFile.exists()) { //改的新名字与其他文件重名
                                Date lastModifiedDate = new Date(videoFile.lastModified());
                                newFileName = lastModifiedDate.toString() + newFileName;
                                newFile = new File(videoFile.getParentFile(), newFileName);
                            }
                            if (videoFile.renameTo(newFile)) {// 更新文件名成功后，可以执行其他操作，如刷新列表等
                                Intent backintent = new Intent();
                                Uri fileUri = Uri.fromFile(newFile);
                                backintent.putExtra("new_photo", fileUri.toString()); // 将照片URI传递给预览活动
                                backintent.putExtra("pos_rename", pos);
                                backintent.putExtra("fileType",222);
                                setResult(MainActivity2.RESULT_CHANGE, backintent);
                                dialog.dismiss();
                                finish();
                            } else {
                                // 更新文件名失败，可以弹出提示信息等处理
                                finish();
                            }
                        }
                    }
                });
                final AlertDialog dialog = alert.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        editor.commit();
    }
}