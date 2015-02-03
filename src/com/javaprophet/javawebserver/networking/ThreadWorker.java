package com.javaprophet.javawebserver.networking;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import com.javaprophet.javawebserver.JavaWebServer;
import com.javaprophet.javawebserver.networking.packets.RequestPacket;
import com.javaprophet.javawebserver.networking.packets.ResponsePacket;
import com.javaprophet.javawebserver.util.Logger;

public class ThreadWorker extends Thread {
	
	public ThreadWorker() {
		
	}
	
	public static void clearWork() {
		workQueue.clear();
	}
	
	private static LinkedBlockingQueue<Work> workQueue = new LinkedBlockingQueue<Work>();
	
	public static void addWork(Socket s, DataInputStream in, DataOutputStream out, boolean ssl) {
		workQueue.add(new Work(s, in, out, ssl));
	}
	
	public static void readdWork(Work work) {
		workQueue.add(work);
	}
	
	private boolean keepRunning = true;
	
	public void close() {
		keepRunning = false;
	}
	
	public static final ArrayList<ThreadStreamWorker> subworkers = new ArrayList<ThreadStreamWorker>();
	
	public void run() {
		while (keepRunning) {
			Work focus = workQueue.poll();
			if (focus == null) {
				try {
					Thread.sleep(10L);
				}catch (InterruptedException e) {
					e.printStackTrace();
				}
				continue;
			}
			try {
				if (!focus.s.isClosed()) {
					RequestPacket incomingRequest = RequestPacket.read(focus.in);
					long benchStart = System.nanoTime();
					if (incomingRequest == null) {
						focus.s.close();
						continue;
					}
					incomingRequest.ssl = focus.ssl;
					focus.tos = 0;
					incomingRequest.userIP = focus.s.getInetAddress().getHostAddress();
					incomingRequest.userPort = focus.s.getPort();
					long set = System.nanoTime();
					ResponsePacket outgoingResponse = new ResponsePacket();
					outgoingResponse.request = incomingRequest;
					JavaWebServer.patchBus.processPacket(incomingRequest);
					long proc1 = System.nanoTime();
					boolean cont = JavaWebServer.rg.process(incomingRequest, outgoingResponse);
					long resp = System.nanoTime();
					if (cont) JavaWebServer.patchBus.processPacket(outgoingResponse);
					long proc2 = System.nanoTime();
					ResponsePacket wrp = outgoingResponse.write(focus.out);
					boolean t = wrp.reqTransfer;
					long write = System.nanoTime();
					if (t && wrp.body != null && wrp.body.getBody() != null) {
						ThreadStreamWorker sw = new ThreadStreamWorker(focus, incomingRequest, wrp);
						subworkers.add(sw);
						sw.start();
					}else {
						workQueue.add(focus);
					}
					long cur = System.nanoTime();
					// System.out.println((set - benchStart) / 1000000D + " start-set");
					// System.out.println((proc1 - set) / 1000000D + " set-proc1");
					// System.out.println((resp - proc1) / 1000000D + " proc1-resp");
					// System.out.println((proc2 - resp) / 1000000D + " resp-proc2");
					// System.out.println((write - proc2) / 1000000D + " proc2-write");
					// System.out.println((cur - write) / 1000000D + " write-cur");
					Logger.INSTANCE.log(incomingRequest.userIP + " " + incomingRequest.method.name + " " + incomingRequest.target + " returned " + wrp.statusCode + " " + wrp.reasonPhrase + " took: " + (cur - benchStart) / 1000000D + " ms");
				}else {
					Logger.INSTANCE.log(focus.s.getInetAddress().getHostAddress() + " closed.");
				}
			}catch (SocketTimeoutException e) {
				focus.tos++;
				if (focus.tos < 10) {
					workQueue.add(focus);
				}else {
					// e.printStackTrace();
					try {
						focus.s.close();
					}catch (IOException ex) {
						ex.printStackTrace();
					}
					Logger.INSTANCE.log(focus.s.getInetAddress().getHostAddress() + " closed.");
				}
			}catch (IOException e) {
				if (!(e instanceof SocketException)) e.printStackTrace();
				Logger.INSTANCE.log(focus.s.getInetAddress().getHostAddress() + " closed.");
			}
		}
	}
}