package com.jakecrane.p2pchat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import android.support.v7.app.ActionBarActivity;
import android.util.Log;

public class Server {

    public static ChatActivity currentChatActivity = null;

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

            if (Server.currentChatActivity != null && d.getSenderUsername().equals(Server.currentChatActivity.getFriend().getUsername())) {
                Log.d("", "got message placing it in textview");
                Server.currentChatActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Server.currentChatActivity.getChatTextView().append(ChatActivity.MY_FORMAT.format(new Date(d.getReceivedTime())) + "received " + d.getMessage() + "\n");
                    }
                });
                return;
            } else {
                File storageDir = new File("/data/data/com.jakecrane.p2pchat/files/" + username + "/"); //TODO make path dynamic
                if (!storageDir.exists()) {
                    if (!storageDir.mkdir()) {
                        Log.e("", "unable to create storage dir");
                    }
                }
                File outputFile = new File(storageDir, d.getSenderUsername());
                Log.d("", "server writing files to " + outputFile);
                try (FileWriter writer = new FileWriter(outputFile, true)) {
                    writer.write(ChatActivity.MY_FORMAT.format(new Date(d.getReceivedTime())) + "received " + d.getMessage() + "\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
