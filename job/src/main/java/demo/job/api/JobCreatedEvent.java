package demo.job.api;

public record JobCreatedEvent (String jobId,
	String bookingId,
	String paxId) {
	
}
