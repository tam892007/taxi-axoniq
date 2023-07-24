package demo.job.api;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

//Tag this command to use it as code sample in the documentation
//tag::CreateJobCommand[]
public record CreateDriverJobCommand(
   String jobId,
   @TargetAggregateIdentifier String driverId
) {

}
//end::CreateJobCommand[]