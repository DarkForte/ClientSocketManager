package com.zero.clientsocketmanager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ClientSocketManager 
{
	String ip;
	int port;
	Socket socket;
	Handler handler;
	
	/**
	 * Thread to get message from server
	 * @author Zero
	 *
	 */
	class GetThread implements Runnable
	{
		@Override
		public void run() 
		{
			try 
			{
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				while(true)
				{
					String input = in.readLine();
					if(input!=null)
					{
						Message msg = createMessage(input);
						handler.sendMessage(msg);
					}
				} 
			}
			catch (IOException e) 
			{
				System.out.println("NetThreadIOException");
				Message msg = createMessage("net error");
				handler.sendMessage(msg);
			}
			catch (NullPointerException e)
			{
				//System.out.println("NetThreadIOException");
				Message msg = createMessage("net error");
				handler.sendMessage(msg);
			}
		}
	}
	
	/**
	 * Thread to login to server
	 * @author Zero
	 *
	 */
	class LoginThread extends Thread
	{
		boolean ok;
		@Override
		public void run() 
		{
			try 
			{
				socket = new Socket();
				socket.connect(new InetSocketAddress(ip, port) , 5000);
				Message msg = createMessage("ok");
				handler.sendMessage(msg);
				ok = true;
				
			} catch (UnknownHostException e) 
			{
				System.out.println("UnknownHostException");
				Message msg = createMessage("did not login");
				handler.sendMessage(msg);
				ok=false;
			} catch (IOException e) {
				System.out.println("IOException");
				Message msg = createMessage("did not login");
				handler.sendMessage(msg);
				ok=false;
			}
		}
	}
	
	/**
	 * Thread to send something to the server
	 * @author Zero
	 *
	 */
	class SendThread implements Runnable
    {
		String send_message;
		@Override
		public void run() 
		{
			PrintWriter cout;
			try 
			{
				cout = new PrintWriter(socket.getOutputStream(),true);
				cout.println(send_message);
			} catch (IOException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		SendThread(String msg)
		{
			send_message = msg;
		}
    	
    }
	
	protected Message createMessage(String str)
	{
		Message msg = new Message();
		msg.obj = str;
		return msg;
	}
	
	public ClientSocketManager(){}
	
	/**
	 * Create a new ClientSocketManager
	 * @param _ip
	 * @param _port
	 * @param _handler
	 */
	public ClientSocketManager(String _ip, int _port, Handler _handler)
	{
		ip = _ip;
		port = _port;
		handler = _handler;
	}
	
	public void loginUnblocked()
	{
		LoginThread login_thread = new LoginThread();
		login_thread.start();
		return;
	}
	
	public boolean loginBlocked()
	{
		LoginThread login_thread = new LoginThread();
		login_thread.start();
		try 
		{
			login_thread.join();
		} catch (InterruptedException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return login_thread.ok;
		
	}
	
	public void startGetThread()
	{
		GetThread getThread = new GetThread();
		new Thread(getThread).start();
	}
	
	public void sendMessage(String msg)
	{
		SendThread sendThread = new SendThread(msg);
		new Thread(sendThread).start();
	}
	
	public Socket getSocket()
	{
		return socket;
	}
}

