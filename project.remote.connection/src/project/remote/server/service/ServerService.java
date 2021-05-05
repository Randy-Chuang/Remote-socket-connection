package project.remote.server.service;

import com.google.gson.Gson;

public class ServerService implements IServerService{
	private final static SystemService systemService = new SystemService();

	@Override
	public String getServerDate() {
		Gson gson = new Gson();
		
		return null;
	}

	@Override
	public String getServerSystemInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getServerSquare(double base) {
		// TODO Auto-generated method stub
		return null;
	}

}
