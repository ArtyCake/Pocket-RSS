package com.artycake.pocketrss.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.artycake.pocketrss.R;
import com.artycake.pocketrss.holders.SourceGroupHolder;
import com.artycake.pocketrss.holders.SourceHolder;
import com.artycake.pocketrss.models.Category;
import com.artycake.pocketrss.models.Source;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by artycake on 3/2/17.
 */

public class CategoriesAdapter extends BaseExpandableListAdapter {
    private List<Category> categories;
    private ArrayList<Category> groups = new ArrayList<>();
    private OnSettingsClickListener onSettingsClickListener;

    public CategoriesAdapter(List<Category> categories) {
        this.categories = categories;
        updateGroups();
    }

    public void setOnSettingsClickListener(OnSettingsClickListener onSettingsClickListener) {
        this.onSettingsClickListener = onSettingsClickListener;
    }

    private void updateGroups() {
        groups.clear();
        for (Category category : categories) {
            if (category.getSources().size() == 0) {
                continue;
            }
            groups.add(category);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        updateGroups();
        super.notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return groups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groups.get(groupPosition).getSources().size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return groups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return groups.get(groupPosition).getSources().get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        SourceGroupHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_source_group, parent, false);
            holder = new SourceGroupHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (SourceGroupHolder) convertView.getTag();
        }
        holder.updateUI(groups.get(groupPosition).getName());
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        SourceHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_source, parent, false);
            holder = new SourceHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (SourceHolder) convertView.getTag();
        }
        final Source source = groups.get(groupPosition).getSources().get(childPosition);
        if (onSettingsClickListener != null) {
            holder.setSettingsClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSettingsClickListener.onClick(source);
                }
            });
        }
        holder.updateUI(source);

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public interface OnSettingsClickListener {
        void onClick(Source source);
    }
}
