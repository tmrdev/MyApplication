package org.timreynolds.myapplication;

/**
 * Created Tim Reynolds
 *
 */
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import org.timreynolds.myapplication.adapter.NavigationDrawerAdapter;
import org.timreynolds.myapplication.model.NavDrawerItem;
import org.timreynolds.myapplication.utility.Logger;
import org.timreynolds.myapplication.utility.PicassoCache;

/**
 * FragmentDrawer - handles functionality for left hand drawer
 */
public class  FragmentDrawer extends Fragment {

    private static String TAG = FragmentDrawer.class.getSimpleName();

    private RecyclerView recyclerView;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private NavigationDrawerAdapter adapter;
    private View containerView;
    private static String[] titles = null;
    private FragmentDrawerListener drawerListener;
    String myProfileURL;

    /**
     * FragmentDrawer
     */
    public FragmentDrawer() {

    }

    /**
     * setDrawerListener
     *
     * @param listener
     */
    public void setDrawerListener(FragmentDrawerListener listener) {
        this.drawerListener = listener;
    }

    /**
     * List<NavDrawerItem> getData
     *
     * @return
     */
    public static List<NavDrawerItem> getData() {
        List<NavDrawerItem> data = new ArrayList<>();


        // preparing navigation drawer items
        for (int i = 0; i < titles.length; i++) {
            NavDrawerItem navItem = new NavDrawerItem();
            navItem.setTitle(titles[i]);
            data.add(navItem);
        }
        return data;
    }

    /**
     * onCreate
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // drawer labels
        titles = getActivity().getResources().getStringArray(R.array.nav_drawer_labels);
    }

    /**
     * onCreateView
     *
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflating view layout
        View layout = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        recyclerView = (RecyclerView) layout.findViewById(R.id.drawerList);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        myProfileURL = settings.getString("my_profile_image_url", "null");

        ImageView myNavProfileImages = (ImageView) layout.findViewById(R.id.circleView);
        //Picasso.with(getContext()).load(testImage).resize(0, 75).into(myNavProfileImages);
        // NOTE: error exception handled in PicassoCache class, basic error implemented here
        PicassoCache.getPicassoInstance(getActivity()).load(myProfileURL)
                .resize(0, 75).placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile).into(myNavProfileImages, new Callback() {
            @Override
            public void onSuccess() {


            }

            @Override
            public void onError() {
                Logger.i("picassoError", "picasso error : imageURL -> " + myProfileURL);
            }
        });

        adapter = new NavigationDrawerAdapter(getActivity(), getData());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                drawerListener.onDrawerItemSelected(view, position);
                mDrawerLayout.closeDrawer(containerView);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        return layout;
    }

    /**
     * setUp
     *
     * @param fragmentId
     * @param drawerLayout
     * @param toolbar
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout, final Toolbar toolbar) {
        containerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                toolbar.setAlpha(1 - slideOffset / 2);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

    }

    public static interface ClickListener {
        public void onClick(View view, int position);

        public void onLongClick(View view, int position);
    }

    static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                clickListener.onClick(child, rv.getChildPosition(child));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }


    }

    /**
     * onResume
     */
    public void onResume(){
        super.onResume();

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        myProfileURL = settings.getString("my_profile_image_url", "null");

        // if myProfileURL is invalid error placeholder will be used
        ImageView myNavProfileImages = (ImageView) getView().findViewById(R.id.circleView);
        //Picasso.with(getContext()).load(testImage).resize(0, 75).into(myNavProfileImages);
        // NOTE: error exception handled in PicassoCache class, basic error implemented here
        PicassoCache.getPicassoInstance(getActivity()).load(myProfileURL)
                .resize(0, 75).placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile).into(myNavProfileImages, new Callback() {
            @Override
            public void onSuccess() {


            }

            @Override
            public void onError() {
                Logger.i("picassoError", "picasso error : imageURL -> " + myProfileURL);
            }
        });
    }

    public interface FragmentDrawerListener {
        public void onDrawerItemSelected(View view, int position);
    }
}