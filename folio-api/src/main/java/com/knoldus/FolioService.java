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

    ServiceCall<NotUsed, Optional<Folio>> folio(Optional<String> shipCode,
                                                Optional<String> sailDate,Optional<String> bookingId,Optional<Integer> paxId );
    ServiceCall<Folio, Done> newFolio();

    ServiceCall<Folio, Done> updateFolio(Optional<String> shipCode,
                                         Optional<String> sailDate,Optional<String> bookingId,Optional<Integer> paxId);

    ServiceCall<NotUsed, Done> deleteFolio(Optional<String> shipCode,
                                           Optional<String> sailDate,Optional<String> bookingId,Optional<Integer> paxId);

    @Override
    default Descriptor descriptor() {

        return named("folio").withCalls(
                restCall(GET, "/api/folio/:shipCode?sailDate&bookingId&paxId", this::folio),
                restCall(POST, "/api/new-folio", this::newFolio),
                restCall(PUT, "/api/update-folio/:shipCode?sailDate&bookingId&paxId", this::updateFolio),
                restCall(DELETE, "/api/delete-folio/:shipCode?sailDate&bookingId&paxId", this::deleteFolio)
        ).withAutoAcl(true);
    }
}
