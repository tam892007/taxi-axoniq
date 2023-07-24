package demo.job.api;

import org.axonframework.modelling.command.TargetAggregateIdentifier;

//Tag this command to use it as code sample in the documentation
//tag::CancelJobCommand[]
public record CancelJobCommand (
		@TargetAggregateIdentifier String jobId
) {
	
}
//end::CancelJobCommand[]
