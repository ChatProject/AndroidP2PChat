package com.jakecrane.p2pchat;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;


public class FriendsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        final ListView listView = (ListView)findViewById(R.id.listView);
            new Thread() {
                @Override
                public void run() {
                    final ArrayList<Friend> friends = getFriends();
                    if (friends != null) {
                        final ArrayAdapter<Friend> arrayAdapter = new ArrayAdapter<Friend>(
                                FriendsActivity.this,
                                android.R.layout.simple_list_item_1,
                                friends);
                        FriendsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listView.setAdapter(arrayAdapter);
                            }
                        });
                    }
                }
            }.start();

    }

    public static ArrayList<Friend> getFriends() {

        try {
            URL obj = new URL("http://" + MainActivity.SERVER_ADDRESS + "/P2PChat/GetFriends");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "AndroidApp");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            String urlParameters = "display_name=" + MainActivity.DISPLAY_NAME;

            // Send post request
            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.writeBytes(urlParameters);
                wr.flush();
            }

            ArrayList<Friend> friends = new ArrayList<Friend>();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()))) {
                String inputLine;

                while ((inputLine = in.readLine()) != null) {
                    System.out.println(inputLine);
                    Friend f = new ObjectMapper().readValue(inputLine, Friend.class);
                    friends.add(f);
                }
            }

            final int responseCode = con.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println("Friends Retrieved");
            } else {
                System.out.println("Unable to retrieve friends");
            }

            return friends;

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_friends, menu);
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
}
