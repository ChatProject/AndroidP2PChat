package com.jakecrane.p2pchat;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;
import android.widget.TextView;

import java.lang.Thread;

public class Client {

    public static final SimpleDateFormat MY_FORMAT = new SimpleDateFormat("[h:mm:ss] ");

	public Client(final ChatActivity chatActivity, final EditText textField, final TextView chatTextView, Button button) throws UnknownHostException, IOException {

                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        try (Socket socket = new Socket(ChatActivity.peerIpAddress, ChatActivity.peerOpenPort)) {
                            try (ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream())) {
                                final Data d = new Data(textField.getText().toString());
                                chatActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        chatTextView.append(MY_FORMAT.format(new Date(d.getCreatedTime())) + "sent " + textField.getText() + "\n");
                                        textField.setText("");
                                    }
                                });
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
                            chatActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    chatTextView.append(MY_FORMAT.format(new Date()) + "unable to send message" + "\n");
                                }
                            });
                            Log.d("", "", e3);
                        }
                    }
                }.start();
            }
        });

	}
}
