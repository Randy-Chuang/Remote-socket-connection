package project.remote.common.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class MessageField {
	public final static String METHOD_OBJ_STRING = "method", 
									PARAMETERS_OBJ_STRING = "params", 
									RETURN_OBJ_STRING = "return";
	
	public final static JsonObject jsonMessageHeader = new JsonObject();
	static {
		Gson gson = new Gson();
		jsonMessageHeader.add("jsonrpc", gson.toJsonTree("2.0"));
		jsonMessageHeader.add("id", gson.toJsonTree(1));
	}
}
