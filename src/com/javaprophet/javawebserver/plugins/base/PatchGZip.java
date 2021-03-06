package com.javaprophet.javawebserver.plugins.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.GZIPOutputStream;
import com.javaprophet.javawebserver.networking.Packet;
import com.javaprophet.javawebserver.networking.packets.RequestPacket;
import com.javaprophet.javawebserver.networking.packets.ResponsePacket;
import com.javaprophet.javawebserver.plugins.Patch;
import com.javaprophet.javawebserver.util.Logger;

public class PatchGZip extends Patch {
	
	public PatchGZip(String name) {
		super(name);
	}
	
	@Override
	public void formatConfig(HashMap<String, Object> json) {
		
	}
	
	@Override
	public boolean shouldProcessPacket(Packet packet) {
		return false;
	}
	
	@Override
	public void processPacket(Packet packet) {
		
	}
	
	@Override
	public boolean shouldProcessResponse(ResponsePacket response, RequestPacket request, byte[] data) {
		return request.headers.hasHeader("Accept-Encoding") && request.headers.getHeader("Accept-Encoding").contains("gzip") && !response.headers.hasHeader("Content-Encoding") && ((data != null && data.length > 0) || (response.body != null && response.body.getBody() != null && response.body.getBody().tooBig));
	}
	
	@Override
	public byte[] processResponse(ResponsePacket response, RequestPacket request, byte[] data) {
		byte[] data2 = data;
		try {
			if (data != null && data.length > 0) {
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				GZIPOutputStream gout = new GZIPOutputStream(bout);
				gout.write(data, 0, data.length);
				gout.flush();
				gout.close();
				data2 = bout.toByteArray();
			}
			response.headers.addHeader("Content-Encoding", "gzip");
			response.headers.addHeader("Vary", "Accept-Encoding");
		}catch (IOException e) {
			Logger.logError(e);
		}
		return data2;
	}
	
	@Override
	public void processMethod(RequestPacket request, ResponsePacket response) {
		
	}
	
}
