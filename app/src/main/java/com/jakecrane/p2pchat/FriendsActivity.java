package com.jakecrane.p2pchat;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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

    private ListView listView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
            new Thread() {
                @Override
                public void run() {

                    listView = (ListView)findViewById(R.id.listView);

                    updateFriends();

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                            Friend f = (Friend)parent.getItemAtPosition(position);

                            ChatActivity.peerDisplayName = f.getDisplayName();
                            ChatActivity.peerIpAddress = f.getIpv4_address();
                            ChatActivity.peerOpenPort = f.getListeningPort();

                            Toast.makeText(getBaseContext(), f.getDisplayName() + "@"
                                    + f.getIpv4_address() + ":" + f.getListeningPort(),
                                    Toast.LENGTH_LONG).show();
                            final Intent intent1 = new Intent(FriendsActivity.this, ChatActivity.class);
                            startActivity(intent1);
                            finish();

                        }
                    });
                }
            }.start();

        Button refreshButton = (Button)findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        updateFriends();
                    }
                }.start();
            }
        });
    }

    public void updateFriends() {
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

    public static ArrayList<Friend> getFriends() {

        try {
            URL obj = new URL("http://" + ChatActivity.serverAddress + "/P2PChat/GetFriends");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "AndroidApp");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            String urlParameters = "display_name=" + ChatActivity.displayName; //FIXME not secure

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
