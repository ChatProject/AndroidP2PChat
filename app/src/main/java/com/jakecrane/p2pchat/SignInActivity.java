package com.jakecrane.p2pchat;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
                MainActivity.serverAddress = ((EditText)findViewById(R.id.serverEditText)).getText().toString();
                MainActivity.displayName = ((EditText)findViewById(R.id.displayNameEditText)).getText().toString();
                MainActivity.myOpenPort = Integer.parseInt(((EditText)findViewById(R.id.myOpenPortEditText)).getText().toString());
                MainActivity.peerIpAddress = ((EditText)findViewById(R.id.peerIpAddressEditText)).getText().toString();
                MainActivity.peerOpenPort = Integer.parseInt(((EditText)findViewById(R.id.peerPortEditText)).getText().toString());
                //finish();
                final Intent intent1 = new Intent(SignInActivity.this, MainActivity.class);
                startActivity(intent1);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void updateStatus() {

        try {
            URL obj = new URL("http://" + MainActivity.serverAddress + "/P2PChat/UpdateUser");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add reuqest header

            con.setRequestMethod("POST");

            con.setRequestProperty("User-Agent", "AndroidApp");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            String urlParameters = "display_name=" + MainActivity.displayName + "&listening_port=" + MainActivity.myOpenPort; //FIXME not secure

            // Send post request
            con.setDoOutput(true);
            try (DataOutputStream wr = new DataOutputStream(con.getOutputStream())) {
                wr.writeBytes(urlParameters);
                wr.flush();
            }

            final int responseCode = con.getResponseCode();
            SignInActivity.this.runOnUiThread(new Runnable() {
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
