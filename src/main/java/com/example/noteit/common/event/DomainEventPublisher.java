package com.example.noteit.common.event;

public interface DomainEventPublisher {

    void publish(DomainEvent event);
}
