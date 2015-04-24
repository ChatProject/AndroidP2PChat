package com.jakecrane.p2pchat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.TextView;

public class Server {

    public static final ArrayList<ChatActivity> chatActivities = new ArrayList<ChatActivity>();

	public Server(final int myOpenPort, final String serverAddress, final String username, final String password, final ActionBarActivity activity) {
        new Thread() {
            @Override
            public void run() {
                try (ServerSocket serverSocket = new ServerSocket(myOpenPort)) {
                    Log.d("Server started on " + myOpenPort, "");
                    while (true) {
                        try {
                            Socket socket = serverSocket.accept();
                            new ServerThread(socket, activity, serverAddress, username, password).start();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e("", "", e);
                        }
                    }
                } catch (IOException i) {
                    System.err.println("Unable to start. Another instance may already be running.");
                    Log.e("", "Unable to start. Another instance may already be running.");
                }
            }
        }.start();
    }
}

class ServerThread extends Thread {

	private Socket socket;
    private ActionBarActivity activity;
    private String serverAddress;//TODO remove
    private String username;//TODO remove
    private String password;//TODO remove
	//private ObjectInputStream inputFromClient;

	public ServerThread(Socket socket, ActionBarActivity activity, String serverAddress, String username, String password) {
        this.socket = socket;
        this.activity = activity;
        this.serverAddress = serverAddress;
        this.username = username;
        this.password = password;
	}

	@Override
	public void run() {
		try (ObjectInputStream inputFromClient = new ObjectInputStream(socket.getInputStream())) {

			final Data d = (Data)inputFromClient.readObject();
			d.setReceivedTime(System.currentTimeMillis());

            Log.d("", d.getSenderUsername() + " is sending a message");
            for (final ChatActivity chatActivity : Server.chatActivities) {
                Log.d("", "checking to see if " + d.getSenderUsername() + " equals " + chatActivity.getFriend().getUsername());
                if (d.getSenderUsername().equals(chatActivity.getFriend().getUsername())) {
                    Log.d("", "got message placing it in textview");
                    chatActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chatActivity.getChatTextView().append(ChatActivity.MY_FORMAT.format(new Date(d.getReceivedTime())) + "received " + d.getMessage() + "\n");
                        }
                    });
                    return;
                }
            }

            //open new ChatActivity
            final Intent intent1 = new Intent(activity, ChatActivity.class);
            intent1.putExtra("username", username);
            ArrayList<Friend> friends = FriendsActivity.getFriends(serverAddress, username, password);
            for (Friend friend : friends) {
                if (friend.getUsername().equals(d.getSenderUsername())) {
                    intent1.putExtra("friend", friend);
                }
            }
            if (intent1.getSerializableExtra("friend") != null) {
                intent1.putExtra("message", ChatActivity.MY_FORMAT.format(new Date(d.getReceivedTime())) + "received " + d.getMessage() + "\n");
                activity.startActivity(intent1);
            } else {
                Log.d("", d.getSenderUsername() + " send you a message but is not your friend.");
            }

			/*try (ObjectOutputStream outputToClient = new ObjectOutputStream(socket.getOutputStream())) {
				Data d = new Data("server says " + s.getMessage() + " back");
				outputToClient.writeObject(d);
			}*/

        } catch(ClassNotFoundException ex) {
		    ex.printStackTrace();
            Log.d("", "", ex);
		} catch(IOException ex) {
			ex.printStackTrace();
            Log.d("", "", ex);
		}
	}

}
