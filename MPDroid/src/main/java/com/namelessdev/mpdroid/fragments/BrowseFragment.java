/*
 * Copyright (C) 2010-2014 The MPDroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.namelessdev.mpdroid.fragments;

import com.namelessdev.mpdroid.MPDApplication;
import com.namelessdev.mpdroid.R;
import com.namelessdev.mpdroid.adapters.ArrayIndexerAdapter;
import com.namelessdev.mpdroid.helpers.MPDAsyncHelper.AsyncExecListener;
import com.namelessdev.mpdroid.tools.Tools;

import org.a0z.mpd.MPDStatus;
import org.a0z.mpd.exception.MPDServerException;
import org.a0z.mpd.item.Item;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public abstract class BrowseFragment extends Fragment implements OnMenuItemClickListener,
        AsyncExecListener, OnItemClickListener,
        OnRefreshListener {

    public static final int ADD = 0;

    public static final int ADD_PLAY = 2;

    public static final int ADD_REPLACE = 1;

    public static final int ADD_REPLACE_PLAY = 4;

    public static final int ADD_TO_PLAYLIST = 3;

    public static final int MAIN = 0;

    public static final int PLAYLIST = 3;

    private static final int MIN_ITEMS_BEFORE_FASTSCROLL = 50;

    private static final String TAG = "BrowseFragment";

    protected MPDApplication mApp = MPDApplication.getInstance();

    protected List<? extends Item> mItems = null;

    protected int mJobID = -1;

    protected AbsListView mList;

    protected TextView mLoadingTextView;

    protected View mLoadingView;

    protected View mNoResultView;

    protected PullToRefreshLayout mPullToRefreshLayout;

    String mContext;

    int mIrAdd, mIrAdded;

    private boolean mFirstUpdateDone = false;

    public BrowseFragment(int rAdd, int rAdded, String pContext) {
        super();
        mIrAdd = rAdd;
        mIrAdded = rAdded;

        mContext = pContext;

        setHasOptionsMenu(false);
    }

    protected abstract void add(Item item, boolean replace, boolean play);

    protected abstract void add(Item item, String playlist);

    @Override
    public void asyncExecSucceeded(int jobID) {
        if (mJobID == jobID) {
            updateFromItems();
        }

    }

    protected void asyncUpdate() {

    }

    // Override if you want setEmptyView to be called on the list even if you
    // have a header
    protected boolean forceEmptyView() {
        return false;
    }

    protected ListAdapter getCustomListAdapter() {
        return new ArrayIndexerAdapter(getActivity(), R.layout.simple_list_item_1, mItems);
    }

    /*
     * Override this to display a custom loading text
     */
    public int getLoadingText() {
        return R.string.loading;
    }

    /**
     * Should return the minimum number of songs in the queue before the
     * fastscroll thumb is shown
     */
    protected int getMinimumItemsCountBeforeFastscroll() {
        return MIN_ITEMS_BEFORE_FASTSCROLL;
    }

    /*
     * Override this to display a custom activity title
     */
    public String getTitle() {
        return "";
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Activity activity = getActivity();
        if (activity != null) {
            final ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenu.ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;

        int index = (int) info.id;
        if (index >= 0 && mItems.size() > index) {
            menu.setHeaderTitle(mItems.get((int) info.id).toString());
            // If in simple mode, show "Play" (add, replace & play), "Add to queue" and "Add to playlist"
            if (mApp.isInSimpleMode()) {
                android.view.MenuItem playItem = menu.add(ADD_REPLACE_PLAY,
                        ADD_REPLACE_PLAY, 0, R.string.play);
                playItem.setOnMenuItemClickListener(this);
                android.view.MenuItem addItem = menu.add(ADD, ADD, 0, R.string.addToQueue);
                addItem.setOnMenuItemClickListener(this);
            } else {
                android.view.MenuItem addItem = menu.add(ADD, ADD, 0, mIrAdd);
                addItem.setOnMenuItemClickListener(this);
                android.view.MenuItem addAndReplaceItem = menu.add(ADD_REPLACE, ADD_REPLACE, 0,
                        R.string.addAndReplace);
                addAndReplaceItem.setOnMenuItemClickListener(this);
                android.view.MenuItem addAndReplacePlayItem = menu.add(ADD_REPLACE_PLAY,
                        ADD_REPLACE_PLAY, 0, R.string.addAndReplacePlay);
                addAndReplacePlayItem.setOnMenuItemClickListener(this);
                android.view.MenuItem addAndPlayItem = menu.add(ADD_PLAY, ADD_PLAY, 0,
                        R.string.addAndPlay);
                addAndPlayItem.setOnMenuItemClickListener(this);
            }

            if (R.string.addPlaylist != mIrAdd && R.string.addStream != mIrAdd) {
                int id = 0;
                SubMenu playlistMenu = menu.addSubMenu(R.string.addToPlaylist);
                android.view.MenuItem item = playlistMenu.add(ADD_TO_PLAYLIST, id++, (int) info.id,
                        R.string.newPlaylist);
                item.setOnMenuItemClickListener(this);

                try {
                    List<Item> playlists = mApp.oMPDAsyncHelper.oMPD.getPlaylists();

                    if (null != playlists) {
                        for (Item pl : playlists) {
                            item = playlistMenu.add(ADD_TO_PLAYLIST, id++, (int) info.id,
                                    pl.getName());
                            item.setOnMenuItemClickListener(this);
                        }
                    }
                } catch (MPDServerException e) {
                    Log.e(TAG, "Failed to parse playlists.", e);
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.browse, container, false);
        mList = (ListView) view.findViewById(R.id.list);
        registerForContextMenu(mList);
        mList.setOnItemClickListener(this);
        mLoadingView = view.findViewById(R.id.loadingLayout);
        mLoadingTextView = (TextView) view.findViewById(R.id.loadingText);
        mNoResultView = view.findViewById(R.id.noResultLayout);
        mLoadingTextView.setText(getLoadingText());
        mPullToRefreshLayout = (PullToRefreshLayout) view.findViewById(R.id.pullToRefresh);

        return view;
    }

    @Override
    public void onDestroy() {
        try {
            mApp.oMPDAsyncHelper.removeAsyncExecListener(this);
        } catch (Exception e) {
            Log.e(TAG, "Error while destroying BrowseFragment", e);
        }
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        // help out the GC; imitated from ListFragment source
        mLoadingView = null;
        mLoadingTextView = null;
        mNoResultView = null;
        super.onDestroyView();
    }

    @Override
    public boolean onMenuItemClick(final android.view.MenuItem item) {
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getGroupId()) {
            case ADD_REPLACE_PLAY:
            case ADD_REPLACE:
            case ADD:
            case ADD_PLAY:
                mApp.oMPDAsyncHelper.execAsync(new Runnable() {
                    @Override
                    public void run() {
                        boolean replace = false;
                        boolean play = false;
                        switch (item.getGroupId()) {
                            case ADD_REPLACE_PLAY:
                                replace = true;
                                play = true;
                                break;
                            case ADD_REPLACE:
                                replace = true;
                                break;
                            case ADD_PLAY:
                                final MPDStatus status = mApp.oMPDAsyncHelper.oMPD.getStatus();

                                /**
                                 * Let the user know if we're not going to play the added music.
                                 */
                                if (status.isRandom() && status.isState(MPDStatus.STATE_PLAYING)) {
                                    Tools.notifyUser(R.string.notPlayingInRandomMode);
                                } else {
                                    play = true;
                                }
                                break;
                        }
                        add(mItems.get((int) info.id), replace, play);
                    }
                });
                break;
            case ADD_TO_PLAYLIST: {
                final EditText input = new EditText(getActivity());
                final int id = item.getOrder();
                if (item.getItemId() == 0) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.playlistName)
                            .setMessage(R.string.newPlaylistPrompt)
                            .setView(input)
                            .setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                            final String name = input.getText().toString().trim();
                                            if (null != name && name.length() > 0) {
                                                mApp.oMPDAsyncHelper.execAsync(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        add(mItems.get(id), name);
                                                    }
                                                });
                                            }
                                        }
                                    })
                            .setNegativeButton(android.R.string.cancel,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                            // Do nothing.
                                        }
                                    }).show();
                } else {
                    add(mItems.get(id), item.getTitle().toString());
                }
                break;
            }
            default:
                final String name = item.getTitle().toString();
                final int id = item.getOrder();
                mApp.oMPDAsyncHelper.execAsync(new Runnable() {
                    @Override
                    public void run() {
                        add(mItems.get(id), name);
                    }
                });
                break;
        }
        return false;
    }

    @Override
    public void onRefreshStarted(View view) {
        mPullToRefreshLayout.setRefreshComplete();
        updateList();
    }

    @Override
    public void onStart() {
        super.onStart();
        mApp.setActivity(getActivity());
        if (!mFirstUpdateDone) {
            mFirstUpdateDone = true;
            updateList();
        }
    }

    @Override
    public void onStop() {
        mApp.unsetActivity(getActivity());
        super.onStop();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (mItems != null) {
            mList.setAdapter(getCustomListAdapter());
        }
        refreshFastScrollStyle();
        if (mPullToRefreshLayout != null) {
            ActionBarPullToRefresh.from(getActivity())
                    .allChildrenArePullable()
                    .listener(this)
                    .setup(mPullToRefreshLayout);
        }
    }

    /**
     * This method is used for the fastcroll visibility decision.<br/>
     * Don't override this if you want to change the fastscroll style, override
     * {@link #refreshFastScrollStyle(boolean)} instead.
     */
    protected void refreshFastScrollStyle() {
        refreshFastScrollStyle(mItems != null
                && mItems.size() >= getMinimumItemsCountBeforeFastscroll());
    }

    /**
     * This is required because setting the fast scroll prior to KitKat was
     * important because of a bug. This bug has since been corrected, but the
     * opposite order is now required or the fast scroll will not show.
     *
     * @param shouldShowFastScroll If the fast scroll should be shown or not
     */
    protected void refreshFastScrollStyle(final boolean shouldShowFastScroll) {
        if (shouldShowFastScroll) {
            refreshFastScrollStyle(View.SCROLLBARS_INSIDE_INSET, true);
        } else {
            refreshFastScrollStyle(View.SCROLLBARS_INSIDE_OVERLAY, false);
        }
    }

    /**
     * This is a helper method to workaround shortcomings of the fast scroll API.
     *
     * @param scrollbarStyle  The {@code View} scrollbar style.
     * @param isAlwaysVisible The visibility of the scrollbar.
     */
    final void refreshFastScrollStyle(final int scrollbarStyle, final boolean isAlwaysVisible) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mList.setFastScrollAlwaysVisible(isAlwaysVisible);
            mList.setScrollBarStyle(scrollbarStyle);
        } else {
            mList.setScrollBarStyle(scrollbarStyle);
            mList.setFastScrollAlwaysVisible(isAlwaysVisible);
        }
    }

    public void scrollToTop() {
        try {
            mList.setSelection(-1);
        } catch (Exception e) {
            // What if the list is empty or some other bug ? I don't want any
            // crashes because of that
        }
    }

    public void setActivityTitle(String title) {
        getActivity().setTitle(title);
    }

    /**
     * Update the view from the items list if items is set.
     */
    public void updateFromItems() {
        if (getView() == null) {
            // The view has been destroyed, bail.
            return;
        }
        if (mPullToRefreshLayout != null) {
            mPullToRefreshLayout.setEnabled(true);
        }
        if (mItems != null) {
            mList.setAdapter(getCustomListAdapter());
        }
        try {
            if (forceEmptyView()
                    || ((mList instanceof ListView)
                    && ((ListView) mList).getHeaderViewsCount() == 0)) {
                mList.setEmptyView(mNoResultView);
            } else {
                if (mItems == null || mItems.isEmpty()) {
                    mNoResultView.setVisibility(View.VISIBLE);
                }
            }
        } catch (final Exception e) {
            Log.e(TAG, "Exception.", e);
        }

        mLoadingView.setVisibility(View.GONE);
        refreshFastScrollStyle();
    }

    public void updateList() {
        mList.setAdapter(null);
        mNoResultView.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.VISIBLE);
        if (mPullToRefreshLayout != null) {
            mPullToRefreshLayout.setEnabled(false);
        }

        // Loading Artists asynchronous...
        mApp.oMPDAsyncHelper.addAsyncExecListener(this);
        mJobID = mApp.oMPDAsyncHelper.execAsync(new Runnable() {
            @Override
            public void run() {
                asyncUpdate();
            }
        });
    }
}
