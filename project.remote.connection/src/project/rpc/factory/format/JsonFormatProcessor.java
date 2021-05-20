package project.rpc.factory.format;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * A format processor to encode / decode message to / from JSON-RPC format. 
 * 
 */
public class JsonFormatProcessor implements IFormatProcessor {
	// Common used field names.
	private final static String METHOD_OBJ_STRING = "method", 
								PARAMETERS_OBJ_STRING = "params",
								RETURN_OBJ_STRING = "return";
	// Common header of JSON RPC format. 
	private final static JsonObject jsonMessageHeader = new JsonObject();
	static {
		Gson gson = new Gson();
		jsonMessageHeader.add("jsonrpc", gson.toJsonTree("2.0"));
		jsonMessageHeader.add("id", gson.toJsonTree(1));
	}
	// Gson object used for serialization / deserialization. (thread-safe indicated by Gson documentation)
	private static Gson gson = new GsonBuilder().serializeNulls().create();
	private static Gson prettyGson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
	// JSON parser used for parsing string into JsonObject. 
	private JsonParser jsonParser = new JsonParser();
	
	/**
	 * Decode the service (method) name from formatted message.
	 * @param message the formatted message. 
	 * @return service (method) name.
	 */
	@Override
	public String getMethod(final String message) {	
		JsonObject jsonObject = (JsonObject)toObjectFormat(message);
		return jsonObject.get(METHOD_OBJ_STRING).getAsString();
	}

	/**
	 * Convert the formatted message into object associated with the adopted parser. 
	 * @param message the formatted message. 
	 * @return object associated with the adopted parser.
	 */
	@Override
	public Object toObjectFormat(String message) {
		if(message == null) {
			return null;
		}	
		return jsonParser.parse(message).getAsJsonObject();
	}
	
	/**
	 * Encode the given info into a formatted message. 
	 * @param method
	 * @param returnVal
	 * @param param
	 * @return
	 */
	@Override
	public String encode(String method, Object returnVal, Object... param) {
		JsonObject jsonObject = jsonMessageHeader.deepCopy();

		jsonObject.add(METHOD_OBJ_STRING, gson.toJsonTree(method));
		if(param == null || param.length == 0) {
			jsonObject.add(PARAMETERS_OBJ_STRING, JsonNull.INSTANCE);
		}
		else if(param.length == 1) {
			jsonObject.add(PARAMETERS_OBJ_STRING, gson.toJsonTree(param[0]));
		}
		else {
			jsonObject.add(PARAMETERS_OBJ_STRING, gson.toJsonTree(param));
		}

		jsonObject.add(RETURN_OBJ_STRING, gson.toJsonTree(returnVal));
		
		return jsonObject.toString();
	}

	/**
	 * Decode parameter from formatted message with a given class type for parsing. 
	 * @param message the formatted message. 
	 * @param typeClass the class type used for parsing parameter. 
	 * @return the parameter object.
	 */
	@Override
	public Object decodeParam(final String message, Class<?> typeClass) {	
		JsonObject jsonObject = (JsonObject)toObjectFormat(message);
		
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

	/**
	 * Decode return value from formatted message with a given class type for parsing. 
	 * @param message the formatted message. 
	 * @param typeClass the class type used for parsing return value. 
	 * @return the returned object. 
	 */
	@Override
	public Object decodeReturnVal(String message, Class<?> typeClass) {
		JsonObject jsonObject = (JsonObject)toObjectFormat(message);
		
		JsonElement paramElement = jsonObject.get(RETURN_OBJ_STRING);
		if(paramElement.isJsonNull()) {
			return null;
		}
		else if(typeClass != null) {
			return gson.fromJson(paramElement, typeClass);
		}
		else {
			System.err.println("Null argument of returned type addressing!");
			return gson.fromJson(paramElement, Object.class);
		}
	}

	/**
	 * Convert the formatted message into a prettier formatted string for printing. 
	 * @param message the formatted message. 
	 * @return a prettier formatted string. 
	 */
	@Override
	public String prettyOutput(String message) {	
		JsonObject jsonObject = jsonParser.parse(message).getAsJsonObject();
		return prettyGson.toJson(jsonObject);
	}
		
}
