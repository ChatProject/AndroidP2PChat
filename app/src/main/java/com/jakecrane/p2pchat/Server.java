package com.jakecrane.p2pchat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;

import android.util.Log;
import android.widget.TextView;

public class Server {

    public static final ArrayList<ChatActivity> chatActivities = new ArrayList<ChatActivity>();

	public Server(final int myOpenPort) {
        new Thread() {
            @Override
            public void run() {
                try (ServerSocket serverSocket = new ServerSocket(myOpenPort)) {
                    Log.d("Server started on " + myOpenPort, "");
                    while (true) {
                        try {
                            Socket socket = serverSocket.accept();
                            new ServerThread(socket).start();
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
	//private ObjectInputStream inputFromClient;

	public ServerThread(Socket socket) {
		this.socket = socket;
	}

	@Override
	public void run() {
		try (ObjectInputStream inputFromClient = new ObjectInputStream(socket.getInputStream())) {

			final Data d = (Data)inputFromClient.readObject();
			d.setReceivedTime(System.currentTimeMillis());

            Log.d("", d.getSenderDisplayName() + " is sending a message");
            for (final ChatActivity chatActivity : Server.chatActivities) {
                Log.d("", "checking to see if " + d.getSenderDisplayName() + " equals " + chatActivity.getFriend().getDisplayName());
                if (d.getSenderDisplayName().equals(chatActivity.getFriend().getDisplayName())) {
                    Log.d("", "got message placing it in textview");
                    chatActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            chatActivity.getChatTextView().append(ChatActivity.MY_FORMAT.format(new Date(d.getReceivedTime())) + "received " + d.getMessage() + "\n");
                        }
                    });
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
