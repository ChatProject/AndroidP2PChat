package com.jakecrane.p2pchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

        chatTextView = (TextView)findViewById(R.id.chatTextView);
        if (message != null) {
            chatTextView.append(message);
        }
        final EditText inputEditText = (EditText)findViewById(R.id.editText2);
        final Button button = (Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                button.setEnabled(false);
                Thread t = new Thread() {
                    @Override
                    public void run() {
                        try {
                            boolean success = ChatActivity.sendMessage(username, inputEditText.getText().toString(), friend);
                            if (success) {
                                ChatActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        chatTextView.append(MY_FORMAT.format(new Date()) + "sent " + inputEditText.getText() + "\n");
                                        inputEditText.setText("");
                                    }
                                });
                            } else {
                                ChatActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getBaseContext(), "Error sending Message", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                };
                t.start();
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                button.setEnabled(true);
            }
        });

        File file = new File(getStorageDir(), friend.getUsername());
        Log.d("", "reading from " + file);
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
        Server.currentChatActivity = this;
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
        File file = new File(getStorageDir(), friend.getUsername());
        Log.d("", "writing chat to " + file);
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

    public static boolean sendMessage(String username, String message, Friend recipient) throws UnknownHostException, IOException {
        try (Socket socket = new Socket(recipient.getIpv4_address(), recipient.getListeningPort())) {
            try (ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream())) {
                final Data d = new Data(message, username);

                toServer.writeObject(d);

                try (InputStreamReader fromServer = new InputStreamReader(socket.getInputStream())) {
                    int response = fromServer.read();
                    if (response == 200) {
                        Log.d("", "Message sent successfully");
                        return true;
                    }
                }

            } catch (IOException e2) {
                e2.printStackTrace();
                Log.e("", "", e2);
            }
        } catch (UnknownHostException e3) {
            e3.printStackTrace();
            Log.e("", "", e3);
        } catch (IOException e3) {
            Log.e("", "", e3);
        }
        return false;
    }

    public File getStorageDir() {
        File f = new File(getFilesDir().getPath() + "/" + username);
        if (!f.exists()) {
            if (!f.mkdir()) {
                Log.e("", "unable to create storage dir");
                return null;
            }
        }
        return f;
    }

}
