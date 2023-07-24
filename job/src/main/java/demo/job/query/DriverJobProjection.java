package demo.job.query;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.queryhandling.QueryHandler;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import demo.job.api.DriverJobCreatedEvent;
import demo.job.api.DriverJobSummary;
import demo.job.api.FetchDriverJobQuery;
import demo.job.api.JobAcceptedEvent;
import demo.job.api.JobCancelledEvent;

@Profile("query")
@Service
@ProcessingGroup("job")
public class DriverJobProjection {
	private final Map<String, DriverJobSummary> drvJobsSummaryReadModel;
    private final QueryUpdateEmitter queryUpdateEmitter;
    
	public DriverJobProjection(
            QueryUpdateEmitter queryUpdateEmitter
    ) {
        this.drvJobsSummaryReadModel = new ConcurrentHashMap<>();
        this.queryUpdateEmitter = queryUpdateEmitter;
    }
	
	@EventHandler
    public void on(DriverJobCreatedEvent event) {
        /*
         * Update our read model by inserting the new card. This is done so that upcoming regular
         * (non-subscription) queries get correct data.
         */
        DriverJobSummary summary = DriverJobSummary.create(event.jobId());
        drvJobsSummaryReadModel.put(event.jobId(), summary);
        queryUpdateEmitter.emit(FetchDriverJobQuery.class, query -> true, summary);
    }
    
    @EventHandler
    public void on(JobAcceptedEvent event) {
    	DriverJobSummary summary = drvJobsSummaryReadModel.computeIfPresent(
                event.jobId(), (id, job) -> job.accept()
        );
        queryUpdateEmitter.emit(FetchDriverJobQuery.class, query -> true, summary);
    }
    
    @EventHandler
    public void on(JobCancelledEvent event) {
    	DriverJobSummary summary = drvJobsSummaryReadModel.computeIfPresent(
                event.jobId(), (id, job) -> job.cancel()
        );
        queryUpdateEmitter.emit(FetchDriverJobQuery.class, query -> true, summary);
    }
    
    @QueryHandler
    public List<DriverJobSummary> handle(FetchDriverJobQuery query) {
        return drvJobsSummaryReadModel.values()
                                   .stream()
                                   .toList();
    }
}
