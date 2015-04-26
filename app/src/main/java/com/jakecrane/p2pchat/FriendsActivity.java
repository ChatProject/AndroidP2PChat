package com.jakecrane.p2pchat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
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

    public static FriendsActivity friendsActivity = null;

    private ListView listView = null;

    public ListView getListView() {
        return listView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        final String serverAddress = getIntent().getStringExtra("serverAddress");
        final String username = getIntent().getStringExtra("username");
        final String password = getIntent().getStringExtra("password");

        Toast.makeText(getBaseContext(), "Updating Friends", Toast.LENGTH_SHORT).show();
        new Thread() {
            @Override
            public void run() {
                listView = (ListView)findViewById(R.id.listView);

                final boolean success = updateFriends(serverAddress, username, password);
                FriendsActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            Toast.makeText(getBaseContext(), "Friends Updated", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(getBaseContext(), "Unable to update Friends", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        listView.getChildAt(position).setBackgroundColor(0); //set transparent to remove possible notification
                        Friend friend = (Friend)parent.getItemAtPosition(position);

                        final Intent intent1 = new Intent(FriendsActivity.this, ChatActivity.class);
                        intent1.putExtra("username", username);
                        intent1.putExtra("friend", friend);
                        startActivity(intent1);
                    }
                });
                listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                        final Friend friend = (Friend)parent.getItemAtPosition(position);
                        AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                        builder.setTitle("Delete Confirmation");
                        builder.setMessage("Are you sure you want to delete " + friend.getUsername() + "?");

                        builder.setPositiveButton("Confirm Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread() {
                                    @Override
                                    public void run() {
                                        FriendsActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Log.d("", "Attempting to delete " + friend.getUsername());
                                                Toast.makeText(getBaseContext(), "Attempting to delete " + friend.getUsername(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        final boolean success = deleteFriend(serverAddress, username, password, friend.getUsername());
                                        FriendsActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (success) {
                                                    Toast.makeText(getBaseContext(), friend.getUsername() + " Deleted", Toast.LENGTH_LONG).show();
                                                } else {
                                                    Toast.makeText(getBaseContext(), "Unable to Delete " + friend.getUsername(), Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                        if (success) {
                                            updateFriends(serverAddress, username, password);
                                        }
                                    }
                                }.start();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                        return true;
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
                        final boolean success = updateFriends(serverAddress, username, password);
                        FriendsActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (success) {
                                    Toast.makeText(getBaseContext(), "Friends Updated", Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getBaseContext(), "Unable to Update Friends", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }.start();
            }
        });
        Button addFriendButton = (Button)findViewById(R.id.addFriendButton);
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                builder.setTitle("Add Friend");
                builder.setMessage("Enter the Username of the friend you would like to add.");

                final EditText input = new EditText(FriendsActivity.this);
                builder.setView(input);

                builder.setPositiveButton("Add Friend", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        new Thread() {
                            @Override
                            public void run() {
                                FriendsActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.d("", "Attempting to add " + input.getText().toString());
                                        Toast.makeText(getBaseContext(), "Attempting to add " + input.getText().toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                                final boolean success = addFriend(serverAddress, username, password, input.getText().toString());
                                FriendsActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (success) {
                                            Toast.makeText(getBaseContext(), input.getText().toString() + " Added", Toast.LENGTH_LONG).show();
                                        } else {
                                            Toast.makeText(getBaseContext(), "Unable to Add Friend", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                                if (success) {
                                    updateFriends(serverAddress, username, password);
                                }
                            }
                        }.start();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
        friendsActivity = this;
    }

    public static boolean addFriend(String serverAddress, String username, String password, String friendUsername) {

        try {
            URL obj = new URL("http://" + serverAddress + "/P2PChat/AddFriend");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add reuqest header

            con.setRequestMethod("POST");

            con.setRequestProperty("User-Agent", "AndroidApp");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            String urlParameters = "username=" + username + "&password=" + password + "&friend_username=" + friendUsername; //FIXME not secure

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

    public static boolean deleteFriend(String serverAddress, String username, String password, String friendUsername) {

        try {
            URL obj = new URL("http://" + serverAddress + "/P2PChat/DeleteFriend");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            //add reuqest header

            con.setRequestMethod("POST");

            con.setRequestProperty("User-Agent", "AndroidApp");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            String urlParameters = "username=" + username + "&password=" + password + "&friend_username=" + friendUsername; //FIXME not secure

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

    public boolean updateFriends(String serverAddress, String username, String password) {
        final ArrayList<Friend> friends = getFriends(serverAddress, username, password);
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
            return true;
        }
        return false;
    }

    public static ArrayList<Friend> getFriends(String serverAddress, String username, String password) {

        try {
            URL obj = new URL("http://" + serverAddress + "/P2PChat/GetFriends");
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();

            con.setRequestMethod("POST");
            con.setRequestProperty("User-Agent", "AndroidApp");
            con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

            String urlParameters = "username=" + username + "&password=" + password; //FIXME not secure

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
                Log.d("", "Friends Retrieved");
            } else {
                Log.d("", "Unable to retrieve friends");
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
