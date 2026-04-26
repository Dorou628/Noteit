package com.example.noteit.common.event;

import com.example.noteit.common.id.IdGenerator;
import com.example.noteit.common.util.TimeProvider;
import com.example.noteit.relation.event.FollowRelationshipChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingDomainEventPublisher.class);

    private final EventOutboxRepository eventOutboxRepository;
    private final IdGenerator idGenerator;
    private final TimeProvider timeProvider;

    public LoggingDomainEventPublisher(
            EventOutboxRepository eventOutboxRepository,
            IdGenerator idGenerator,
            TimeProvider timeProvider
    ) {
        this.eventOutboxRepository = eventOutboxRepository;
        this.idGenerator = idGenerator;
        this.timeProvider = timeProvider;
    }

    @Override
    public void publish(DomainEvent event) {
        long id = idGenerator.nextId();
        eventOutboxRepository.add(new EventOutboxDO(
                id,
                String.valueOf(id),
                event.eventType(),
                event.aggregateType(),
                event.aggregateId(),
                serialize(event),
                0,
                0,
                null,
                null,
                null,
                null
        ));
        log.info("Saved domain event to outbox: type={}, aggregateType={}, aggregateId={}",
                event.eventType(), event.aggregateType(), event.aggregateId());
    }

    private String serialize(DomainEvent event) {
        if (event instanceof FollowRelationshipChangedEvent followEvent) {
            return """
                    {"eventType":"%s","aggregateType":"%s","aggregateId":"%s","followerUserId":%d,"followeeUserId":%d,"following":%s,"occurredAt":"%s"}
                    """.formatted(
                    event.eventType(),
                    event.aggregateType(),
                    event.aggregateId(),
                    followEvent.followerUserId(),
                    followEvent.followeeUserId(),
                    followEvent.following(),
                    followEvent.occurredAt()
            );
        }
        return """
                {"eventType":"%s","aggregateType":"%s","aggregateId":"%s","occurredAt":"%s"}
                """.formatted(
                event.eventType(),
                event.aggregateType(),
                event.aggregateId(),
                event.occurredAt() == null ? timeProvider.now() : event.occurredAt()
        );
    }
}
