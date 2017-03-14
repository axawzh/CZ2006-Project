package com.klipspringercui.sgbusgo;

import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.widget.SearchView;

public class SearchActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        activateToolBar(false);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
        SearchableInfo searchableInfo = searchManager.getSearchableInfo(getComponentName());
        searchView.setSearchableInfo(searchableInfo);
        searchView.setIconified(false);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                query = query.trim();
                if (query.length() > 0) {
                    Intent intent = getIntent();
                    Bundle bundle = new Bundle();
                    String caller = getCallingActivity().getClassName().substring(28);
                    switch (caller) {
                        case "BusServiceSelectionActivity":
                            bundle.putString(BusServiceSelectionActivity.BUS_SERVICE_SEARCH_KEYWORD, query);
                            break;
                        case "BusStopSelectionActivity":
                            bundle.putString(BusStopSelectionActivity.BUS_STOP_SEARCH_KEYWORD, query);
                            break;
                        default:
                            break;
                    }
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                } else {
                    setResult(RESULT_CANCELED);
                }
                finish();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                setResult(RESULT_CANCELED);
                finish();
                return true;
            }
        });
        return true;
    }



}
