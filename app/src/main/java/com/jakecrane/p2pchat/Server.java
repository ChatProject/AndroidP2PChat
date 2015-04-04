package com.jakecrane.p2pchat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import android.util.Log;
import android.widget.TextView;

public class Server {

	public Server(final ChatActivity chatActivity, final TextView chatTextView) {
        new Thread() {
            @Override
            public void run() {
                try (ServerSocket serverSocket = new ServerSocket(ChatActivity.myOpenPort)) {
                    Log.d("Server started on " + ChatActivity.myOpenPort, "");
                    while (true) {
                        try {
                            Socket socket = serverSocket.accept();
                            new ServerThread(socket, chatActivity, chatTextView).start();
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
	private TextView chatTextView;
    private ChatActivity chatActivity;

	public ServerThread(Socket socket, final ChatActivity chatActivity, TextView chatTextView) {
		this.socket = socket;
		this.chatTextView = chatTextView;
        this.chatActivity = chatActivity;
	}

	@Override
	public void run() {
		try (ObjectInputStream inputFromClient = new ObjectInputStream(socket.getInputStream())) {

			final Data d = (Data)inputFromClient.readObject();
			d.setReceivedTime(System.currentTimeMillis());

			//System.out.println("server received " + s.getMessage());
            chatActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chatTextView.append(Client.MY_FORMAT.format(new Date(d.getReceivedTime())) + "received " + d.getMessage() + "\n");
                }
            });
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
