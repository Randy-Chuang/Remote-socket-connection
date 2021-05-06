package project.remote.server.service;

import com.google.gson.JsonObject;

public interface IServerService {
	public JsonObject getServerDate(JsonObject jsonRequest);
	public JsonObject getServerSystemInfo(JsonObject jsonRequest);
	public JsonObject getServerSquare(JsonObject jsonRequest);
}
