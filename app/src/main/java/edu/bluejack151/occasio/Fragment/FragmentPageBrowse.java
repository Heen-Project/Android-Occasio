package edu.bluejack151.occasio.Fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import edu.bluejack151.occasio.Activity.DetailActivity;
import edu.bluejack151.occasio.Adapter.MyGridViewAdapter;
import edu.bluejack151.occasio.Class.CurrentMasterData;
import edu.bluejack151.occasio.Class.TimelineDetailPassingHelper;
import edu.bluejack151.occasio.R;

public class FragmentPageBrowse extends Fragment {

    private View v;
    private GridView gridView;
    private CurrentMasterData currentMasterData;
    private MyGridViewAdapter myGridViewAdapter;
    private ImageView btnBrowseSearch;
    private EditText txtBrowseSearch;
    private LinearLayout blankView;
    private InputMethodManager imm;

    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_page_browse, container, false);

        imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

        gridView = (GridView) v.findViewById(R.id.grid_browse);
        btnBrowseSearch = (ImageView) v.findViewById(R.id.browse_btn_search);
        txtBrowseSearch = (EditText) v.findViewById(R.id.browse_text_search);
        blankView = (LinearLayout) v.findViewById(R.id.browse_blank);
        currentMasterData = CurrentMasterData.getInstance();

        btnBrowseSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!txtBrowseSearch.getText().toString().trim().equals("")) {
                    imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                    currentMasterData.browse_tryLoad(v.getContext(), gridView, myGridViewAdapter, txtBrowseSearch.getText().toString().trim());
                    blankView.setVisibility(View.GONE);
                } else {
                    blankView.setVisibility(View.VISIBLE);
                }
            }
        });
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(v.getContext(), DetailActivity.class);
                Bundle bundle = new Bundle();
                Bundle bundle_src = new Bundle();
                bundle_src.putString("source", "browse");
                bundle.putSerializable("timelineDataDetail", new TimelineDetailPassingHelper(currentMasterData.getBrowseDataList().get(position)));
                intent.putExtras(bundle);
                intent.putExtras(bundle_src);
                v.getContext().startActivity(intent);
            }
        });

        return v;
    }
}
