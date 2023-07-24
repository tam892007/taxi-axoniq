package demo.booking.api;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

//Tag this command to use it as code sample in the documentation
//tag::CancelBookingCommand[]
public record CancelBookingCommand(
     @TargetAggregateIdentifier String userId,
     String bookingId
) {

}
//end::CancelBookingCommand[]