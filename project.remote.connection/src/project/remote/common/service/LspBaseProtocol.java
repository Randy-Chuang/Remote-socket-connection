package project.remote.common.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * A message encoder encode message according to Microsoft LSP base protocol. 
 *
 * @see <a href="https://microsoft.github.io/language-server-protocol/specifications/specification-current/#baseProtocol">Microsoft LSP - Base protocol</a>
 */
public class LspBaseProtocol {
	// Fields used in message header processing.
	private static final String HEADER_PREFIX_CHECK = "Content-Length:";
	private static final String HEADER_PREFIX = "Content-Length: ";
	private static final String DELIMITER = "\r\n";
	private static final String HEADER_SUFFIX = DELIMITER.repeat(2);
	
	// Gson object used for serialization / deserialization. (thread-safe indicated by Gson documentation)
	private static Gson gson = new GsonBuilder().serializeNulls().create();
	private static Gson prettyGson = new GsonBuilder().serializeNulls().setPrettyPrinting().create();
	
	/**
	 * Decode the info of length value from the given formatted header. 
	 * @param message the string of formatted header. 
	 * @return the integer value encoded in the header. 
	 * @throws Exception if the given string does not meet the requirement of header prefix. 
	 */
	public static int decodeHeader(String message) throws Exception {
		if(message == null || !message.startsWith(HEADER_PREFIX_CHECK)) {
			throw new Exception("Invalid format: Not a valid header of LSP base protocol!");
		}
		// Strip the substring of length value and parse it into Integer. 
		Integer length = Integer.valueOf(message.substring(HEADER_PREFIX_CHECK.length()).trim());
		return length.intValue();
	}

	/**
	 * Encode the given object according to protocol. 
	 * @param jsonObject the JSON object to be encapsulated in protocol formatted message. 
	 * @param prettyFormat the indicator of encoding JsonObject with pretty format or not. 
	 * @return the protocol formatted message. 
	 */
	public static String encode(JsonObject jsonObject, boolean prettyFormat) {
		String jsonString;
		if(prettyFormat) {
			jsonString = prettyGson.toJson(new JsonParser().parse(jsonObject.toString())) + DELIMITER;
		}
		else {
			jsonString = gson.toJson(new JsonParser().parse(jsonObject.toString())) + DELIMITER;
		}
		// Generate encoded message. 
		Integer length = jsonString.length();
		String message = HEADER_PREFIX + length.toString() + HEADER_SUFFIX + jsonString;
		return message;
	}
	
	/**
	 * Encode the given pure message according to protocol. 
	 * @param pureMessage the pure message to be encapsulated in protocl formatted message. 
	 * @return the protocol formatted message. 
	 */
	public static String encode(String pureMessage) {
		pureMessage += DELIMITER;
		Integer length = pureMessage.length();
		// Generate encoded message. 
		String message = HEADER_PREFIX + length.toString() + HEADER_SUFFIX + pureMessage;
		return message;
	}
	
}
