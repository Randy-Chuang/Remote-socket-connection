package project.remote.server.service;

import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;

import project.remote.common.service.MessageEncode;
import project.remote.common.service.MessageDecode;
import project.remote.server.service.ISystemService.DateInfo;
import project.remote.server.service.ISystemService.SystemInfo;

public class ServerService implements IServerService{
	private final static SystemService systemService = new SystemService();
	private final static HashBiMap<String, String> systemInfoFieldMap = HashBiMap.create();
	static {
		systemInfoFieldMap.put("javaVersion", "Java version");
		systemInfoFieldMap.put("jvmVersion", "JVM version");
	}


	@Override
	public JsonObject getServerDate(JsonObject jsonRequest) {
		// Invoke designated method
		DateInfo dateInfo = systemService.getDate();
		// Encode replied JsonObject
		JsonObject jsonReply = MessageEncode.encodeDateInfo(dateInfo, jsonRequest);
		return jsonReply;
	}

	@Override
	public JsonObject getServerSystemInfo(JsonObject jsonRequest) {
		// Invoke designated method
		SystemInfo systemInfo = systemService.getSystemInfo();
		// Encode replied JsonObject
		JsonObject jsonReply = MessageEncode.encodeSystemInfo(systemInfo, jsonRequest);
		return jsonReply;
	}

	@Override
	public JsonObject getServerSquare(JsonObject jsonRequest) {
		// Retrieve parameters
		double base = MessageDecode.getSquareParameters(jsonRequest);
		// Invoke designated method
		double squared = systemService.square(base);
		// Encode replied JsonObject
		JsonObject jsonReply = MessageEncode.encodeSquare(squared, jsonRequest);
		
		return jsonReply;
	}

	
	
}
