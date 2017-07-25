package commands;

import akka.Done;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.knoldus.Folio;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import com.lightbend.lagom.serialization.Jsonable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

import java.util.Optional;

/**
 * Created by knoldus on 30/1/17.
 */
public interface FolioCommand extends Jsonable {

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class CreateFolio implements FolioCommand, PersistentEntity.ReplyType<Done> {
        Folio folio;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class UpdateFolio implements FolioCommand, PersistentEntity.ReplyType<Done> {
        Folio folio;
    }

    @Value
    @Builder
    @JsonDeserialize
    @AllArgsConstructor
    final class DeleteFolio implements FolioCommand, PersistentEntity.ReplyType<Done> {
        Folio folio;
    }

    @JsonDeserialize
    final class FolioCurrentState implements FolioCommand, PersistentEntity.ReplyType<Optional<Folio>> {}
}
