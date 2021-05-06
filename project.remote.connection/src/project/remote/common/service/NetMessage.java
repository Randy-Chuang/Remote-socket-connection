package project.remote.common.service;

import java.io.UnsupportedEncodingException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class NetMessage {
	private static final String MESSAGE_HEADER_PREFIX = "Content-Length:";
	private static final String MESSAGE_HEADER_SUFFIX = "\r\n\r\n";
	private static final String UTF_8_STRING = "UTF-8";

	public static String netMessageEncode(JsonObject jsonObject) throws UnsupportedEncodingException {
		Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
		String jsonString = gson.toJson(new JsonParser().parse(jsonObject.toString()));
		
		String message = new String(MESSAGE_HEADER_PREFIX);
		Integer length = jsonString.getBytes(UTF_8_STRING).length;
		
		message += length.toString() + MESSAGE_HEADER_SUFFIX + jsonString;
		
		return message;
		
	}
	
	public static JsonObject netMessageDecode(String message) throws Exception {
		String[] tokens = message.split(MESSAGE_HEADER_SUFFIX, 2);
		if(tokens.length != 2 || !tokens[0].startsWith(MESSAGE_HEADER_PREFIX)) {
			throw new Exception("Invalid format: requested message doesn't meet the required format!");
		}
		
		Integer length = Integer.valueOf(tokens[0].substring(MESSAGE_HEADER_PREFIX.length()).trim());
		
		String jsonString = tokens[1].substring(0, length.intValue());
		
		JsonParser jsonParser = new JsonParser();		
		JsonObject jsonObject = jsonParser.parse(jsonString).getAsJsonObject();
		
		return jsonObject;
	}
}
