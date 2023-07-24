package demo.job.api;

public record JobDispatchedEvent (String jobId,
		String driverId) {
		
}