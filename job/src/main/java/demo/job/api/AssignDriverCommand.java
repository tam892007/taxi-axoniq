package demo.job.api;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

//Tag this command to use it as code sample in the documentation
//tag::AssignDriverCommand[]
public record AssignDriverCommand(
		@TargetAggregateIdentifier String jobId,
		String driverId
) {
	
}
//end::AssignDriverCommand[]