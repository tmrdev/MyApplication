package org.timreynolds.myapplication;

import org.timreynolds.myapplication.adapter.SearchAdapter;
import org.timreynolds.myapplication.api.ApiManager;
import org.timreynolds.myapplication.database.SearchEntryDB;
import org.timreynolds.myapplication.database.SearchEntryProvider;
import org.timreynolds.myapplication.model.Items;
import org.timreynolds.myapplication.model.ResponseData;
import org.timreynolds.myapplication.utility.Logger;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by Tim Reynolds
 * https://developer.android.com/training/volley/request.html
 * Allow user to submit search query to Google+ API and return search results
 * Google+ API Key required for searches: see https://developers.google.com/+/web/api/rest/oauth
 */

public class SearchFragment extends Fragment implements View.OnClickListener {

    private static String TAG = MainActivity.class.getSimpleName();

    private View view;
    // data adapter for search results
    private SearchAdapter mAdapter;

    // NOTE: need to configure Google+ API access key through Google Dev Console
    // Visit the following URL to learn how to create an app and api key from within the console
    // For Google+ API search, you can create an Public API access key (Select Server key with no ip restrictions for testing
    // visit the url below to get an api key
    // https://developers.google.com/+/web/api/rest/oauth
    private String googleAPIKEY = "";

    private EditText mSearchField;
    private ImageButton mSearchFieldButton;
    public String mSearchText;
    ListView listView;

    List<Items> searchList;
    SearchAdapter searchAdapter;
    private int myTotalResults;
    private String orderBy;

    public SearchFragment() {

    }

    /**
     * onCreateView
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.fragment_search, container, false);
        view = inflater.inflate(R.layout.fragment_search, container, false);
        mSearchField = (EditText) view.findViewById(R.id.search_field);
        mAdapter = new SearchAdapter(getActivity(), R.layout.search_list_item, searchList);
        listView = (ListView) view.findViewById(R.id.list1);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
                String postURL = searchList.get(position).getTitle();
                if (postURL != null && !postURL.isEmpty()) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(searchList.get(position).getUrl()));
                    startActivity(i);
                }
            }
        });

        return view;
    }

    /**
     * onActivityCreated
     *
     * @param savedInstanceState
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mSearchFieldButton = (ImageButton) view.findViewById(R.id.search_text_button);
        mSearchFieldButton.setOnClickListener(this);
    }

    /**
     * searchGooglePlus - create JSON Object request for Google+ API search, then add request to MyApplication/Volley instance
     */
    private void searchGooglePlus() {
        // check for network connection before making api search call
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String totalResults = settings.getString("search_result_total_list_preference", "null");
        if(totalResults == "null"){
            myTotalResults = 10;
        }
        else{
            myTotalResults = Integer.parseInt(totalResults);
        }
        orderBy = settings.getString("search_result_orderby_preference", "best");

        Logger.i(TAG, "dump total results -> " + totalResults);
        Logger.i(TAG, "dump orderby -> " + orderBy);

        if(MyApplication.isConnected()) {
            ApiManager.getService().getData(mSearchText, googleAPIKEY, myTotalResults, orderBy, new Callback<ResponseData>() {
                @Override
                public void success(ResponseData responseData, Response response) {
                    ResponseData data = (ResponseData) responseData;
                    searchList = responseData.getItems();
                    searchAdapter = new SearchAdapter(getActivity(), R.layout.search_list_item, searchList);
                    listView.setAdapter(searchAdapter);
                    searchAdapter.notifyDataSetChanged();
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.google_search_error), Toast.LENGTH_SHORT).show();
                    if (retrofitError.getResponse() != null) {
                        Logger.i("TAG", "google plus search error : " + retrofitError.getCause().toString());
                    }

                }
            });
        }
        else{
            Toast.makeText(getActivity(), getResources().getString(R.string.no_network_connection), Toast.LENGTH_SHORT).show();
        }

        /** Setting up values to insert into sqlite */
        SearchEntryDB dbHelper = new SearchEntryDB(getActivity());
        dbHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SearchEntryDB.KEY_NAME, mSearchText);
        String searchType = "google";
        contentValues.put(SearchEntryDB.KEY_TYPE, searchType);
        // invoke content provider insert operation
        getActivity().getContentResolver().insert(SearchEntryProvider.CONTENT_URI, contentValues);
    }

    /**
     * onClick - If search field is not empty then execute searchGooglePlus method
     *
     * @param v
     */
    @Override
    public void onClick(View v) {
        // NOTE: need to encode multiple search keywords
        try {
            mSearchText = URLEncoder.encode(mSearchField.getText().toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (mSearchText.matches("")) {
            Toast.makeText(getActivity(), "You did not enter search terms!", Toast.LENGTH_SHORT).show();
        }
        else{
            searchGooglePlus();
        }

    }
}