package project.remote.server.service;

public interface ISystemService {	
	public DateInfo getDate();
	public SystemInfo getSystemInfo();
	public double square(double base);
	
	public static class DateInfo{
		public String day, month, year;
	}
	
	public static class SystemInfo{
		public String name, version, architecture, javaVersion, jvmVersion;
		public int processors;
		public double load;
	}
}
