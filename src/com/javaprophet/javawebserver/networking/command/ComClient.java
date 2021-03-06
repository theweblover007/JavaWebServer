package com.javaprophet.javawebserver.networking.command;

import java.io.DataInputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;


/**
 * Command Client that is connected to ComServer to handle commands from the outside.
 */
public class ComClient {

    /**
     * Our socket connection
     */
	static Socket cs = null;

    /**
     * Our inputstream to read from the socket connection
     */
	static DataInputStream scan = null;

    /**
     * Out writer to write to the ComClient
     */
	static PrintStream out = null;

    /**
     * Runs the ComClient
     * @param ip the ip
     * @param port the port to use
     */
	public static void run(String ip, int port) {
        //Handles console/com input and processes.
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
        //Start the thread looping
		mte.start();

        //TODO: What does this do JavaProphet?
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
