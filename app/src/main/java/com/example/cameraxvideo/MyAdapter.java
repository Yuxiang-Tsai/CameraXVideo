package com.example.cameraxvideo;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.example.cameraxvideo.pojo.ImageFile;
import com.example.cameraxvideo.pojo.VideoFile;

import java.io.File;
import java.util.List;

public class MyAdapter extends FocusRecyclerView.Adapter<FocusRecyclerView.ViewHolder> {

    public static final int DATE = 0;
    public static final int PHOTO = 1;
    public static final int VIDEO = 2;
    private final Context context;
    private List<Object> mList;
    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;
    private RequestOptions options;

    public MyAdapter(Context context) {
        this.context = context;
    }

    public List<Object> getmList() {
        return mList;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener longClickListener) {
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public FocusRecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == DATE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.date_item, parent, false);
            return new DateHolder(view);
        } else if (viewType == PHOTO) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photo_item, parent, false);
            return new PhotoHolder(view);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_item, parent, false);
            return new VideoHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull FocusRecyclerView.ViewHolder holder, int position) {
        options = new RequestOptions().transform(new RoundedCorners(20));
        if (holder instanceof DateHolder) {
            ((DateHolder) holder).textView.setText((String) mList.get(position));
            holder.itemView.setFocusable(false);
        } else if (holder instanceof PhotoHolder) {
            ImageFile imageFile = (ImageFile) mList.get(position);
            File file = imageFile.getFile();
            ((PhotoHolder) holder).picName.setText(imageFile.getFile().getName());
            Glide.with(context)
                    .asBitmap()
                    .load(file)
                    .apply(options)
                    .into(((PhotoHolder) holder).imageView);
        } else if (holder instanceof VideoHolder) {
            VideoFile videoFile = (VideoFile) mList.get(position);
            File file = videoFile.getFile();
            //加载每个视频的时长
            ((VideoHolder) holder).duration.setText(videoFile.getTime());
            ((VideoHolder) holder).videoName.setText(videoFile.getFile().getName());

            // 使用 Glide 加载视频的第一帧图像，并将其设置为 ImageView 的位图
            Glide.with(context)
                    .asBitmap()
                    .load(file)
                    .apply(options)
                    .into(((VideoHolder) holder).imageView);
        }
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                longClickListener.onLongClick(holder.getAdapterPosition());
                return true;
            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(holder.getAdapterPosition());
            }
        });
        holder.itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (holder instanceof PhotoHolder || holder instanceof VideoHolder) {
                    if (hasFocus) {//当选中这个View时做一些你所需要的操作
                        v.setBackgroundResource(R.drawable.item_boder);
                        ScaleAnimation animation = new ScaleAnimation(1.0f, 1.08f, 1.0f, 1.08f,
                                ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
                        animation.setDuration(400);
                        animation.setFillAfter(true);
                        v.startAnimation(animation);
                    } else {
                        v.setBackgroundResource(0);
                        ScaleAnimation animation = new ScaleAnimation(1.08f, 1.0f, 1.08f, 1.0f,
                                ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
                        animation.setDuration(400);
                        animation.setFillAfter(true);
                        v.startAnimation(animation);
                    }
                } else {
                    if (hasFocus) {
                        v.setBackgroundResource(R.drawable.item_boder);
                    } else {
                        v.setBackgroundResource(0);
                    }

                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (mList.get(position) instanceof String) {
            return DATE;
        } else if (mList.get(position) instanceof VideoFile) {
            return VIDEO;
        } else {
            return PHOTO;
        }

    }

    public void setData(List<Object> mList) {
        this.mList = mList;
        notifyDataSetChanged();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        FocusRecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof final GridLayoutManager gridManager) {
            gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {  //如果是DATE类型的itemview，就占据这个行
                    return getItemViewType(position) == DATE
                            ? gridManager.getSpanCount() : 1;
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onClick(int position);
    }

    public interface OnItemLongClickListener {
        void onLongClick(int position);
    }

    static class DateHolder extends FocusRecyclerView.ViewHolder {
        TextView textView;

        public DateHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.date);
        }
    }

    static class PhotoHolder extends FocusRecyclerView.ViewHolder {
        ImageView imageView;
        TextView picName;

        public PhotoHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            picName = itemView.findViewById(R.id.picName);
        }
    }

    static class VideoHolder extends FocusRecyclerView.ViewHolder {
        ImageView imageView, iconView;
        TextView duration, videoName;

        public VideoHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image1);
            iconView = imageView.findViewById(R.id.image2);
            duration = itemView.findViewById(R.id.duration);
            videoName = itemView.findViewById(R.id.videoName);
        }
    }


}
