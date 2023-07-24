package demo.job.api;

public record DriverJobSummary(String jobId, DriverJobStatus status) {
	public static DriverJobSummary create(String jobId) {
		return new DriverJobSummary(jobId, DriverJobStatus.PENDING);
	}
	public DriverJobSummary accept() {
		return new DriverJobSummary(this.jobId, DriverJobStatus.ACCEPTED);
	}
	public DriverJobSummary cancel() {
		return new DriverJobSummary(this.jobId, DriverJobStatus.CANCELLED);
	}
}
