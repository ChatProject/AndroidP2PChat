package com.jakecrane.p2pchat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

public class Server {

	public Server(final int port, final MainActivity mainActivity, final TextView chatTextView) {
        new Thread() {
            @Override
            public void run() {
                try (ServerSocket serverSocket = new ServerSocket(port)) {
                    Log.d("Server started on " + port, "");
                    while (true) {
                        try {
                            Socket socket = serverSocket.accept();
                            new ServerThread(socket, mainActivity, chatTextView).start();
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
    private MainActivity mainActivity;

	public ServerThread(Socket socket, final MainActivity mainActivity, TextView chatTextView) {
		this.socket = socket;
		this.chatTextView = chatTextView;
        this.mainActivity= mainActivity;
	}

	@Override
	public void run() {
		try (ObjectInputStream inputFromClient = new ObjectInputStream(socket.getInputStream())) {

			final Data d = (Data)inputFromClient.readObject();
			d.setReceivedTime(System.currentTimeMillis());

			//System.out.println("server received " + s.getMessage());
            mainActivity.runOnUiThread(new Runnable() {
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
