package demo.job.command;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import java.util.HashSet;
import java.util.Set;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateCreationPolicy;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.CreationPolicy;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.context.annotation.Profile;

import demo.job.api.AcceptJobCommand;
import demo.job.api.CreateDriverJobCommand;
import demo.job.api.DriverJobCreatedEvent;
import demo.job.api.DriverJobRemovedEvent;
import demo.job.api.DriverJobStatus;
import demo.job.api.JobAcceptedEvent;
import demo.job.api.JobInfo;
import demo.job.api.RemoveDriverJobCommand;


@Profile("command")
@Aggregate(cache = "driverJobCache")
public class DriverJob {
	@AggregateIdentifier
	private String driverId;
	private Set<JobInfo> activeJobs = new HashSet<JobInfo>();

	public DriverJob() {
		// Required by Axon to construct an empty instance to initiate Event Sourcing.
	}

	// Tag this handler to use it as code sample in the documentation
	// tag::CreateDriverJobCommandHandler[]    
	@CommandHandler
	@CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
	public void handle(CreateDriverJobCommand command) {
		if (this.IsBusy())
		{
			throw new IllegalStateException("User has other active bookings");
		}
		apply(new DriverJobCreatedEvent(command.jobId(), 
				command.driverId()));
	}
	// end::CreateDriverJobCommandHandler[]

	// Tag this handler to use it as code sample in the documentation
	// tag::AcceptJobCommandHandler[]    
	@CommandHandler
	public void handle(AcceptJobCommand command) {
		if (!this.CanAccept(command.jobId()) || this.IsBusy())
		{
			throw new IllegalStateException("Driver has already has jobs");
		}
		apply(new JobAcceptedEvent(command.jobId(), 
				this.driverId));
	}
	// end::AcceptJobCommandHandler[]

	// Tag this handler to use it as code sample in the documentation
	// tag::RemoveDriverJobCommandHandler[]    
	@CommandHandler
	public void handle(RemoveDriverJobCommand command) {
		apply(new DriverJobRemovedEvent(command.jobId(), 
				this.driverId));
	}
	// end::RemoveDriverJobCommandHandler[]

	@EventSourcingHandler
	public void on(DriverJobCreatedEvent event) {
		this.driverId = event.driverId();
		this.activeJobs.add(new JobInfo(event.jobId(), DriverJobStatus.PENDING));
	}

	@EventSourcingHandler
	public void on(DriverJobRemovedEvent event) {
		this.activeJobs.removeIf(x -> x.getJobId().equalsIgnoreCase(event.jobId()));
	}

	@EventSourcingHandler
	public void on(JobAcceptedEvent event) {
		var jobInfo = this.activeJobs.stream().filter(x -> x.getJobId().equalsIgnoreCase(event.jobId())).findFirst();
		if (jobInfo.isPresent()) {
			jobInfo.get().Accept();
		}
	}

	private Boolean CanAccept(String jobId) {
		return this.activeJobs.stream()
				.filter(x -> x.getJobId().equalsIgnoreCase(jobId) && x.getStatus() == DriverJobStatus.PENDING)
				.count() > 0;
	}

	private Boolean IsBusy() {
		return this.activeJobs != null && 
				this.activeJobs.stream().filter(x -> x.getStatus() == DriverJobStatus.ACCEPTED).count() > 0;
	}
}
