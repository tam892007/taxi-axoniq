package demo.job.command;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import demo.job.api.AssignDriverCommand;
import demo.job.api.CancelJobCommand;
import demo.job.api.CreateJobCommand;
import demo.job.api.DispatchJobCommand;
import demo.job.api.JobAssignedEvent;
import demo.job.api.JobCancelledEvent;
import demo.job.api.JobCreatedEvent;
import demo.job.api.JobDispatchedEvent;
import demo.job.api.JobStatus;

@Profile("command")
@Aggregate(cache = "jobCache")
public class Job {
	@AggregateIdentifier
	private String jobId;
	private String bookingId;
	private String paxId;
	private String dispatchingDriverId;
	private String driverId;

	private JobStatus status;

	public Job() {
		// Required by Axon to construct an empty instance to initiate Event Sourcing.
	}

	// Tag this handler to use it as code sample in the documentation
	// tag::CreateJobCommandHandler[]    
	@CommandHandler
	public Job(CreateJobCommand command) {
		apply(new JobCreatedEvent(command.jobId(), 
				command.bookingId(), 
				command.paxId()));
	}
	// end::CreateJobCommandHandler[]

	// Tag this handler to use it as code sample in the documentation
	// tag::CreateJobCommandHandler[]    
	@CommandHandler
	public void handle(DispatchJobCommand command, DriverService driverService) {
		if (this.status == JobStatus.CANCELLED) {
			return;
		}

		var driverId = driverService.MatchDriver();
		apply(new JobDispatchedEvent(command.jobId(), 
				driverId));
	}
	// end::CreateJobCommandHandler[]

	// Tag this handler to use it as code sample in the documentation
	// tag::AssignDriverCommandHandler[]    
	@CommandHandler
	public void handle(AssignDriverCommand command) {
		apply(new JobAssignedEvent(command.jobId(), 
				this.driverId));
	}
	// end::AssignDriverCommandHandler[]

	// Tag this handler to use it as code sample in the documentation
	// tag::CancelJobCommandHandler[]    
	@CommandHandler
	public void handle(CancelJobCommand command) {
		apply(new JobCancelledEvent(command.jobId(), 
				this.dispatchingDriverId));
	}
	// end::CancelJobCommandHandler[]

	@EventSourcingHandler
	public void on(JobCancelledEvent event) {
		this.status = JobStatus.CANCELLED;
	}

	@EventSourcingHandler
	public void on(JobDispatchedEvent event) {
		this.status = JobStatus.DISPATCHED;
		this.dispatchingDriverId = event.driverId();
	}

	@EventSourcingHandler
	public void on(JobAssignedEvent event) {
		this.driverId = event.driverId();
		this.status = JobStatus.ASSIGNED;
	}

	@EventSourcingHandler
	public void on(JobCreatedEvent event) {
		this.jobId = event.jobId();
		this.bookingId = event.bookingId();
		this.paxId = event.paxId();
		this.status = JobStatus.CREATED;
	}
}
