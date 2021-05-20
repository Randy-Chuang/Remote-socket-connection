package project.rpc.factory.format;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * A JSON format processor to encode to and decode from JSON-RPC format. 
 * 
 * @see <a href="https://microsoft.github.io/language-server-protocol/specifications/specification-current/#contentPart">Microsoft LSP - Content Part</a>
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
	 * Convert the JSON formatted message into JsonObject. 
	 * @param message the JSON formatted message. 
	 * @return JsonObject that represents the JSON formatted message. 
	 */
	private JsonObject toObjectFormat(String message) {
		if(message == null) {
			return null;
		}	
		return jsonParser.parse(message).getAsJsonObject();
	}
	
	/**
	 * Decode the service (method) name from JSON formatted message.
	 * @param message the JSON formatted message. 
	 * @return service (method) name.
	 */
	@Override
	public String getMethod(final String message) {	
		JsonObject jsonObject = toObjectFormat(message);
		return jsonObject.get(METHOD_OBJ_STRING).getAsString();
	}
	
	/**
	 * Encode the given info into a JSON formatted message. 
	 * @param method the service (method) name field in JSON formatted message.
	 * @param returnVal the object for return field in JSON formatted message. 
	 * @param param the object for parameter field in JSON formatted message.  
	 * @return the encoded JSON formatted message.
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
			System.err.println("Currently, only accpt up to one parameter for encoding!");
			return null;
		}

		jsonObject.add(RETURN_OBJ_STRING, gson.toJsonTree(returnVal));
		return jsonObject.toString();
	}

	/**
	 * Decode parameter from JSON formatted message with a given class type for parsing. 
	 * @param message the JSON formatted message. 
	 * @param typeClass the class type used for parsing parameter. 
	 * @return the parameter object.
	 */
	@Override
	public Object decodeParam(final String message, Class<?> typeClass) {	
		JsonObject jsonObject = toObjectFormat(message);
		JsonElement paramElement = jsonObject.get(PARAMETERS_OBJ_STRING);
		if(paramElement.isJsonNull()) {
			return null;
		}
		else if(typeClass != null) {
			return gson.fromJson(paramElement, typeClass);
		}
		else {
			System.err.println("Null argument of parameter type addressing!");
			return gson.fromJson(paramElement, Object.class);
		}
	}

	/**
	 * Decode return value from JSON formatted message with a given class type for parsing. 
	 * @param message the JSON formatted message. 
	 * @param typeClass the class type used for parsing return value. 
	 * @return the returned object. 
	 */
	@Override
	public Object decodeReturnVal(String message, Class<?> typeClass) {
		JsonObject jsonObject = toObjectFormat(message);
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
	 * Convert the JSON formatted message into a prettier formatted string for printing. 
	 * @param message the JSON formatted message. 
	 * @return a prettier formatted string. 
	 */
	@Override
	public String prettyOutput(String message) {	
		JsonObject jsonObject = jsonParser.parse(message).getAsJsonObject();
		return prettyGson.toJson(jsonObject);
	}
	
}
