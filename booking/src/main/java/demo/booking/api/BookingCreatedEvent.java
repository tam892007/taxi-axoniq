package demo.booking.api;

public record BookingCreatedEvent (String userId,
	String bookingId,
	String pickUp,
	String dropOff) {
	
}
