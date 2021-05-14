package project.remote.common.service;

import com.google.gson.annotations.SerializedName;

public class ServiceClass {
	public static class DateInfo{
		public String day, month, year;
	}
	
	public static class SystemInfo{
		public String name, version, architecture;
		public int processors;
		public double load;	
		@SerializedName("Java version")
		public String javaVersion;
		@SerializedName("JVM version")
		public String jvmVersion;
	}
}
