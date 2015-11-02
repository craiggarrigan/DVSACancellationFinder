package uk.co.craiggarrigan.dvsacancellationfinder;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CancellationService {

    private final OkHttpClient client = new OkHttpClient();

    private static final String SVC = "CnclSvc";

    public CancellationService() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        client.setCookieHandler(cookieManager);
    }

    public String[] getCancellations(final String licenseNumber, final String applicationRefNumber) {

        Log.d(SVC, "Getting cancellations");

        AsyncTask<Void, Void, List<String>> task = new AsyncTask<Void, Void, List<String>>() {
            @Override
            protected List<String> doInBackground(Void... params) {

                List<String> result = new ArrayList<>();

                try {

                    RequestBody formBody = new FormEncodingBuilder()
                            .add("javascriptEnabled", "true")
                            .add("passwordType", "NORMAL")
                            .add("username", licenseNumber)
                            .add("password", applicationRefNumber)
                            .add("alternativePassword", "")
                            .add("booking-login", "Continue")
                            .build();
                    Request request = new Request.Builder()
                            .url("https://driverpracticaltest.direct.gov.uk/login")
                            .post(formBody)
                            .build();

                    Log.d(SVC, "Sending HTTP request");

                    Response response = client.newCall(request).execute();

                    Log.d(SVC, "Extracting CSRF token");

                    BufferedReader r = new BufferedReader(new InputStreamReader(response.body().byteStream()));
                    String s;
                    String csrf = null;
                    while ((s = r.readLine()) != null) {
                        Log.d(SVC, s);
                        Matcher m = Pattern.compile("csrftoken=([a-zA-Z0-9]{32})").matcher(s);
                        if(m.find()){
                            csrf = m.group(1);
                            //break;
                        }
                    }

                    Log.d(SVC, "CSRF token = " + csrf);

                    request = new Request.Builder()
                            .url("https://driverpracticaltest.direct.gov.uk/manage?execution=e1s1&_eventId=editTestDateTime&csrftoken=" + csrf)
                            .build();

                    response = client.newCall(request).execute();

                    printResponse(response);

                    formBody = new FormEncodingBuilder()
                            .add("testChoice", "ASAP")
                            .add("preferredTestDate", "")
                            .addEncoded("drivingLicenceSubmit", "Find+available+dates")
                            .build();

                    request = new Request.Builder()
                            .url("https://driverpracticaltest.direct.gov.uk/manage?execution=e1s2&_eventId=proceed&csrftoken=" + csrf)
                            .post(formBody)
                            .build();

                    response = client.newCall(request).execute();

                    printResponse(response);

                    request = new Request.Builder()
                            .url("https://driverpracticaltest.direct.gov.uk/manage?execution=e1s3")
                            .build();

                    response = client.newCall(request).execute();

                    r = new BufferedReader(new InputStreamReader(response.body().byteStream()));
                    List<String> slots = new ArrayList<>();
                    while ((s = r.readLine()) != null) {
                        Log.d(SVC, s);
                        Matcher m = Pattern.compile("chosenSlot=([0-9]{13})").matcher(s);
                        if (m.find()) {
                            String strTimestamp = m.group(1);
                            Date d = new Date(Long.parseLong(strTimestamp));
                            slots.add(DateFormat.getDateTimeInstance().format(d));
                        }
                    }

                    result = slots;

                } catch (IOException e) {
                    Log.i(SVC, "Failed HTTP", e);
                }

                return result;
            }
        };


        List<String> result = new ArrayList<>();
        result.add("Failed");
        try {
            Log.d(SVC, "Starting task");
            result = task.execute().get();
        } catch (InterruptedException | ExecutionException e) {
            Log.i(SVC, "Failed task", e);
        }

        return result.toArray(new String[result.size()]);
    }

    private void printResponse(Response response) throws IOException {

        Log.d(SVC, "Response for " + response.request().urlString());

        BufferedReader r;
        String s;
        r = new BufferedReader(new InputStreamReader(response.body().byteStream()));
        while ((s = r.readLine()) != null) {
            Log.d(SVC, s);
        }
    }
}
