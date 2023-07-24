package demo.job.rest;

import java.time.Duration;
import java.util.List;

import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import demo.job.api.AcceptJobCommand;
import demo.job.api.DriverJobSummary;
import demo.job.api.FetchDriverJobQuery;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Profile("gui")
@RequestMapping("/job")
public class JobController {
	private final CommandGateway commandGateway;
	private final QueryGateway queryGateway;

	public JobController(CommandGateway commandGateway, QueryGateway queryGateway) {
		this.commandGateway = commandGateway;
		this.queryGateway = queryGateway;
	}
	
	@GetMapping(path = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<DriverJobSummary> subscribe() {
		return jobSubscription();
	}
	
	@PostMapping("accept/driver/{driverId}/job/{jobId}")
	public Mono<Result> issue(
			@PathVariable String driverId,
			@PathVariable String jobId
			) {
		var command = new AcceptJobCommand(jobId, driverId);
		return Mono.fromFuture(commandGateway.send(command))
				.then(Mono.just(Result.ok()))
				.onErrorResume(e -> Mono.just(Result.Error(jobId, e.getMessage())))
				.timeout(Duration.ofSeconds(5L));
	}
	
	private Flux<DriverJobSummary> jobSubscription() {
        var query = new FetchDriverJobQuery();
        SubscriptionQueryResult<List<DriverJobSummary>, DriverJobSummary> result = queryGateway.subscriptionQuery(
                query,
                ResponseTypes.multipleInstancesOf(DriverJobSummary.class),
                ResponseTypes.instanceOf(DriverJobSummary.class));
        return result.initialResult()
                     .flatMapMany(Flux::fromIterable)
                     .concatWith(result.updates())
                     .doFinally(signal -> result.close());
    }
}
