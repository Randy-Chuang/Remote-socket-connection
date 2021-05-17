package project.rpc.factory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class JsonFormatProcessor implements IFormatProcessor {
	public final static String METHOD_OBJ_STRING = "method", 
								PARAMETERS_OBJ_STRING = "params",
								RETURN_OBJ_STRING = "return";

	public final static JsonObject jsonMessageHeader = new JsonObject();
	static {
		Gson gson = new Gson();
		jsonMessageHeader.add("jsonrpc", gson.toJsonTree("2.0"));
		jsonMessageHeader.add("id", gson.toJsonTree(1));
	}
	
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

	@Override
	public String getMethod(final String message) {	
		JsonObject jsonObject = (JsonObject)toObjectFormat(message);
		return jsonObject.get(METHOD_OBJ_STRING).getAsString();
	}

	@Override
	public Object toObjectFormat(String message) {
		if(message == null) {
			return null;
		}
		JsonParser jsonParser = new JsonParser();		
		return jsonParser.parse(message).getAsJsonObject();
	}
	
	@Override
	public String encode(final Object refObject, final String methodName, Object returnVal, Object... param) {
		JsonObject jsonObject = jsonMessageHeader.deepCopy();
		Gson gson = new GsonBuilder().serializeNulls().create();
		
		if(refObject != null) {
			copyRequestInfo(jsonObject, (JsonObject)refObject);
		}
		else {
			jsonObject.add(METHOD_OBJ_STRING, gson.toJsonTree(methodName));
			jsonObject.add(PARAMETERS_OBJ_STRING, gson.toJsonTree(param));
		}

		jsonObject.add(RETURN_OBJ_STRING, gson.toJsonTree(returnVal));
		
		return jsonObject.toString();
	}
	
	@Override
	public String encode(final String refRequest, final String methodName, Object returnVal, Object... param) {
		JsonObject jsonObject = jsonMessageHeader.deepCopy();
		Gson gson = new GsonBuilder().serializeNulls().create();
		
		if(refRequest != null) {
			copyRequestInfo(jsonObject, (JsonObject)toObjectFormat(refRequest));
		}
		else {
			jsonObject.add(METHOD_OBJ_STRING, gson.toJsonTree(methodName));
			if(param == null || param.length == 0) {
				jsonObject.add(PARAMETERS_OBJ_STRING, JsonNull.INSTANCE);
			}
			else if(param.length == 1) {
				jsonObject.add(PARAMETERS_OBJ_STRING, gson.toJsonTree(param[0]));
			}
			else {
				jsonObject.add(PARAMETERS_OBJ_STRING, gson.toJsonTree(param));
			}
		}

		jsonObject.add(RETURN_OBJ_STRING, gson.toJsonTree(returnVal));
		
		return jsonObject.toString();
	}

	@Override
	public Object decodeParam(final String message, Class<?> typeClass) {	
		JsonObject jsonObject = (JsonObject)toObjectFormat(message);
		
		Gson gson = new Gson();
		JsonElement paramElement = jsonObject.get(PARAMETERS_OBJ_STRING);
		if(paramElement.isJsonNull()) {
			return null;
		}
		else if(typeClass != null) {
			return gson.fromJson(paramElement, typeClass);
		}
		else {
			return gson.fromJson(paramElement, Object.class);
		}
		
	}

	@Override
	public Object decodeReturnVal(String message, Class<?> typeClass) {
		JsonObject jsonObject = (JsonObject)toObjectFormat(message);
		
		Gson gson = new Gson();
		JsonElement paramElement = jsonObject.get(RETURN_OBJ_STRING);
		if(paramElement.isJsonNull()) {
			return null;
		}
		else if(typeClass != null) {
			return gson.fromJson(paramElement, typeClass);
		}
		else {
			return gson.fromJson(paramElement, Object.class);
		}
	}

	@Override
	public String prettyOutput(String message) {
		JsonParser jsonParser = new JsonParser();		
		JsonObject jsonObject = jsonParser.parse(message).getAsJsonObject();
		Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
		return gson.toJson(jsonObject);
	}
		
}
