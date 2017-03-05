package com.artycake.pocketrss.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.artycake.pocketrss.R;
import com.artycake.pocketrss.holders.FeedHolder;
import com.artycake.pocketrss.holders.NativeAdHolder;
import com.artycake.pocketrss.models.Article;

import java.util.List;

/**
 * Created by artycake on 2/28/17.
 */

public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ITEM_VIEW_TYPE = 267;
    private static final int AD_VIEW_TYPE = 981;
    private static final int AD_POSITION = 25;

    private List<Article> articles;
    private OnItemClick onItemClick;

    public FeedAdapter(List<Article> articles) {
        this.articles = articles;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == AD_VIEW_TYPE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed_ad, parent, false);
            return new NativeAdHolder(view, parent);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_feed, parent, false);
            return new FeedHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == AD_VIEW_TYPE) {
            ((NativeAdHolder) holder).updateAd();
            return;
        }
        ((FeedHolder) holder).updateUI(articles.get(position - position / AD_POSITION));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClick != null) {
                    onItemClick.onClick(articles.get(holder.getAdapterPosition() - holder.getAdapterPosition() / AD_POSITION));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return articles.size() + articles.size() / AD_POSITION;
    }

    public void setOnItemClick(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public interface OnItemClick {
        void onClick(Article article);
    }

    @Override
    public int getItemViewType(int position) {
        if (position > 0 && position % AD_POSITION == 0) {
            return AD_VIEW_TYPE;
        }
        return ITEM_VIEW_TYPE;
    }
}
