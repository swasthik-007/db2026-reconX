package com.dbtraining.reconx.repository.entity;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="dlq_messages") public class DlqMessage {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(name="event_id", unique=true, nullable=false) private String eventId;
 @Column(name="trade_ref") private String tradeRef; @Column(name="original_topic") private String originalTopic;
 @Column(name="partition_no") private int partitionNo; @Column(name="offset_no") private long offsetNo;
 @Lob private String payload; @Lob private String reason; @Column(name="first_seen") private Instant firstSeen;
 protected DlqMessage() {} public DlqMessage(String eventId,String tradeRef,String originalTopic,int partitionNo,long offsetNo,String payload,String reason,Instant firstSeen){this.eventId=eventId;this.tradeRef=tradeRef;this.originalTopic=originalTopic;this.partitionNo=partitionNo;this.offsetNo=offsetNo;this.payload=payload;this.reason=reason;this.firstSeen=firstSeen;}
 public String getEventId(){return eventId;} public String getTradeRef(){return tradeRef;} public String getOriginalTopic(){return originalTopic;} public String getPayload(){return payload;}
}
