package com.dbtraining.reconx.repository.entity;

import jakarta.persistence.*;
import java.time.Instant;


@Entity
@Table(name = "audit_log")
public class AuditLogEntry {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(
            name = "event_id",
            nullable = false,
            unique = true,
            length = 36
    )
    private String eventId;


    @Column(
            name = "trade_ref",
            nullable = false,
            length = 30
    )
    private String tradeRef;


    @Column(
            name = "event_type",
            nullable = false,
            length = 30
    )
    private String eventType;


    @Column(
            name = "event_timestamp",
            nullable = false
    )
    private Instant eventTimestamp;


    @Column(
            name = "actor",
            length = 100
    )
    private String actor;



    /*
     * Large JSON snapshot before trade change.
     * Matches Liquibase TEXT column.
     */
     @Lob
   @Column(name = "before_state")
private String beforeState;




    /*
     * Large JSON snapshot after trade change.
     * Matches Liquibase TEXT column.
     */
     @Lob
    @Column(name = "after_state")
private String afterState;




    public AuditLogEntry() {
    }



    public AuditLogEntry(
            String eventId,
            String tradeRef,
            String eventType,
            Instant eventTimestamp,
            String actor,
            String beforeState,
            String afterState
    ) {

        this.eventId = eventId;
        this.tradeRef = tradeRef;
        this.eventType = eventType;
        this.eventTimestamp = eventTimestamp;
        this.actor = actor;
        this.beforeState = beforeState;
        this.afterState = afterState;

    }



    public Long getId() {
        return id;
    }


    public String getEventId() {
        return eventId;
    }


    public String getTradeRef() {
        return tradeRef;
    }


    public String getEventType() {
        return eventType;
    }


    public Instant getEventTimestamp() {
        return eventTimestamp;
    }


    public String getActor() {
        return actor;
    }


    public String getBeforeState() {
        return beforeState;
    }


    public String getAfterState() {
        return afterState;
    }

}