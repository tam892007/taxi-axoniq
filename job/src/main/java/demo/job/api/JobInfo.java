package demo.job.api;

public class JobInfo {
	private String jobId;
	private DriverJobStatus status;
	public JobInfo (String jobId, DriverJobStatus status)
	{
		this.jobId = jobId;
		this.status = status;
	}

	public String getJobId() {
		return jobId;
	}

	public DriverJobStatus getStatus() {
		return status;
	}
	
	public void Accept() {
		this.status = DriverJobStatus.ACCEPTED;
	}
}
