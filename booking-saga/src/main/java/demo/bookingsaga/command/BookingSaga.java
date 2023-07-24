package demo.bookingsaga.command;

import java.util.UUID;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import demo.booking.api.BookingCancelledEvent;
import demo.booking.api.BookingCreatedEvent;
import demo.booking.api.InformPaxCommand;
import demo.job.api.AssignDriverCommand;
import demo.job.api.CancelJobCommand;
import demo.job.api.CreateDriverJobCommand;
import demo.job.api.CreateJobCommand;
import demo.job.api.DispatchJobCommand;
import demo.job.api.JobAcceptedEvent;
import demo.job.api.JobCreatedEvent;
import demo.job.api.JobDispatchedEvent;
import demo.job.api.RemoveDriverJobCommand;

@Profile("saga")
@Saga
public class BookingSaga {
	@Autowired
	private transient CommandGateway commandGateway;
	private String paxId;
	private String bookingId;
	private String jobId;
	private String dispatchingDriverId;
	private String driverId;
	private BookingSagaState state;

	@StartSaga
	@SagaEventHandler(associationProperty = "bookingId")
	public void handle(BookingCreatedEvent event) {
		this.state = BookingSagaState.BOOKING_CREATED;
		this.paxId = event.userId();
		this.bookingId = event.bookingId();
		this.jobId = UUID.randomUUID().toString();
		SagaLifecycle.associateWith("jobId", this.jobId);
		this.commandGateway.send(new CreateJobCommand(this.jobId, event.bookingId(), event.userId()));
	}

	@SagaEventHandler(associationProperty = "jobId")
	public void handle(JobCreatedEvent event) {
		if (this.state == BookingSagaState.BOOKING_CANCELLED) {
			return;
		}
		this.state = BookingSagaState.JOB_CREATED;
		this.commandGateway.send(new DispatchJobCommand(event.jobId()));
	}

	@SagaEventHandler(associationProperty = "jobId")
	public void handle(JobDispatchedEvent event) {
		if (this.state == BookingSagaState.BOOKING_CANCELLED) {
			return;
		}
		this.state = BookingSagaState.JOB_DISPATCHED;
		this.dispatchingDriverId = event.driverId();
		this.commandGateway.send(new CreateDriverJobCommand(event.jobId(), event.driverId()));
	}

	@SagaEventHandler(associationProperty = "jobId")
	public void handle(JobAcceptedEvent event) {
		if (this.state == BookingSagaState.BOOKING_CANCELLED) {
			return;
		}

		this.state = BookingSagaState.JOB_ACCEPTED;
		this.driverId = event.driverId();
		this.commandGateway.send(new AssignDriverCommand(event.jobId(), event.driverId()));
		this.commandGateway.send(new InformPaxCommand(this.paxId, this.bookingId, this.jobId, this.driverId));
	}

	@SagaEventHandler(associationProperty = "bookingId")
	public void handle(BookingCancelledEvent event) {
		var prevState = this.state;
		this.state = BookingSagaState.BOOKING_CANCELLED;
		this.commandGateway.send(new CancelJobCommand(this.jobId));
		if (prevState == BookingSagaState.JOB_DISPATCHED) {
			this.commandGateway.send(new RemoveDriverJobCommand(this.jobId, this.dispatchingDriverId));
		}

		if (prevState == BookingSagaState.JOB_ACCEPTED) {
			this.commandGateway.send(new RemoveDriverJobCommand(this.jobId, this.driverId));
		}
	}
}
