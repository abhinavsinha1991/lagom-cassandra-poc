package com.knoldus;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import javax.annotation.concurrent.Immutable;

@Value
@Builder
@Immutable
@JsonDeserialize
@AllArgsConstructor
public final class Folio {
    String shipCode;
    String sailDate;
    String bookingId;
    Integer paxId;
//    String folioTransaction;
   String transactionId;
   String recordType;
   String payerFolioNumber;
   String buyerFolioNumber;
   String buyerPaxId;
   String checkNumber;
   Double transactionAmount;
   String transactionDateTime;
   String transactionDescription;
   String transactionType;
   String departmentId;
   String departmentDescription;
   String sourceRecordTimeStamp;
}
