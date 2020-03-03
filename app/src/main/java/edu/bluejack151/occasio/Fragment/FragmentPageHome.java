package edu.bluejack151.occasio.Fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.RelativeLayout;

import edu.bluejack151.occasio.Adapter.MyRecyclerviewAdapter;
import edu.bluejack151.occasio.Class.CurrentMasterData;
import edu.bluejack151.occasio.R;

public class FragmentPageHome extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private View v;

    private MyRecyclerviewAdapter myRecyclerviewAdapter;
    private Boolean loading = false, pauseAppend = false;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CurrentMasterData currentMasterData;
    private RelativeLayout progressBar;
    private int pastVisiblesItems, visibleItemCount, totalItemCount;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_page_home, container, false);
        recyclerView = (RecyclerView) v.findViewById(R.id.home_list);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        progressBar = (RelativeLayout) v.findViewById(R.id.load_more_progressbar);
        currentMasterData = CurrentMasterData.getInstance();
        initRefreshData();
        if (getActivity().getIntent().getExtras() != null) {
            if (this.getActivity().getIntent().getExtras().getBoolean("refresh")) {
                getActivity().getIntent().removeExtra("refresh");
                reloadData();
            }
        } else {
            currentMasterData.firstLoad(v.getContext(), myRecyclerviewAdapter, recyclerView, linearLayoutManager);
        }
        viewScrollListener();
        return v;
    }

    private void initRefreshData() {
        swipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.home_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);
    }

    private void reloadData() {
        swipeRefreshLayout.setRefreshing(true);
        currentMasterData.tryLoad(v.getContext(), myRecyclerviewAdapter, recyclerView, linearLayoutManager);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void appendData() {
        pauseAppend = true;
        progressBar.setVisibility(View.VISIBLE);
        pauseAppend = currentMasterData.loadNextImageData_withReturnValue(v.getContext(), recyclerView, linearLayoutManager, progressBar);
    }

    private void viewScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    if (pauseAppend == false) {
                        loading = true;
                    }
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = linearLayoutManager.getChildCount();
                totalItemCount = linearLayoutManager.getItemCount();
                pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();
                if (loading && (visibleItemCount + pastVisiblesItems) == totalItemCount && dy > 0 && visibleItemCount > 1) {
                    loading = false;
                    appendData();
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        reloadData();
    }
}
