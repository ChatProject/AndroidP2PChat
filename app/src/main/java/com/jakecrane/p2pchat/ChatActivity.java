package com.jakecrane.p2pchat;

import android.content.Intent;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class ChatActivity extends ActionBarActivity {

    public static final SimpleDateFormat MY_FORMAT = new SimpleDateFormat("[h:mm:ss] ");

    private static String username;
    private Friend friend;
    private TextView chatTextView;

    public TextView getChatTextView() {
        return chatTextView;
    }

    public String getMyUsername() {
        return username;
    }

    public Friend getFriend() {
        return friend;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        username = getIntent().getStringExtra("username");
        friend = (Friend)getIntent().getSerializableExtra("friend");
        String message = getIntent().getStringExtra("message");
        Log.d("", "chatting with " + friend.getUsername());

        TextView usernameTextView = (TextView)findViewById(R.id.usernameTextView);
        usernameTextView.setText("Chatting with " + friend.getUsername() + "@"
                + friend.getIpv4_address() + ":"
                + friend.getListeningPort());

        final Intent intent = new Intent(this, FriendsActivity.class);

        chatTextView = (TextView)findViewById(R.id.chatTextView);
        if (message != null) {
            chatTextView.append(message);
        }
        final EditText inputEditText = (EditText)findViewById(R.id.editText2);
        final Button button = (Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            ChatActivity.sendMessage(username, inputEditText.getText().toString(), friend);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                chatTextView.append(MY_FORMAT.format(new Date()) + "sent " + inputEditText.getText() + "\n");
                inputEditText.setText("");
            }
        });

        File file = new File(getFilesDir(), friend.getUsername());
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line = null;
                while ((line = reader.readLine()) != null) {
                    chatTextView.append(line + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Server.chatActivities.add(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Server.chatActivities.remove(this);
        File file = new File(getFilesDir(), friend.getUsername());
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(chatTextView.getText().toString());
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

    private void setText(final EditText text,final String value){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setText(value);
            }
        });
    }

    public static void sendMessage(String username, String message, Friend recipient) throws UnknownHostException, IOException {
        try (Socket socket = new Socket(recipient.getIpv4_address(), recipient.getListeningPort())) {
            try (ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream())) {
                final Data d = new Data(message, username);

                toServer.writeObject(d);

						/*try (ObjectInputStream fromServer = new ObjectInputStream(socket.getInputStream())) {

							Data reply = (Data)fromServer.readObject();
							textArea.append(reply.getMessage() + "\n");
						} catch (ClassNotFoundException e1) {
							e1.printStackTrace();
						}*/
            } catch (IOException e2) {
                e2.printStackTrace();
                Log.d("", "", e2);
            }
        } catch (UnknownHostException e3) {
            e3.printStackTrace();
            Log.d("", "", e3);
        } catch (IOException e3) {
            Log.d("", "", e3);
        }
    }

}
