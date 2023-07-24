package demo.job.api;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

//Tag this command to use it as code sample in the documentation
//tag::DispatchJobCommand[]
public record DispatchJobCommand(
   @TargetAggregateIdentifier String jobId
) {

}
//end::DispatchJobCommand[]