package demo.job.command;

import org.springframework.stereotype.Component;

@Component("driverService")
public class DriverService {
	public DriverService() {
		
	}
	public String MatchDriver() {
		return "driver-1";
	}
}
