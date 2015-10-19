package org.timreynolds.myapplication.adapter;

import com.squareup.picasso.Callback;

import org.timreynolds.myapplication.R;
import org.timreynolds.myapplication.model.Items;
import org.timreynolds.myapplication.utility.Logger;
import org.timreynolds.myapplication.utility.PicassoCache;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by sonic-ssd on 10/17/15.
 */
public class SearchAdapter extends ArrayAdapter<Items> {

    private static String TAG = SearchAdapter.class.getSimpleName();

    private Context context;
    private List<Items> mResponseData;

    public SearchAdapter(Context context, int resource, List<Items> objects) {
        super(context, resource, objects);
        this.context = context;
        this.mResponseData = objects;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.search_list_item, parent, false);
        //ResponseData responseData = mResponseData.get(position);
        TextView tv = (TextView) view.findViewById(R.id.text1);
        String titleText = mResponseData.get(position).getTitle();

        tv.setText(titleText);
        final String thumbPicURL = mResponseData.get(position).getActor().getImage().getUrl();

        ImageView img = (ImageView) view.findViewById(R.id.thumbnail);
        //Picasso.with(getContext()).load(thumbPicURL).resize(0, 75).into(img);
        // NOTE: error exception handled in PicassoCache class, basic error implemented here
        PicassoCache.getPicassoInstance(context).load(thumbPicURL)
                .resize(0, 125).placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder).into(img, new Callback() {
            @Override
            public void onSuccess() {


            }

            @Override
            public void onError() {
                // remove list item if image does not load
                mResponseData.remove(position);
                notifyDataSetChanged();
                Logger.i(TAG, "picasso error : imageURL -> " + thumbPicURL);
            }
        });

        if( titleText == "" || titleText.isEmpty() ){
            mResponseData.remove(position);
            notifyDataSetChanged();
            Logger.i(TAG, "title text empty remove row");
        }

        return view;
    }
}
