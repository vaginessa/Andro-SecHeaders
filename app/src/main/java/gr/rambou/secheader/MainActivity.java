/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Nikos Bousios
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package gr.rambou.secheader;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import gr.rambou.secheader.adapter.TabsPagerAdapter;

public class MainActivity extends ActionBarActivity implements
        ActionBar.TabListener {

    private ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;
    private RequestQueue queue;
    //number that track requests
    private int requests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialization
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getSupportActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());

        viewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Adding Tab's Tittles
        String[] tabs = {getString(R.string.scan), getString(R.string.stats), getString(R.string.about)};
        for (String tab_name : tabs) {
            actionBar.addTab(actionBar.newTab().setText(tab_name)
                    .setTabListener(this));
        }

        // on swiping the viewpager make respective tab selected
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
                // on changing the page
                // make respected tab selected
                actionBar.setSelectedNavigationItem(position);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // on tab selected, show respected fragment view
        viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    public void Button_Clicked(View v) {
        //find EditText View
        EditText txt_website = (EditText) findViewById(R.id.txt_websites);

        //Split string with comma and pass it into a string array
        String[] websites = txt_website.getText().toString().split(",");

        // Instantiate the RequestQueue.
        queue = Volley.newRequestQueue(this);

        //Set requests number
        requests = websites.length;

        //Disable Buttons functionality
        (findViewById(R.id.button_scan)).setEnabled(false);
        (findViewById(R.id.checkbox_save)).setEnabled(false);

        //Initialize progressBar
        ((ProgressBar) findViewById(R.id.progressBar)).setMax(requests);
        ((ProgressBar) findViewById(R.id.progressBar)).setProgress(0);

        //Start getting each website headers
        for (String url : websites) {
            //fix url
            if (!url.toLowerCase().contains("http://") && !url.toLowerCase().contains("https://"))
                url = "http://" + url.trim();
            //get headers and analyze them
            getHeader(url);
        }

    }

    private void getHeader(String url) {
        //Create a Listener for our requests
        Response.Listener listener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject newjson) {
                //Check if we need to save into database
                CheckBox cb = (CheckBox) findViewById(R.id.checkbox_save);
                if (cb.isChecked()) {
                    //We open/create our sqlite database
                    DatabaseHandler mydb = new DatabaseHandler(getApplicationContext());

                    //Loop over all results
                    for (int i = 0; i < newjson.names().length(); i++) {
                        try {
                            //Check if the value is URL and don't insert it into db
                            String value = newjson.names().getString(i).toString();
                            if (!value.equals("URL"))
                                //Add result into database
                                mydb.addResult(newjson.getString("URL"), value, (newjson.get(newjson.names().getString(i)).equals("Secure")) ? 1 : 0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
                /*
                for (int i = 0; i < newjson.names().length(); i++) {
                    try {
                        Log.wtf("LOL", "key = " + newjson.names().getString(i) + " value = " + newjson.get(newjson.names().getString(i)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }*/

                //decrease requests value;
                requests--;

                //Update progressBar
                int max = ((ProgressBar) findViewById(R.id.progressBar)).getMax();
                ((ProgressBar) findViewById(R.id.progressBar)).setProgress(max - requests);

                //Check if we finished
                if (requests == 1) {
                    //Sending a toast to the user that we got all headers
                    Context context = getApplicationContext();
                    CharSequence text = "We Got all Headers!!!";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
                    toast.show();
                    //Re-Enable Buttons functionality
                    (findViewById(R.id.button_scan)).setEnabled(true);
                    (findViewById(R.id.checkbox_save)).setEnabled(true);
                }

            }
        };
        //Create an Error Listener for our requests
        Response.ErrorListener errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    Log.v("Error Response code", String.valueOf(error.networkResponse.statusCode));
                }
            }
        };

        // Request a string response from the provided URL.
        JsonObjectRequest stringRequest = new JsonObjectRequest(
                Request.Method.HEAD, //We only want the headers, although it's similar with GET.
                url, //our url
                listener, //our listener, handling our findings
                errorListener) { //Error Listener to handle errors in requests

            @Override
            protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
                try {
                    //We create a JSONObject and add the url address to it.
                    org.json.JSONObject f = new JSONObject(response.headers).put("URL", this.getUrl());

                    /*We return the headers as a JSONObject which,
                    we filter the headers and return the security ones*/
                    return Response.success(FilterHeaders(f), HttpHeaderParser.parseCacheHeaders(response));
                } catch (JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }

        };

        // Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

    private JSONObject FilterHeaders(JSONObject headers) { //headers
        JSONObject newjson = new JSONObject();
        for (int i = 0; i < headers.names().length(); i++) {
            try {
                String key = headers.names().getString(i), value = headers.get(headers.names().getString(i)).toString();

                //We filter the header and keep only the ones that has to do with security!
                switch (key) {
                    //Header about Access Control
                    case "Access-Control-Allow-Origin":
                        if (value.equals("*"))
                            newjson.put(key, "Not Secure");
                        else
                            newjson.put(key, "Secure");
                        break;
                    //All headers about CSP
                    case "Content-Security-Policy":
                        if (value.equals("*") || value.contains("-src *") || value.contains("*;"))
                            newjson.put("CSP", "Not Secure");
                        else
                            newjson.put("CSP", "Secure");
                        break;
                    case "X-Webkit-CSP":
                        if (value.equals("*") || value.contains("-src *") || value.contains("*;"))
                            newjson.put("CSP", "Not Secure");
                        else
                            newjson.put("CSP", "Secure");
                        break;
                    //Cross Domain Meta Policy
                    case "X-Permitted-Cross-Domain-Policies":
                        newjson.put(key, "Secure");
                        break;
                    //Show your server information? Srsly?
                    case "Server":
                        newjson.put(key, "Not Secure");
                        break;
                    //Not using utf-8? Then probably somehow you'll get fucked...
                    case "Content-Type":
                        if (value.toLowerCase().contains("utf-8"))
                            newjson.put(key, "Secure");
                        else
                            newjson.put(key, "Not Secure");
                        break;
                    //Frame Options, say no to clickjacking
                    case "X-Frame-Options":
                        newjson.put("Frame-Options", "Secure");
                        break;
                    case "Frame-Options":
                        newjson.put(key, "Secure");
                        break;
                    //Show your software version information? Srsly?
                    case "X-Powered-By":
                        newjson.put(key, "Not Secure");
                        break;
                    //XSS Protection, good boy!
                    case "X-XSS-Protection":
                        if (value.contains("0"))
                            newjson.put(key, "Not Secure");
                        else
                            newjson.put(key, "Secure");
                        break;
                    //say "no!" to MIME-sniffing
                    case "X-Content-Type-Options":
                        if (value.contains("nosniff"))
                            newjson.put(key, "Secure");
                        else
                            newjson.put(key, "Not Secure");
                        break;
                    //Download Options
                    case "X-Download-Options":
                        newjson.put(key, "Secure");
                        break;
                    //HTTP Strict Transport Security, Show you care... use HTTPS everywhere!
                    case "Strict-Transport-Security":
                        newjson.put(key, "Secure");
                        break;
                    //Cookies for Fuck shake!!!
                    case "Set-Cookie":
                        newjson.put(key, "Secure");
                        break;
                    //p3p Platform for Privacy Preferences
                    case "p3p":
                        newjson.put(key, "Secure");
                        break;
                    //Blogging and DDOS, watch the Pingback.
                    case "X-Pingback":
                        newjson.put(key, "Secure");
                        break;
                    case "URL":
                        newjson.put(key, value.replace("https://", "").replace("http://", ""));
                    default:
                        //We remove headers that has nothing to do with Security
                        //newjson.remove(key);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return newjson;
    }

}
