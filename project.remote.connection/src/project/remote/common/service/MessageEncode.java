package project.remote.common.service;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import project.remote.common.service.ServiceClass.DateInfo;
import project.remote.common.service.ServiceClass.SystemInfo;

public class MessageEncode extends MessageField {
	
	/*
	 * Copy the requested info (method and parameters) into replied JsonObject.
	 */
	private static void copyRequestInfo(JsonObject jsonReply, final JsonObject jsonRefRequest) {
		if(jsonReply == null || jsonRefRequest == null) {
			return;
		}
		// Making deep copy from mandatory info of request to reply. 
		// com.google.gson.JsonObject.add() only add the reference into the object.
		jsonReply.add(METHOD_OBJ_STRING, jsonRefRequest.get(METHOD_OBJ_STRING).deepCopy());
		jsonReply.add(PARAMETERS_OBJ_STRING, jsonRefRequest.get(PARAMETERS_OBJ_STRING).deepCopy());
	}

	public static JsonObject encodeDateInfo(DateInfo dateInfo, final JsonObject jsonRefRequest) {
		JsonObject jsonObject = MessageField.jsonMessageHeader.deepCopy();
		Gson gson = new GsonBuilder().serializeNulls().create();
		
		if(jsonRefRequest != null) {
			copyRequestInfo(jsonObject, jsonRefRequest);
		}
		else {
			jsonObject.add(METHOD_OBJ_STRING, gson.toJsonTree("getDate"));
			jsonObject.add(PARAMETERS_OBJ_STRING, JsonNull.INSTANCE);
		}
		
		if(dateInfo == null) {
			dateInfo = new DateInfo();
		}
				
		jsonObject.add(RETURN_OBJ_STRING, gson.toJsonTree(dateInfo));
		
		return jsonObject;
	}

	public static JsonObject encodeSystemInfo(SystemInfo systemInfo, final JsonObject jsonRefRequest) {
		JsonObject jsonObject = MessageField.jsonMessageHeader.deepCopy();
		Gson gson = new GsonBuilder().serializeNulls().create();
		
		if(jsonRefRequest != null) {
			copyRequestInfo(jsonObject, jsonRefRequest);
		}
		else {
			jsonObject.add(METHOD_OBJ_STRING, gson.toJsonTree("getSystemInfo"));
			jsonObject.add(PARAMETERS_OBJ_STRING, JsonNull.INSTANCE);
		}
		
		if(systemInfo == null) {
			systemInfo = new SystemInfo();
		}
		
		JsonElement resultElement = gson.toJsonTree(systemInfo);
		JsonObject resultObject = resultElement.getAsJsonObject();
		
		for(Map.Entry<String, String> entry: systemInfoFieldMap.entrySet()) {
			JsonElement temp = resultObject.get(entry.getKey());
			resultObject.remove(entry.getKey());
			resultObject.add(entry.getValue(), temp);
		}
		jsonObject.add(RETURN_OBJ_STRING, resultElement);
		
		return jsonObject;
	}

	public static JsonObject encodeSquare(Double squared, final JsonObject jsonRefRequest) {
		JsonObject jsonObject = MessageField.jsonMessageHeader.deepCopy();
		Gson gson = new GsonBuilder().serializeNulls().create();
		
		if(jsonRefRequest != null) {
			copyRequestInfo(jsonObject, jsonRefRequest);
		}
		else {
			jsonObject.add(METHOD_OBJ_STRING, gson.toJsonTree("square"));
			jsonObject.add(PARAMETERS_OBJ_STRING, JsonNull.INSTANCE);
		}

		
		jsonObject.add(RETURN_OBJ_STRING, gson.toJsonTree(squared));
		
		return jsonObject;
	}
	
	/*
	 * Return a String for pretty printing. 
	 */
	public static String jsonObjectPrettyString(final JsonObject jsonRef) {
		Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
		return gson.toJson(jsonRef);
	}
}
