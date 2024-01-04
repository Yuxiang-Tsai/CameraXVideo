package com.example.cameraxvideo;


import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cameraxvideo.pojo.ImageFile;
import com.example.cameraxvideo.pojo.VideoFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity2 extends AppCompatActivity {
    public static final int RESULT_CHANGE = 223;
    private static final int REQUEST_CODE = 123;
    public static SharedPreferences sharedPreferences;
    private final List<Object> objectsList = new ArrayList<>();
    private SharedPreferences.Editor editor;
    private boolean is_filter = false;//是否有搜索
    private boolean is_view_like = false;//是否看收藏
    private MyAdapter myAdapter;
    private List<Object> filteredLikeData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        FocusRecyclerView recyclerView = findViewById(R.id.recyclerView);
        loadData();
        myAdapter = new MyAdapter(this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 4);
        gridLayoutManager.setOrientation(RecyclerView.VERTICAL);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(myAdapter);
        myAdapter.setData(objectsList);
        //myAdapter.getItemId(0);
        myAdapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                if (myAdapter.getItemViewType(position) == MyAdapter.PHOTO) {
                    Intent intent = new Intent(MainActivity2.this, PhotoPreviewActivity.class);
                    ImageFile imageFile = (ImageFile) myAdapter.getmList().get(position);
                    File file = imageFile.getFile();
                    Uri fileUri = Uri.fromFile(file);
                    intent.putExtra("photo", fileUri.toString()); // 将照片URI传递给预览活动
                    intent.putExtra("pos", position);
                    startActivityForResult(intent, REQUEST_CODE);
                    overridePendingTransition(R.anim.in1, R.anim.out1);
                } else if (myAdapter.getItemViewType(position) == MyAdapter.VIDEO) {
                    Intent intent = new Intent(MainActivity2.this, VideoPreviewActivity.class);
                    VideoFile videoFile = (VideoFile) myAdapter.getmList().get(position);
                    File file = videoFile.getFile();
                    Uri fileUri = Uri.fromFile(file);
                    intent.putExtra("video", fileUri.toString()); // 将视频URI传递给预览活动
                    intent.putExtra("pos", position);
                    startActivityForResult(intent, REQUEST_CODE);
                    overridePendingTransition(R.anim.in1, R.anim.out1);
                }
            }
        });
        myAdapter.setOnItemLongClickListener(new MyAdapter.OnItemLongClickListener() {
            @Override
            public void onLongClick(int position) {
                SweetAlertDialog.DARK_STYLE = true;
                new SweetAlertDialog(MainActivity2.this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("你确定删除吗？")
                        .setContentText("此文件将永久删除")
                        .setConfirmText("我确定删除!")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                if (myAdapter.getItemViewType(position) == MyAdapter.PHOTO) {
                                    ImageFile imageFile = (ImageFile) myAdapter.getmList().get(position);
                                    File file = imageFile.getFile();
                                    String fileID = String.valueOf(file.lastModified());
                                    boolean isDelete = file.delete();
                                    if (isDelete) {
                                        editor.remove(fileID);
                                        deleteOP(position);
                                        sDialog
                                                .setTitleText("删除成功!")
                                                .setConfirmText("好的")
                                                .setConfirmClickListener(null)
                                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                    } else {
                                        sDialog
                                                .setTitleText("删除失败!")
                                                .setConfirmText("好的")
                                                .setConfirmClickListener(null)
                                                .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                    }
                                } else if (myAdapter.getItemViewType(position) == MyAdapter.VIDEO) {
                                    VideoFile videoFile = (VideoFile) myAdapter.getmList().get(position);
                                    File file = videoFile.getFile();
                                    String fileID = String.valueOf(file.lastModified());
                                    boolean isDelete = file.delete();
                                    if (isDelete) {
                                        editor.remove(fileID);
                                        deleteOP(position);
                                        sDialog
                                                .setTitleText("删除成功!")
                                                .setConfirmText("好的")
                                                .setConfirmClickListener(null)
                                                .changeAlertType(SweetAlertDialog.SUCCESS_TYPE);
                                    } else {
                                        sDialog
                                                .setTitleText("删除失败!")
                                                .setConfirmText("好的")
                                                .setConfirmClickListener(null)
                                                .changeAlertType(SweetAlertDialog.ERROR_TYPE);
                                    }
                                }

                            }
                        })
                        .show();
            }
        });
    }


    //将指定目录下的文件全部加载进list里
    private void loadData() {
        sharedPreferences = getSharedPreferences("like", MODE_PRIVATE);
        editor = sharedPreferences.edit();
        Intent intent = getIntent();
        // 获取文件 URI
        String fileUriString = intent.getStringExtra("file_uri");
        // 将字符串转换回 Uri 对象
        Uri fileUri = Uri.parse(fileUriString);
        //根据Uri解析出文件的绝对路径
        File directory = new File(Objects.requireNonNull(fileUri.getPath()));
        //fileUri: /storage/emulated/0/Android/media/com.example.cameraxvideo/CameraXVideo (TV)
        //根据修改时间排序file
        String tempDay = null;
        String lastModifiedString = null;
        File[] files = directory.listFiles();
        if (files != null) {
            Arrays.sort(files, new Comparator<File>() {
                public int compare(File f1, File f2) {
                    return Long.compare(f1.lastModified(), f2.lastModified());
                }
            }.reversed());
            //将指定目录下的指定文件全部加载进list里
            for (File file : files) {
                if (file.isFile()) {
                    String fileName = file.getName();
                    Date lastModifiedDate = new Date(file.lastModified());
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                    lastModifiedString = formatter.format(lastModifiedDate);

                    if (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".jpeg") || fileName.endsWith(".mp4")) {
                        //imageFiles.add(file);
                        if (!lastModifiedString.equals(tempDay)) {
                            tempDay = lastModifiedString;
                            objectsList.add(lastModifiedString);
                        }
                        if (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.endsWith(".jpeg")) {
                            ImageFile imageFile = new ImageFile();
                            imageFile.setFile(file);
                            objectsList.add(imageFile);
                        }
                        if (fileName.endsWith(".mp4")) {
                            VideoFile videoFile = new VideoFile();
                            videoFile.setFile(file);
                            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                            retriever.setDataSource(file.getAbsolutePath());
                            long duration = Long.parseLong(Objects.requireNonNull(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)));
                            // 将时长转换为秒
                            long seconds = TimeUnit.MILLISECONDS.toSeconds(duration);
                            String formattedDuration = String.format("%02d:%02d", seconds / 60, seconds % 60);
                            videoFile.setTime(formattedDuration);
                            objectsList.add(videoFile);
                        }
                    }
                }
            }
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                int pos = data.getIntExtra("pos_delete", -1);
                deleteOP(pos);
                // 处理返回的数据
            } else if (resultCode == RESULT_CHANGE) {
                int pos = data.getIntExtra("pos_rename", -1);
                String FileUriString = data.getStringExtra("new_photo");
//                Log.d("cyx", "FileUriString is: "+FileUriString);
//                FileUriString is: file:///storage/emulated/0/Android/media/com.example.cameraxvideo/CameraXVideo/just.mp4
                int Type = data.getIntExtra("fileType", 0);
                Uri fileUri = Uri.parse(FileUriString);
                File file = new File(Objects.requireNonNull(fileUri.getPath()));
                if (Type == 111) {
                    ImageFile imageFile = (ImageFile) objectsList.get(pos);
                    imageFile.setFile(file);
                    objectsList.set(pos, imageFile);
                } else if (Type == 222) {
                    VideoFile videoFile = (VideoFile) objectsList.get(pos);
                    videoFile.setFile(file);
                    objectsList.set(pos, videoFile);
                }
                myAdapter.notifyItemChanged(pos);
                // 处理重命名操作

            } else if (resultCode == RESULT_CANCELED) {

                //普通返回时的逻辑操作
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu, menu);
        inflater.inflate(R.menu.recycleview_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.search_item);
        SearchView searchView = (SearchView) searchItem.getActionView();
        int width = getResources().getDisplayMetrics().widthPixels;   //找到屏幕宽度
        Objects.requireNonNull(searchView).setMaxWidth((int) (0.6 * width));   //设置搜索框展开宽度
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 0) {
                    filterData(newText);
                    is_filter = true;
                } else {
                    if (is_view_like) {
                        myAdapter.setData(filteredLikeData);
                    } else {
                        myAdapter.setData(objectsList);
                    }
                    is_filter = false;
                }
                return true;
            }
        });
        return true;
    }

    private void filterData(String filter) {
        List<Object> filteredData = new ArrayList<>();
        if (!is_view_like) {
            for (Object item : objectsList) {
                if (item instanceof VideoFile videoFile) {
                    File file = videoFile.getFile();
                    if (file.getName().contains(filter)) {
                        filteredData.add(item);
                    }
                }
                if (item instanceof ImageFile imageFile) {
                    File file = imageFile.getFile();
                    if (file.getName().contains(filter)) {
                        filteredData.add(item);
                    }
                }
            }
        } else {
            for (Object item : filteredLikeData) {
                if (item instanceof VideoFile videoFile) {
                    File file = videoFile.getFile();
                    if (file.getName().contains(filter)) {
                        filteredData.add(item);
                    }
                }
                if (item instanceof ImageFile imageFile) {
                    File file = imageFile.getFile();
                    if (file.getName().contains(filter)) {
                        filteredData.add(item);
                    }
                }
            }
        }


        myAdapter.setData(filteredData);
    }

    private void filterLikeData() {
        filteredLikeData = new ArrayList<>();
        for (Object item : objectsList) {
            if (item instanceof VideoFile videoFile) {
                File file = videoFile.getFile();
                if (sharedPreferences.getBoolean(String.valueOf(file.lastModified()), false)) {
                    filteredLikeData.add(item);
                }
            }
            if (item instanceof ImageFile imageFile) {
                File file = imageFile.getFile();
                if (sharedPreferences.getBoolean(String.valueOf(file.lastModified()), false)) {
                    filteredLikeData.add(item);
                }
            }
        }
        myAdapter.setData(filteredLikeData);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_view_like) {
            filterLikeData();
            is_view_like = true;
        } else if (item.getItemId() == R.id.action_view_all) {
            myAdapter.setData(objectsList);
            is_view_like = false;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteOP(int pos) {
        if (!is_filter && !is_view_like) {
            //处理删除操作
            // 获取从第二个 Activity 返回的数据
            if (objectsList.size() == pos + 1 && objectsList.get(pos - 1) instanceof String) { //所有照片的最后一张，是该日期下的最后一张照片，前面只有日期
                objectsList.remove(pos - 1);
                myAdapter.notifyItemRemoved(pos - 1);
                objectsList.remove(pos - 1);
                myAdapter.notifyItemRemoved(pos - 1);
            } else if (objectsList.size() == pos + 1 && !(objectsList.get(pos - 1) instanceof String)) {//所有照片的最后一张，该日期下的最后一张照片，但前面还有照片
                objectsList.remove(pos);
                myAdapter.notifyItemRemoved(pos);
            } else if (objectsList.get(pos + 1) instanceof String && objectsList.get(pos - 1) instanceof String) {//是该日期下的最后一张照片，前面只有日期，后面也只有日期
                objectsList.remove(pos - 1);
                myAdapter.notifyItemRemoved(pos - 1);
                objectsList.remove(pos - 1);
                myAdapter.notifyItemRemoved(pos - 1);
            } else {// 通知适配器数据已经更改
                objectsList.remove(pos);
                myAdapter.notifyItemRemoved(pos);
            }
        } else {
            if (is_view_like && is_filter) {
                filteredLikeData.remove(pos);
            }
            myAdapter.getmList().remove(pos);
            myAdapter.notifyItemRemoved(pos);
            objectsList.clear();
            loadData();
        }
    }

}