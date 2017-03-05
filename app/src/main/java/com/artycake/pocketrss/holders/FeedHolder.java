package com.artycake.pocketrss.holders;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.artycake.pocketrss.R;
import com.artycake.pocketrss.models.Article;
import com.artycake.pocketrss.utils.TextHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by artycake on 2/28/17.
 */

public class FeedHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.article_cover)
    ImageView articleCover;
    @BindView(R.id.article_cover_text)
    TextView articleCoverText;
    @BindView(R.id.source_name)
    TextView sourceName;
    @BindView(R.id.source_domain)
    TextView sourceDomain;
    @BindView(R.id.category_name)
    TextView categoryName;
    @BindView(R.id.article_date)
    TextView articleDate;
    @BindView(R.id.article_title)
    TextView articleTitle;

    public FeedHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void updateUI(Article article) {
        articleCover.setColorFilter(article.getSource().getColor());
        articleCoverText.setText(TextHelper.getShortSourceName(article.getSource()));
        sourceName.setText(article.getSource().getName());
        sourceDomain.setText(TextHelper.getSourceDomain(article.getSource()));
        categoryName.setText(article.getCategory().getName());
        articleDate.setText(TextHelper.getArticleDate(article));
        articleTitle.setText(article.getTitle());
    }
}
