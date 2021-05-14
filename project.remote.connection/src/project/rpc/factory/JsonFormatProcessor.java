package project.rpc.factory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/*
 * TODO: 
 */
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
		System.out.println("encode.is param null: " + (param == null) + ", len = " + param.length);
		
		JsonObject jsonObject = jsonMessageHeader.deepCopy();
		Gson gson = new GsonBuilder().serializeNulls().create();
		
		if(refRequest != null) {
			copyRequestInfo(jsonObject, (JsonObject)toObjectFormat(refRequest));
		}
		else {
			jsonObject.add(METHOD_OBJ_STRING, gson.toJsonTree(methodName));
			jsonObject.add(PARAMETERS_OBJ_STRING, gson.toJsonTree(param));
		}

		jsonObject.add(RETURN_OBJ_STRING, gson.toJsonTree(returnVal));
		
		return jsonObject.toString();
	}

	@Override
	public Object decodeParam(final String message) {	
		JsonObject jsonObject = (JsonObject)toObjectFormat(message);
		
		Gson gson = new Gson();
		JsonElement paramElement = jsonObject.get(PARAMETERS_OBJ_STRING);
		if(paramElement.isJsonNull()) {
			return null;
		}
		else if(paramElement.isJsonArray()) {
			System.out.println("is json arr");
			return gson.fromJson(paramElement, Object[].class);
		}
		return gson.fromJson(paramElement, Object.class);
		
	}

	@Override
	public Object decodeReturnVal(String message) {
		JsonObject jsonObject = (JsonObject)toObjectFormat(message);
		
		Gson gson = new Gson();
		JsonElement paramElement = jsonObject.get(RETURN_OBJ_STRING);
		if(paramElement.isJsonNull()) {
			return null;
		}
		else if(paramElement.isJsonArray()) {
			return gson.fromJson(paramElement, Object[].class);
		}
		return gson.fromJson(paramElement, Object.class);
	}

	@Override
	public String prettyOutput(String message) {
		JsonParser jsonParser = new JsonParser();		
		JsonObject jsonObject = jsonParser.parse(message).getAsJsonObject();
		Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
		return gson.toJson(jsonObject);
	}
	
	@Override
	public void addMethodReturnType(String method, Class<?> type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Class<?> getMethodReturnType(String method) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addMethodParamType(String method, Class<?> type) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Class<?> getMethodParamType(String method) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	public static void main(String[] args) {
		JsonFormatProcessor processor = new JsonFormatProcessor();
		Object[] arr = new Object[] { "asdf", new Integer(123), new Double[] {1.0,4.0 ,2.0,3.0}, new Integer(123)};
		String string = processor.encode(null, "getDate", arr, null);
		arr = (Object[])processor.decodeParam(string);
	}

}
