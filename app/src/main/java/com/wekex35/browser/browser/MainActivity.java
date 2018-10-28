package com.wekex35.browser.browser;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.inputmethod.EditorInfo;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    static ArrayList<String> tabResult;
    private String searchText;
    TabLayout tabLayout;
    private static Pattern patternDomainName;
    private Matcher matcher;
    private static final String DOMAIN_NAME_PATTERN
            = "([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,6}";
    static {
        patternDomainName = Pattern.compile(DOMAIN_NAME_PATTERN);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        final SearchView sv = findViewById(R.id.search);
        sv.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String TAG="hhh";
                Log.d(TAG, "keyCode: " + "hdfc");
            }
        });
        sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String TAG="hhh";
                Log.d(TAG, "query: " + "query");
                search(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });
        // Set up the ViewPager with the sections adapter.


        mViewPager = (ViewPager) findViewById(R.id.container);
        tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        search("bye");

    }

    public void initFragment(){
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mSectionsPagerAdapter.notifyDataSetChanged();
        mViewPager.setAdapter(mSectionsPagerAdapter);
    }
    private void search(String query) {
        if (tabLayout.getTabCount()>0){
            tabLayout.removeAllTabs();
        }

        String request = "https://www.google.com/search?q="+query+"&num=20";
        tabResult = new ArrayList<>();
        tabResult.add(request);
        tabLayout.addTab(tabLayout.newTab().setText("Search Results"));

        Log.d("Sizes", "hello "+String.valueOf(tabResult.size()));
        Toast.makeText(this,  String.valueOf(tabResult.size()), Toast.LENGTH_SHORT).show();

        RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, request,

                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        scrap(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
// Add the request to the RequestQueue.
        queue.add(stringRequest);



    }

    public void scrap(String reponse){

              // need http protocol, set this as a Google bot agent :)
        Document doc = Jsoup.parse(reponse);

        // Log.d("jsoupS","hello "+doc.body().html());
        // get all links
        Elements divs = doc.select("div.srg");
        for (Element div : divs) {
            Elements links = div.select("a[href]");
            for (Element link : links) {
               // Log.d("hello",link.toString());
                String temp = link.attr("href");
                String title = link.text();
                if (temp.startsWith("/")||title.equals(""))
                    continue;
                tabResult.add(temp);
                Log.d("hellotemp", temp + title);
                if (temp.startsWith("https:") || temp.startsWith("http:")) {

                    //use regex to get domain name
                    // result.add(getDomainName(temp));
                    // Log.d("hello",getDomainName(temp));
                    tabLayout.addTab(tabLayout.newTab().setText(getDomainName(temp)));

                }

            }

            }
        initFragment();

    }
    public String getDomainName(String url){

        String domainName = "";
        matcher = patternDomainName.matcher(url);
        if (matcher.find()) {
            domainName = matcher.group(0).toLowerCase().trim();
        }
        return domainName;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }
        private WebView webView;
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            String url =  tabResult.get(getArguments().getInt(ARG_SECTION_NUMBER));
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
             webView = rootView.findViewById(R.id.webview);
            webView.loadUrl(url);
            webView.setWebViewClient(new myWebCLient());
            try {
                WebSettings settings = webView.getSettings();
                webView.setFocusable(true);
                webView.setFocusableInTouchMode(true);
                settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
                webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
                settings.setJavaScriptCanOpenWindowsAutomatically(true);
                settings.setSupportMultipleWindows(true);
                settings.setBuiltInZoomControls(true);
                settings.setDisplayZoomControls(false);
                settings.setJavaScriptEnabled(true);
                settings.setAppCacheEnabled(true);
                settings.setAppCacheMaxSize(10 * 1024 * 1024);
                settings.setAppCachePath("");
                settings.setDatabaseEnabled(true);
                settings.setDomStorageEnabled(true);
                settings.setGeolocationEnabled(true);
                settings.setSaveFormData(false);
                settings.setSavePassword(false);
                settings.setLoadWithOverviewMode(true);
                settings.setUseWideViewPort(true);
                settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
                settings.setPluginState(WebSettings.PluginState.ON);
                settings.setGeolocationEnabled(true);
                settings.setGeolocationDatabasePath("/data/data/selendroid");
            } catch (Exception e) {
                Log.e("web view", e.toString());
            }
            textView.setText(url);
            return rootView;
        }
        private class myWebCLient extends WebViewClient {

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                return handleUri(url);
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

                return handleUri(String.valueOf(request.getUrl()));
            }

            private boolean handleUri(final String uri) {
                Log.i("webviews", "Uri =" + uri);
                webView.loadUrl(uri);
                return false;
            }
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.

             return tabResult.size();
        }
    }
}
