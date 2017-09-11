package com.cmnd97.booklistingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Boolean.getBoolean;

public class MainActivity extends AppCompatActivity {

    static String FETCH_LINK_BASE = "https://www.googleapis.com/books/v1/volumes?q="; //not referenced to string resource because it needs to be static
    String userQuery = "";
    ArrayList<Entry> entries = new ArrayList<>();
    EntryAdapter adapter;
    ProgressBar progressBar;
    int maxResults;
    boolean includeNotForSale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchPrefs();
                inflateResults();
            }
        });

        if (savedInstanceState != null && !EntryContainer.getInstance().rotatedWithEmptyAdapter) {
            adapter = new EntryAdapter(this, EntryContainer.getInstance().entries);
            inflateResults();
        }

    }


    void inflateResults() {
        String query = ((EditText) findViewById(R.id.text_search)).getText().toString();
        if (!query.equals("")) {
            progressBar.setVisibility(View.VISIBLE);
            entries.clear();
            userQuery = query.replace(" ", "+");
            progressBar.setProgress(0);
            new JsonFetchTask().execute(query);
            adapter = new EntryAdapter(this, entries);
            checkIfResults();
        }
        ListView list = (ListView) findViewById(R.id.list);
        findViewById(R.id.hint).setVisibility(View.GONE);
        ((ListView) list.findViewById(R.id.list)).setAdapter(adapter);

    }

    void checkIfResults() {  //setEmptyView is buggy
        if (entries.size() == 0) findViewById(R.id.no_results).setVisibility(View.VISIBLE);
        else findViewById(R.id.no_results).setVisibility(View.GONE);
    }

    void clickOnViewItem(String url) {

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (adapter != null) {
            EntryContainer.getInstance().entries = entries;
            EntryContainer.getInstance().rotatedWithEmptyAdapter = false;
        } else
            EntryContainer.getInstance().rotatedWithEmptyAdapter = true;

    }

    void fetchPrefs() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        maxResults = Integer.parseInt(sharedPrefs.getString(getString(R.string.settings_max_results_key), getString(R.string.settings_max_results_default)));
        includeNotForSale = sharedPrefs.getBoolean(getString(R.string.settings_not_for_sale_key), getBoolean(getString(R.string.settings_not_for_sale_default)));
    }

    private class JsonFetchTask extends AsyncTask<String, Integer, List<Entry>> {

        protected List<Entry> doInBackground(String... urls) {

            try {
                JSONObject root = readJsonFromUrl(FETCH_LINK_BASE + userQuery);
                JSONArray booksArray = root.getJSONArray("items");
                int length = (booksArray.length() <= maxResults) ? booksArray.length() : ((maxResults == 0) ? booksArray.length() : maxResults);
                // ignore  maxResults if booksArray.length() is smaller than maxResults or if maxResults is 0
                for (int i = 0; i < length; i++) {

                    JSONObject currentBookInfo = booksArray.getJSONObject(i).getJSONObject("volumeInfo");
                    String bookAuthors = getString(R.string.by);
                    for (int j = 0; j < currentBookInfo.getJSONArray("authors").length(); j++) {
                        bookAuthors += currentBookInfo.getJSONArray("authors").get(j);

                        if (j != currentBookInfo.getJSONArray("authors").length() - 1)
                            bookAuthors += "\n ";
                    }
                    String thumbLink = currentBookInfo.getJSONObject("imageLinks").getString("thumbnail");
                    Boolean isForSale = booksArray.getJSONObject(i).getJSONObject("saleInfo").getString("saleability").equals("FOR_SALE");
                    String price;
                    if (isForSale)
                        price = booksArray.getJSONObject(i).getJSONObject("saleInfo").getJSONObject("retailPrice").getString("amount") + " " + booksArray.getJSONObject(i).getJSONObject("saleInfo").getJSONObject("retailPrice").getString("currencyCode");
                    else price = getString(R.string.not_for_sale);

                    if (isForSale || includeNotForSale)
                        entries.add(new Entry(currentBookInfo.getString("title"), bookAuthors, currentBookInfo.getString("infoLink"), fetchThumbnail(thumbLink), price, i + 1));

                    /*
                         simplified statement from boolean truth table
                            add | isForSale | includeNotForSale
                              0         0           0
                              1         0           1
                              1         1           0
                              1         1           1
                            _____________________________
                            therefore, add = isForSale + includeNotForSale, or add= ( isForSale || includeNotForSale)

                     */

                    publishProgress((int) (((i + 1) / (float) length) * 100));
                }
            } catch (Exception e) {
                Log.e(getString(R.string.app_name), e.toString());
            }
            return entries;
        }

        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if (progressBar != null)
                progressBar.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(List<Entry> entries) {
            findViewById(R.id.progress).setVisibility(View.GONE);
            if (entries == null) return;
            checkIfResults();
        }

        Bitmap fetchThumbnail(String url) {
            try {
                InputStream is = (InputStream) new URL(url).getContent();
                return BitmapFactory.decodeStream(is);
            } catch (Exception e) {
                Log.e(getString(R.string.app_name), e.toString());
                return null;
            }
        }

        JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
            InputStream is = new URL(url).openStream();
            try {
                BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
                String jsonText = readAll(rd);
                JSONObject json = new JSONObject(jsonText);
                return json;
            } finally {
                is.close();
            }
        }

        String readAll(Reader rd) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);


    }

}
