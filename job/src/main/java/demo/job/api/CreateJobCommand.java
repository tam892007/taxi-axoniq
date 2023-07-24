package demo.job.api;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

//Tag this command to use it as code sample in the documentation
//tag::CreateJobCommand[]
public record CreateJobCommand(
     @TargetAggregateIdentifier String jobId,
     String bookingId,
     String paxId
) {

}
//end::CreateJobCommand[]
