package project.remote.common.service;

import com.google.gson.JsonObject;

import project.remote.server.service.ISystemService.DateInfo;
import project.remote.server.service.ISystemService.SystemInfo;

public class MessageDecode extends MessageField{
	
	public static String getMethod(final JsonObject jsonRequest) {
		if(jsonRequest == null) {
			return null;
		}
		return jsonRequest.get(MessageField.METHOD_OBJ_STRING).getAsString();
	}
	
	public static double getSquareParameters(final JsonObject jsonRequest) {
		if(jsonRequest == null) {
			return Double.NaN;
		}
		return jsonRequest.get(MessageField.PARAMETERS_OBJ_STRING).getAsDouble();
	}

	
	public static DateInfo decodeDateInfo(final JsonObject jsonRequest) {
		return null;
	}

	public static SystemInfo decodeSystemInfo(final JsonObject jsonRequest) {
		return null;
	}

	public static double decodeSquare(final JsonObject jsonRequest) {
		jsonRequest.get(MessageField.PARAMETERS_OBJ_STRING);
		// adding different fields or copying from request 
		return 0;
	}
}
