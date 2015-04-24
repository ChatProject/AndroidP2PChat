package com.jakecrane.p2pchat;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;

import java.io.DataOutputStream;
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

    private static String myDisplayName;
    private Friend friend;
    private TextView chatTextView;

    public TextView getChatTextView() {
        return chatTextView;
    }

    public String getMyDisplayName() {
        return myDisplayName;
    }

    public Friend getFriend() {
        return friend;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        myDisplayName = getIntent().getStringExtra("myDisplayName");
        friend = (Friend)getIntent().getSerializableExtra("friend");
        Log.d("", "chatting with " + friend.getDisplayName());

        TextView displayNameTextView = (TextView)findViewById(R.id.displayNameTextView);
        displayNameTextView.setText("Chatting with " + friend.getDisplayName() + "@"
                + friend.getIpv4_address() + ":"
                + friend.getListeningPort());

        final Intent intent = new Intent(this, FriendsActivity.class);

        chatTextView = (TextView)findViewById(R.id.chatTextView);
        final EditText inputEditText = (EditText)findViewById(R.id.editText2);
        final Button button = (Button)findViewById(R.id.button);
        final Button friendsButton = (Button)findViewById(R.id.button2);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            ChatActivity.sendMessage(myDisplayName, inputEditText.getText().toString(), friend);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }.start();
                chatTextView.append(MY_FORMAT.format(new Date()) + "sent " + inputEditText.getText() + "\n");
                inputEditText.setText("");
            }
        });

        friendsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChatActivity.this.startActivity(intent);
            }
        });
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

    public static void sendMessage(String myDisplayName, String message, Friend recipient) throws UnknownHostException, IOException {
        try (Socket socket = new Socket(recipient.getIpv4_address(), recipient.getListeningPort())) {
            try (ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream())) {
                final Data d = new Data(message, myDisplayName);

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
