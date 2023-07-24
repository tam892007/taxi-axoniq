package demo.booking.api;

public record BookingSummary(String bookingId, String userId, String pickUp, String dropOff, BookingStatus status, String driverId) {
	public static BookingSummary create(String bookingId, String userId, String pickUp, String dropOff) {
        return new BookingSummary(bookingId, userId, pickUp, dropOff, BookingStatus.ACTIVE, "");
    }
	public BookingSummary cancel() {
        return new BookingSummary(this.bookingId, this.userId, pickUp, dropOff, BookingStatus.CANCELLED, this.driverId);
    }
	public BookingSummary accept(String driverId) {
        return new BookingSummary(this.bookingId, this.userId, pickUp, dropOff, this.status, driverId);
    }
}
