package com.knoldus;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import javax.annotation.concurrent.Immutable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Value
@Builder
@Immutable
@JsonDeserialize
@AllArgsConstructor
public final class Folio {
    String shipCode;
    String sailDate;
    Integer payerPaxId;
    String bookingId;
     int chargeId;
     int buyerPaxId;
//   UUID transactionId;
//   String recordType;
   String payerFolioNumber;
   String buyerFolioNumber;
   int itemId;
   String checkNumber;
   BigDecimal transactionAmount;
   Date transactionDateTime;
   String transactionDescription;
   String chargeType;
   String departmentId;
   String departmentDescription;
   Date sourceRecordTimeStamp;
}
