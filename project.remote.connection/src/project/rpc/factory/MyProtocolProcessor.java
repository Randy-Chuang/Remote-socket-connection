package project.rpc.factory;

import java.io.BufferedReader;
import java.io.IOException;

import project.remote.common.service.IOUtility;
import project.remote.common.service.NetMessage;
import project.rpc.factory.IJsonRpcServer.ProtocolProcessor;

public class MyProtocolProcessor extends ProtocolProcessor{

	protected MyProtocolProcessor() {
		super("Exit");
	}

	@Override
	protected String read(BufferedReader reader) {
		try {
			if(reader.ready()) {
				return reader.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected String decode(BufferedReader reader, String header) {
		String received;
		try {
			// Decode for header and get the length of request.
			int length = NetMessage.decodeHeader(header);
			// skip a line. 
			reader.readLine();
			// fetch requested message with length.
			received = IOUtility.read(reader, length);
		} catch (Exception e) {
			received = null;
			e.printStackTrace();
		}
		return received;
	}

	@Override
	protected String encode(String message) {
		return NetMessage.netMessageEncode(message);
	}

}
