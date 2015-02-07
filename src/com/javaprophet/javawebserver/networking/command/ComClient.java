package com.javaprophet.javawebserver.networking.command;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

public class ComClient {
	static Socket cs = null;
	static DataInputStream scan = null;
	static PrintStream out = null;
	
	public static void run(String ip, int port) {
		Thread mte = new Thread() {
			public void run() {
				Scanner inp = new Scanner(System.in);
				while (inp.hasNextLine()) {
					String com = inp.nextLine();
					if (com.equals("close")) {
						System.exit(0);
					}
					if (cs != null && out != null && !cs.isClosed()) {
						out.println(com);
						out.flush();
					}
				}
			}
		};
		mte.start();
		while (true) {
			try {
				cs = new Socket(ip, port);
				out = new PrintStream(cs.getOutputStream());
				out.flush();
				scan = new DataInputStream(cs.getInputStream());
				int nc = 0;
				while (!cs.isClosed()) {
					String com = scan.readLine();
					if (com == null) {
						nc++;
						if (nc >= 10) {
							cs.close();
							throw new Exception();
						}else {
							continue;
						}
					}
					System.out.println(com);
				}
			}catch (Exception e) {
				System.out.println("Connection Terminated, restarting... [close]");
			}
			try {
				Thread.sleep(1000L);
			}catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
