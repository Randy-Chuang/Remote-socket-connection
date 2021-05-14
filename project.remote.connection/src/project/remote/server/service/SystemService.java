package project.remote.server.service;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import project.remote.common.service.ServiceClass.DateInfo;
import project.remote.common.service.ServiceClass.SystemInfo;

public class SystemService implements ISystemService{
	public static final DateFormat forDay = new SimpleDateFormat("dd");
	public static final DateFormat forMonth = new SimpleDateFormat("MM");
	public static final DateFormat forYear = new SimpleDateFormat("yyyy");

	@Override
	public DateInfo getDate() {
		Date date = new Date();
		DateInfo dateInfo = new DateInfo();
		dateInfo.day = forDay.format(date);
		dateInfo.month = forMonth.format(date);
		dateInfo.year = forYear.format(date);
		return dateInfo;
	}

	@Override
	public SystemInfo getSystemInfo() {
		SystemInfo systemInfo = new SystemInfo();
		OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
		systemInfo.name = osBean.getName();
		systemInfo.version = osBean.getVersion();
		systemInfo.architecture = osBean.getArch();
		systemInfo.processors = osBean.getAvailableProcessors();
		systemInfo.load = osBean.getSystemLoadAverage();
		systemInfo.javaVersion = (System.getProperty("java.version") == null) ? 
				"" : System.getProperty("java.version");
		systemInfo.jvmVersion = (System.getProperty("java.vm.version") == null) ? 
				"" : System.getProperty("java.vm.version");
		
		return systemInfo;
	}

	@Override
	public double square(double base) {
		return Math.pow(base, 2);
	}
	
	@Override
	public String stringConcat(String a, String b) {
		return a + b;
	}

}
