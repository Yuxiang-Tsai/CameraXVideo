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
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import java.io.File;
import java.util.Date;
import java.util.Objects;

public class PhotoPreviewActivity extends AppCompatActivity {
    private Button button_delete;
    private Button button_rename;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_preview);
        Objects.requireNonNull(getSupportActionBar()).hide();
        SparkButton btn_like = findViewById(R.id.like_photo_button);
        ImageView imageView = findViewById(R.id.bigImageView);
        button_delete = findViewById(R.id.btn_delete);
        Button button_info = findViewById(R.id.btn_info);
        button_rename = findViewById(R.id.btn_rename);
        btn_like.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                TVStyleFocusAnimation(v, hasFocus, 1.0f, 1.2f);
            }
        });
        button_info.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                TVStyleFocusAnimation(v, hasFocus, 1.0f, 1.1f);
            }
        });
        button_rename.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                TVStyleFocusAnimation(v, hasFocus, 1.0f, 1.2f);
                ButtonTextColorChange(button_rename, hasFocus);
            }
        });
        button_delete.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                TVStyleFocusAnimation(v, hasFocus, 1.0f, 1.2f);
                ButtonTextColorChange(button_delete, hasFocus);
            }
        });
        Intent intent = getIntent();
        String photoFileUri = intent.getStringExtra("photo");
        int pos = intent.getIntExtra("pos", 0);
        // Log.d(TAG, "getIntExtra pos is: "+pos);
        Uri fileUri = Uri.parse(photoFileUri);
        //根据Uri解析出文件的绝对路径
        File photoFile = new File(Objects.requireNonNull(fileUri.getPath()));
        editor = sharedPreferences.edit();
        //将文件的上次修改时间设置为文件的唯一标识
        String fileID = String.valueOf(photoFile.lastModified());
        Glide.with(PhotoPreviewActivity.this)
                .load(photoFile)
                .into(imageView);
        button_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PhotoPreviewActivity.this);
                builder.setTitle("确认删除");
                builder.setMessage("您确定要删除这个照片吗？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean isDeleted = photoFile.delete();
                        if (isDeleted) {
                            Intent backintent = new Intent();
                            backintent.putExtra("pos_delete", pos);
                            editor.remove(fileID);
                            setResult(RESULT_OK, backintent);
                            finish();
                        } else {
                            Toast.makeText(PhotoPreviewActivity.this, "照片删除失败", Toast.LENGTH_SHORT).show();
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
        button_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert = new AlertDialog.Builder(PhotoPreviewActivity.this);
                StringBuilder sb = new StringBuilder();
                String filePath = photoFile.getPath();
                String fileName = photoFile.getName();
                double fileSizeInKB = photoFile.length() * 1.0 / 1024;
                DecimalFormat decimalFormat = new DecimalFormat("#.00");
                Date lastModifiedDate = new Date(photoFile.lastModified());
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                String lastModifiedString = formatter.format(lastModifiedDate);
                sb.append("照片名字：  ").append(fileName).append("\n\n照片路径：  ").append(filePath).append("\n\n照片大小： ").append(decimalFormat.format(fileSizeInKB)).append(" KB").append("\n\n照片日期：  ").append(lastModifiedString);
                alert.setMessage(sb.toString());
                alert.setTitle("照片信息：");
                alert.setPositiveButton("确定", null);
                final AlertDialog dialog = alert.create();
                dialog.show();//展示dialog
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).requestFocus();
            }
        });
        button_rename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldFileName = photoFile.getName();
                AlertDialog.Builder alert = new AlertDialog.Builder(PhotoPreviewActivity.this);
                alert.setTitle("确认修改名字");
                LinearLayout layout = new LinearLayout(PhotoPreviewActivity.this);
                layout.setOrientation(LinearLayout.VERTICAL);
                EditText editText = new EditText(PhotoPreviewActivity.this);
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
                        if (newFileName.equals(".jpg")) {
                            Toast.makeText(PhotoPreviewActivity.this, "照片改名失败,照片名不能为空", Toast.LENGTH_LONG).show();
                            finish();
                        }
                        // 检查新的文件名是否与原文件名相同，如果不同则执行重命名操作
                        if (!newFileName.equals(oldFileName)) {
                            File newFile = new File(photoFile.getParentFile(), newFileName);
                            if (newFile.exists()) {
                                //newfilename后面加日期
                                Date lastModifiedDate = new Date(photoFile.lastModified());
                                newFileName = lastModifiedDate.toString() + newFileName;
                                newFile = new File(photoFile.getParentFile(), newFileName);
                            }
                            if (photoFile.renameTo(newFile)) {
                                Intent backintent = new Intent();
                                Uri fileUri = Uri.fromFile(newFile);
                                backintent.putExtra("new_photo", fileUri.toString()); // 将照片URI传递给预览活动
                                backintent.putExtra("pos_rename", pos);
                                backintent.putExtra("fileType", 111);
                                setResult(MainActivity2.RESULT_CHANGE, backintent);
                                dialog.dismiss();
                                finish();
                                // 更新文件名成功后，可以执行其他操作，如刷新列表等
                            } else {
                                // 更新文件名失败，可以弹出提示信息等处理
                                //setResult(RESULT_CANCELED);
                                Toast.makeText(PhotoPreviewActivity.this, "照片改名失败", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }
                    }
                });

                alert.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 关闭对话框
                        dialog.dismiss();
                    }
                });
                // 显示对话框
                final AlertDialog dialog = alert.create();
                dialog.show();
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
            }
        });
        if (sharedPreferences.getBoolean(fileID, false)) {
            btn_like.setChecked(true);
        }
        btn_like.setEventListener(new SparkEventListener() {
            @Override
            public void onEvent(ImageView button, boolean buttonState) {
                if (buttonState) {
                    editor.putBoolean(fileID, true);
                } else {
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        editor.commit();
    }
}