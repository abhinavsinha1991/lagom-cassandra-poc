package com.knoldus;

import akka.Done;
import akka.NotUsed;
import com.lightbend.lagom.javadsl.api.Descriptor;
import com.lightbend.lagom.javadsl.api.Service;
import com.lightbend.lagom.javadsl.api.ServiceCall;

import java.util.Optional;

import static com.lightbend.lagom.javadsl.api.Service.named;
import static com.lightbend.lagom.javadsl.api.Service.restCall;
import static com.lightbend.lagom.javadsl.api.transport.Method.*;

public interface FolioService extends Service {

    ServiceCall<NotUsed, Optional<Folio>> folio(Optional<String> shipCode, Optional<String> sailDate,
                                                Optional<Integer> paxId, Optional<Integer> chargeId );
    ServiceCall<Folio, Done> newFolio();

    ServiceCall<Folio, Done> updateFolio(Optional<String> shipCode,
                                         Optional<String> sailDate, Optional<Integer> payerPaxId, Optional<Integer> chargeId);

    ServiceCall<NotUsed, Done> deleteFolio(Optional<String> shipCode,
                                           Optional<String> sailDate, Optional<Integer> paxId, Optional<Integer> chargeId);
/*
10.16.4.106:8042
10.16.7.47:8042
10.16.6.162:8042
 */
    @Override
    default Descriptor descriptor() {

        return named("folio").withCalls(
                restCall(GET, "/api/folio/:shipCode?sailDate&payerPaxId&chargeId", this::folio),
                restCall(POST, "/api/new-folio", this::newFolio),
                restCall(PUT, "/api/update-folio/:shipCode?sailDate&payerPaxId&chargeId", this::updateFolio),
                restCall(DELETE, "/api/delete-folio/:shipCode?sailDate&payerPaxId&chargeId", this::deleteFolio)
        ).withAutoAcl(true);
    }
}
