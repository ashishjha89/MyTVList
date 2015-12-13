package com.mytvlist.list;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.mytvlist.R;
import com.mytvlist.activity.EpisodeDetailActivity;
import com.mytvlist.utils.Utils;

import java.util.HashMap;
import java.util.List;

/**
 * Created by ashish on 15/8/15.
 */
public class SeasonListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private List<String> mSeasonNumberList; // header titles
    // child data in format of header title, child title
    private List<String> mSeasonNumberTraktList;
    private HashMap<String, List<String>> mEpisodeMap;
    private HashMap<String, List<String>> mEpisodeTraktMap;

    private String mShowTraktId, mShowTitle, mShowTiming, mShowRuntime;

    private final String TAG = "SeasonListAdapter";

    public SeasonListAdapter(Context context, String showTraktId, List<String> listDataHeader, List<String> seasonTraktList,
                             HashMap<String, List<String>> listChildData, HashMap<String, List<String>> listChildTraktData,
                             String title, String timing, String runtime) {
        this.mContext = context;
        mShowTraktId = showTraktId;
        this.mSeasonNumberList = listDataHeader;
        this.mSeasonNumberTraktList = seasonTraktList;
        this.mEpisodeMap = listChildData;
        this.mEpisodeTraktMap = listChildTraktData;
        mShowTitle = title;
        mShowTiming = timing;
        mShowRuntime = runtime;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.mEpisodeMap.get(this.mSeasonNumberList.get(groupPosition))
                .get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = (String) getChild(groupPosition, childPosition);

        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.episode_item, null);
        }

        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.episodeItem);

        txtListChild.setText(childText);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Log.d(TAG, "Child View OnClick() childPosition=" + childPosition + " groupPosition=" + groupPosition);
                String seasonTraktId = mSeasonNumberTraktList.get(groupPosition);
                String seasonNumber = mSeasonNumberList.get(groupPosition);
                String episodeTraktId = mEpisodeTraktMap.get(seasonNumber).get(childPosition);
                Intent intent = new Intent(mContext, EpisodeDetailActivity.class);
                intent.putExtra(Utils.SHOW_TRAKT_ID, mShowTraktId);
                intent.putExtra(Utils.SEASON_TRAKT_ID, seasonTraktId);
                intent.putExtra(Utils.NUMBER, seasonNumber);
                intent.putExtra(Utils.EPISODE_NUMBER, ((childPosition + 1) + ""));
                intent.putExtra(Utils.TRAKT_ID, episodeTraktId);
                intent.putExtra(Utils.TITLE, mShowTitle);
                intent.putExtra(Utils.AIRS_TIME, mShowTiming);
                intent.putExtra(Utils.RUNTINME, mShowRuntime);
                mContext.startActivity(intent);
            }
        });
        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.mEpisodeMap.get(this.mSeasonNumberList.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.mSeasonNumberList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return this.mSeasonNumberList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.single_season, null);
        }

        TextView lblListHeader = (TextView) convertView
                .findViewById(R.id.seasonNo);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText("Season " + headerTitle);

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}