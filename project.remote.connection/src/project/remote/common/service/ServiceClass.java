package project.remote.common.service;

import com.google.gson.annotations.SerializedName;

public class ServiceClass {
	public static class DateInfo{
		public String day, month, year;
		
		public void printContent() {
			System.out.println("--DateInfo--");
			System.out.println("day: " + day);
			System.out.println("month: " + month);
			System.out.println("year: " + year);
		}
	}
	
	public static class SystemInfo{
		public String name, version, architecture;
		public int processors;
		public double load;	
		@SerializedName("Java version")
		public String javaVersion;
		@SerializedName("JVM version")
		public String jvmVersion;
		
		public void printContent() {
			System.out.println("--SystemInfo--");
			System.out.println("name: " + name);
			System.out.println("version: " + version);
			System.out.println("architecture: " + architecture);
			System.out.println("processors: " + processors);
			System.out.println("load: " + load);
			System.out.println("javaVersion: " + javaVersion);
			System.out.println("jvmVersion: " + jvmVersion);
		}
	}
}
