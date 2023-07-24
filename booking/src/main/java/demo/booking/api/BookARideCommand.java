package demo.booking.api;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

// Tag this command to use it as code sample in the documentation
// tag::BookARideCommand[]
public record BookARideCommand(
        @TargetAggregateIdentifier String userId,
        String pickUp,
        String dropOff
) {

}
// end::BookARideCommand[]
