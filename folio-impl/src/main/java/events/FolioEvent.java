package events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.knoldus.Folio;
import com.lightbend.lagom.javadsl.persistence.AggregateEvent;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTagger;
import com.lightbend.lagom.serialization.CompressedJsonable;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

/**
 * Created by knoldus on 30/1/17.
 */
public interface FolioEvent extends Jsonable, AggregateEvent<FolioEvent> {

    @Override
    default AggregateEventTagger<FolioEvent> aggregateTag() {
        return FolioEventTag.INSTANCE;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class FolioCreated implements FolioEvent, CompressedJsonable {
        Folio folio;
        String entityId;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class FolioUpdated implements FolioEvent, CompressedJsonable {
        Folio folio;
        String entityId;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class FolioDeleted implements FolioEvent, CompressedJsonable {
        Folio folio;
        String entityId;
    }
}
