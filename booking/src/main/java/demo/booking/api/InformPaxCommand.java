package demo.booking.api;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

public record InformPaxCommand(
        @TargetAggregateIdentifier String userId,
        String bookingId,
        String jobId,
        String driverId
) {

}
