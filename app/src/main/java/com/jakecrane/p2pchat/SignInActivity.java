package com.jakecrane.p2pchat;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class SignInActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        Button signInButton = (Button)findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String serverAddress = ((EditText)findViewById(R.id.serverEditText)).getText().toString();
                final String username = ((EditText)findViewById(R.id.usernameEditText)).getText().toString();
                final String password = ((EditText)findViewById(R.id.passwordEditText)).getText().toString();
                final int myOpenPort = Integer.parseInt(((EditText)findViewById(R.id.myOpenPortEditText)).getText().toString());
                new Server(myOpenPort, serverAddress, username, password, SignInActivity.this);
                new Thread() {
                    @Override
                    public void run() {
                        updateStatus(serverAddress, username, password, myOpenPort);
                    }
                }.start();

                final Intent intent1 = new Intent(SignInActivity.this, FriendsActivity.class);
                intent1.putExtra("serverAddress", serverAddress );
                intent1.putExtra("username", username );
                intent1.putExtra("password", password );
                startActivity(intent1);
                finish();
            }
        });
        Button createAccountButton = (Button)findViewById(R.id.createAccountButton);
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String serverAddress = ((EditText)findViewById(R.id.serverEditText)).getText().toString();
                final String username = ((EditText)findViewById(R.id.usernameEditText)).getText().toString();
                final String password = ((EditText)findViewById(R.id.passwordEditText)).getText().toString();
                final int myOpenPort = Integer.parseInt(((EditText)findViewById(R.id.myOpenPortEditText)).getText().toString());
                new Thread() {
                    @Override
                    public void run() {
                        createAccount(serverAddress, username, password, myOpenPort);
                    }
                }.start();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public static boolean createAccount(String serverAddress, String username, String password, int myOpenPort) {

        try {
            URL obj = new URL("http://" + serverAddress + "/P2PChat/CreateAccount");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add reuqest header

            con.setRequestMethod("POST");

            con.setRequestProperty("User-Agent", "AndroidApp");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            String urlParameters = "username=" + username + "&password=" + password + "&listening_port=" + myOpenPort; //FIXME not secure

            // Send post request
            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.writeBytes(urlParameters);
                wr.flush();
            }

            final int responseCode = con.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                return true;
            }

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean updateStatus(String serverAddress, String username, String password, int myOpenPort) {

        try {
            URL obj = new URL("http://" + serverAddress + "/P2PChat/UpdateUser");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add reuqest header

            con.setRequestMethod("POST");

            con.setRequestProperty("User-Agent", "AndroidApp");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            String urlParameters = "username=" + username + "&password=" + password + "&listening_port=" + myOpenPort; //FIXME not secure

            // Send post request
            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.writeBytes(urlParameters);
                wr.flush();
            }

            final int responseCode = con.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                return true;
            }

        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
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
