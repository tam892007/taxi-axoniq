package demo.booking.command;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateCreationPolicy;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.CreationPolicy;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.context.annotation.Profile;

import demo.booking.api.BookARideCommand;
import demo.booking.api.BookingCancelledEvent;
import demo.booking.api.BookingCreatedEvent;
import demo.booking.api.BookingInfo;
import demo.booking.api.CancelBookingCommand;
import demo.booking.api.InformPaxCommand;
import demo.booking.api.PaxInformedEvent;

@Profile("command")
@Aggregate(cache = "userBookingCache")
public class UserBooking {

	@AggregateIdentifier
	private String userId;
	private Set<BookingInfo> activeBookings = new HashSet<BookingInfo>();

	public UserBooking() {
		// Required by Axon to construct an empty instance to initiate Event Sourcing.
	}

	// Tag this handler to use it as code sample in the documentation
	// tag::BookARideCommandHandler[]    
	@CommandHandler
	@CreationPolicy(AggregateCreationPolicy.CREATE_IF_MISSING)
	public void handle(BookARideCommand command) {
		if (this.HasActiveBooking()) {
			throw new IllegalStateException("User has other active bookings");
		}
		apply(new BookingCreatedEvent(command.userId(), 
				UUID.randomUUID().toString(), 
				command.pickUp(), 
				command.dropOff()));
	}
	// end::BookARideCommandHandler[]

	// Tag this handler to use it as code sample in the documentation
	// tag::CancelBookingCommandHandler[]    
	@CommandHandler
	public void handle(CancelBookingCommand command) {
		if (!this.CanCancel(command.bookingId())) {
			throw new IllegalStateException("Booking is not active");
		}
		apply(new BookingCancelledEvent(command.userId(), 
				command.bookingId()));
	}
	// end::CancelBookingCommandHandler[]
	
	@CommandHandler
	public void handle(InformPaxCommand command) {
		apply(new PaxInformedEvent(command.bookingId(), command.jobId(), command.driverId()));
	}

	@EventSourcingHandler
	public void on(BookingCreatedEvent event) {
		this.userId = event.userId();
		this.activeBookings.add(new BookingInfo(event.bookingId(), event.pickUp(), event.dropOff()));
	}

	@EventSourcingHandler
	public void on(BookingCancelledEvent event) {
		this.activeBookings.removeIf(x -> x.bookingId().equalsIgnoreCase(event.bookingId()));
	}

	@EventSourcingHandler
	public void on(PaxInformedEvent event) {
		
	}
	
	private Boolean HasActiveBooking() {
		return this.activeBookings != null && this.activeBookings.size() > 0;
	}

	private Boolean CanCancel(String bookingId) {
		return this.activeBookings.stream().filter(x -> x.bookingId().equalsIgnoreCase(bookingId)).findAny().isPresent();
	}
}

