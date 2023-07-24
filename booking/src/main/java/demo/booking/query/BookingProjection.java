package demo.booking.query;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.Timestamp;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import demo.booking.api.BookingCancelledEvent;
import demo.booking.api.BookingCreatedEvent;
import demo.booking.api.BookingSummary;
import demo.booking.api.FetchBookingQuery;
import demo.booking.api.PaxInformedEvent;

@Profile("query")
@Service
@ProcessingGroup("booking")
public class BookingProjection {
	
	private final Map<String, BookingSummary> bookingSummaryReadModel;
    private final QueryUpdateEmitter queryUpdateEmitter;
    
	public BookingProjection(
            QueryUpdateEmitter queryUpdateEmitter
    ) {
        this.bookingSummaryReadModel = new ConcurrentHashMap<>();
        this.queryUpdateEmitter = queryUpdateEmitter;
    }

    @EventHandler
    public void on(BookingCreatedEvent event, @Timestamp Instant timestamp) {
        /*
         * Update our read model by inserting the new card. This is done so that upcoming regular
         * (non-subscription) queries get correct data.
         */
        BookingSummary summary = BookingSummary.create(event.bookingId(), event.userId(), event.pickUp(), event.dropOff());
        bookingSummaryReadModel.put(event.bookingId(), summary);
        queryUpdateEmitter.emit(FetchBookingQuery.class, query -> true, summary);
    }
    
    @EventHandler
    public void on(BookingCancelledEvent event, @Timestamp Instant timestamp) {
        BookingSummary summary = bookingSummaryReadModel.computeIfPresent(
                event.bookingId(), (id, booking) -> booking.cancel()
        );
        queryUpdateEmitter.emit(FetchBookingQuery.class, query -> true, summary);
    }
    
    @EventHandler
    public void on(PaxInformedEvent event, @Timestamp Instant timestamp) {
        BookingSummary summary = bookingSummaryReadModel.computeIfPresent(
                event.bookingId(), (id, booking) -> booking.accept(event.driverId())
        );
        queryUpdateEmitter.emit(FetchBookingQuery.class, query -> true, summary);
    }
    
    @QueryHandler
    public List<BookingSummary> handle(FetchBookingQuery query) {
        return bookingSummaryReadModel.values()
                                   .stream()
                                   .toList();
    }
}
