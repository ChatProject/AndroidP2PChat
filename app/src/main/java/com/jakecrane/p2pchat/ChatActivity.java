package com.jakecrane.p2pchat;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;

public class ChatActivity extends ActionBarActivity {

    public static String serverAddress = null;
    public static String displayName = null;
    public static int myOpenPort = -1;
    public static String peerDisplayName = null;
    public static String peerIpAddress = null;
    public static int peerOpenPort = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        TextView displayNameTextView = (TextView)findViewById(R.id.displayNameTextView);
        displayNameTextView.setText("Chatting with " + peerDisplayName + "@"
                + ChatActivity.peerIpAddress + ":"
                + ChatActivity.peerOpenPort);

        final Intent intent = new Intent(this, FriendsActivity.class);

        new Thread() {
            @Override
            public void run() {
                updateStatus();
            }
        }.start();

        final TextView chatTextView = (TextView)findViewById(R.id.chatTextView);
        final EditText inputEditText = (EditText)findViewById(R.id.editText2);
        final Button button = (Button)findViewById(R.id.button);
        final Button friendsButton = (Button)findViewById(R.id.button2);

        friendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatActivity.this.startActivity(intent);
            }
        });

        try {
            new Client(this, inputEditText, chatTextView, button);
            new Server(this, chatTextView);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void updateStatus() {

        try {
            URL obj = new URL("http://" + serverAddress + "/P2PChat/UpdateUser");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add reuqest header

            con.setRequestMethod("POST");

            con.setRequestProperty("User-Agent", "AndroidApp");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            String urlParameters = "display_name=" + displayName + "&listening_port=" + myOpenPort; //FIXME not secure

            // Send post request
            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.writeBytes(urlParameters);
                wr.flush();
            }

            final int responseCode = con.getResponseCode();
            ChatActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(getApplicationContext(), "Status Updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Unable to update your status", Toast.LENGTH_LONG).show();
                    }
                }
            });
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void setText(final EditText text,final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }

}
