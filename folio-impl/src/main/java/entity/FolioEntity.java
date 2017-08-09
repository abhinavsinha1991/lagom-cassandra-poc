package entity;

import akka.Done;
import com.lightbend.lagom.javadsl.persistence.PersistentEntity;
import commands.FolioCommand;
import events.FolioEvent;
import states.FolioStates;

import java.time.LocalDateTime;
import java.util.Optional;

public class FolioEntity extends PersistentEntity<FolioCommand, FolioEvent, FolioStates> {
    /**
     *
     * @param snapshotState
     * @return
     */
    @Override
    public Behavior initialBehavior(Optional<FolioStates> snapshotState) {

        // initial behaviour of Folio
        BehaviorBuilder behaviorBuilder = newBehaviorBuilder(
                FolioStates.builder().folio(Optional.empty())
                        .timestamp(LocalDateTime.now().toString()).build()
        );

        behaviorBuilder.setCommandHandler(FolioCommand.CreateFolio.class, (cmd, ctx) ->
                ctx.thenPersist(FolioEvent.FolioCreated.builder().folio(cmd.getFolio())
                        .entityId(entityId()).build(), evt -> ctx.reply(Done.getInstance()))
        );

        behaviorBuilder.setEventHandler(FolioEvent.FolioCreated.class, evt ->
                FolioStates.builder().folio(Optional.of(evt.getFolio()))
                        .timestamp(LocalDateTime.now().toString()).build()
        );

        behaviorBuilder.setCommandHandler(FolioCommand.UpdateFolio.class, (cmd, ctx) ->
                ctx.thenPersist(FolioEvent.FolioUpdated.builder().folio(cmd.getFolio()).entityId(entityId()).build()
                        , evt -> ctx.reply(Done.getInstance()))
        );

        behaviorBuilder.setEventHandler(FolioEvent.FolioUpdated.class, evt ->
                FolioStates.builder().folio(Optional.of(evt.getFolio()))
                        .timestamp(LocalDateTime.now().toString()).build()
        );

        behaviorBuilder.setCommandHandler(FolioCommand.DeleteFolio.class, (cmd, ctx) ->
                ctx.thenPersist(FolioEvent.FolioDeleted.builder().folio(cmd.getFolio()).entityId(entityId()).build(),
                        evt -> ctx.reply(Done.getInstance()))
        );

        behaviorBuilder.setEventHandler(FolioEvent.FolioDeleted.class, evt ->
                FolioStates.builder().folio(Optional.empty())
                        .timestamp(LocalDateTime.now().toString()).build()
        );

        behaviorBuilder.setReadOnlyCommandHandler(FolioCommand.FolioCurrentState.class, (cmd, ctx) ->
                ctx.reply(state().getFolio())
        );

        return behaviorBuilder.build();
    }
}
