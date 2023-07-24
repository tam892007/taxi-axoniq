package demo.booking.rest;

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

import demo.booking.api.BookARideCommand;
import demo.booking.api.BookingSummary;
import demo.booking.api.CancelBookingCommand;
import demo.booking.api.FetchBookingQuery;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Profile("gui")
@RequestMapping("/booking")
public class BookingController {
	private final CommandGateway commandGateway;
	private final QueryGateway queryGateway;

	public BookingController(CommandGateway commandGateway, QueryGateway queryGateway) {
		this.commandGateway = commandGateway;
		this.queryGateway = queryGateway;
	}

	@GetMapping(path = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<BookingSummary> subscribe() {
		return bookingSubscription();
	}

	@PostMapping("book/user/{userId}/pick-up/{pickUp}/drop-off/{dropOff}")
	public Mono<Result> issue(
			@PathVariable String userId,
			@PathVariable String pickUp,
			@PathVariable String dropOff) {
		var command = new BookARideCommand(userId, pickUp, dropOff);
		return Mono.fromFuture(commandGateway.send(command))
				.then(Mono.just(Result.ok()))
				.onErrorResume(e -> Mono.just(Result.Error(userId, e.getMessage())))
				.timeout(Duration.ofSeconds(5L));
	}

	@PostMapping("cancel/user/{userId}/booking/{bookId}")
	public Mono<Result> issue(
			@PathVariable String userId,
			@PathVariable String bookId
			) {
		var command = new CancelBookingCommand(userId, bookId);
		return Mono.fromFuture(commandGateway.send(command))
				.then(Mono.just(Result.ok()))
				.onErrorResume(e -> Mono.just(Result.Error(userId, e.getMessage())))
				.timeout(Duration.ofSeconds(5L));
	}
	
	private Flux<BookingSummary> bookingSubscription() {
        var query = new FetchBookingQuery();
        SubscriptionQueryResult<List<BookingSummary>, BookingSummary> result = queryGateway.subscriptionQuery(
                query,
                ResponseTypes.multipleInstancesOf(BookingSummary.class),
                ResponseTypes.instanceOf(BookingSummary.class));
        return result.initialResult()
                     .flatMapMany(Flux::fromIterable)
                     .concatWith(result.updates())
                     .doFinally(signal -> result.close());
    }
}
