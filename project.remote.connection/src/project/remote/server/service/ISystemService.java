package project.remote.server.service;

import project.remote.common.service.ServiceClass.DateInfo;
import project.remote.common.service.ServiceClass.SystemInfo;

public interface ISystemService {	
	public DateInfo getDate();
	public SystemInfo getSystemInfo();
	public double square(double base);
}
