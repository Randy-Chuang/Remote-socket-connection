package project.remote.common.service;

public class ServiceClass {
	public static class DateInfo{
		public String day, month, year;
	}
	
	public static class SystemInfo{
		public String name, version, architecture, javaVersion, jvmVersion;
		public int processors;
		public double load;	
	}
}
