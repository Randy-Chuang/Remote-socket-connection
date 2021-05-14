package project.remote.server.service;

import com.google.gson.JsonObject;

import project.remote.common.service.MessageDecode;
import project.remote.common.service.MessageEncode;
import project.remote.common.service.ServiceClass.DateInfo;
import project.remote.common.service.ServiceClass.SystemInfo;

public class ServerService implements IServerService{
	private final static SystemService systemService = new SystemService();

	@Override
	public JsonObject getServerDate(JsonObject jsonRequest) {
		// Invoke designated method
		DateInfo dateInfo = systemService.getDate();
		// Encode replied JsonObject
		JsonObject jsonReply = MessageEncode.encodeDateInfo(jsonRequest, dateInfo);
		return jsonReply;
	}

	@Override
	public JsonObject getServerSystemInfo(JsonObject jsonRequest) {
		// Invoke designated method
		SystemInfo systemInfo = systemService.getSystemInfo();
		// Encode replied JsonObject
		JsonObject jsonReply = MessageEncode.encodeSystemInfo(jsonRequest, systemInfo);
		return jsonReply;
	}

	@Override
	public JsonObject getServerSquare(JsonObject jsonRequest) {
		// Retrieve parameters
		double base = MessageDecode.getSquareParameters(jsonRequest);
		// Invoke designated method
		double squared = systemService.square(base);
		// Encode replied JsonObject
		JsonObject jsonReply = MessageEncode.encodeSquare(jsonRequest, squared);
		
		return jsonReply;
	}
}
