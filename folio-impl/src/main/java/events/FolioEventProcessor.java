package events;

import akka.Done;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import events.FolioEvent.*;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;


public class FolioEventProcessor extends ReadSideProcessor<FolioEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FolioEventProcessor.class);

    private final CassandraSession session;
    private final CassandraReadSide readSide;

    private PreparedStatement writeFolios;
    private PreparedStatement deleteFolios;

    @Inject
    public FolioEventProcessor(final CassandraSession session, final CassandraReadSide readSide) {
        this.session = session;
        this.readSide = readSide;
    }

    @Override
    public PSequence<AggregateEventTag<FolioEvent>> aggregateTags() {
        LOGGER.info(" aggregateTags method ... ");
        return TreePVector.singleton(FolioEventTag.INSTANCE);
    }

    @Override
    public ReadSideHandler<FolioEvent> buildHandler() {
        LOGGER.info(" buildHandler method ... ");
        return readSide.<FolioEvent>builder("Folios_offset")
                .setGlobalPrepare(this::createTable)
                .setPrepare(evtTag -> prepareWriteFolio()
                        .thenCombine(prepareDeleteFolio(), (d1, d2) -> Done.getInstance())
                )
                .setEventHandler(FolioCreated.class, this::processPostAdded)
                .setEventHandler(FolioUpdated.class, this::processPostUpdated)
                .setEventHandler(FolioDeleted.class, this::processPostDeleted)
                .build();
    }

    // Execute only once while application is start
    private CompletionStage<Done> createTable() {
        LOGGER.info(" createTable method ... ");
        return session.executeCreateTable(
                "CREATE TABLE IF NOT EXISTS Folios ( " +
                        "shipCode TEXT, sailDate TEXT, bookingId TEXT , paxId INT ," +
                        " folioTransaction TEXT, createdOn TIMESTAMP, PRIMARY KEY((shipCode,sailDate),bookingId,paxId))"
        );
    }

    /*
    * START: Prepare statement for insert Folio values into Folios table.
    * This is just creation of prepared statement, we will map this statement with our event
    */

    private CompletionStage<Done> prepareWriteFolio() {
        LOGGER.info(" prepareWriteFolio method ... ");
        return session.prepare(
                "INSERT INTO Folios (shipCode, sailDate, bookingId, paxId, folioTransaction, createdOn) VALUES (?, ?, ?, ?, ?, ?)"
        ).thenApply(ps -> {
            setWriteFolios(ps);
            return Done.getInstance();
        });
    }

    private void setWriteFolios(PreparedStatement statement) {
        this.writeFolios = statement;
    }

    // Bind prepare statement while FolioCreate event is executed

    private CompletionStage<List<BoundStatement>> processPostAdded(FolioCreated event) {
        BoundStatement bindWriteFolio = writeFolios.bind();
        bindWriteFolio.setString("shipCode", event.getFolio().getShipCode());
        bindWriteFolio.setString("sailDate", event.getFolio().getSailDate());
        bindWriteFolio.setString("bookingId", event.getFolio().getBookingId());
        bindWriteFolio.setInt("paxId", event.getFolio().getPaxId());
        bindWriteFolio.setString("folioTransaction", event.getFolio().getFolioTransaction());
        bindWriteFolio.setTimestamp("createdOn",Date.valueOf(LocalDateTime.now().toLocalDate()));
        return CassandraReadSide.completedStatements(Arrays.asList(bindWriteFolio));
    }
    /* ******************* END ****************************/

    /* START: Prepare statement for update the data in Folios table.
    * This is just creation of prepared statement, we will map this statement with our event
    */

    private CompletionStage<List<BoundStatement>> processPostUpdated(FolioUpdated event) {
        BoundStatement bindWriteFolio = writeFolios.bind();
        bindWriteFolio.setString("shipCode", event.getFolio().getShipCode());
        bindWriteFolio.setString("sailDate", event.getFolio().getSailDate());
        bindWriteFolio.setString("bookingId", event.getFolio().getBookingId());
        bindWriteFolio.setInt("paxId", event.getFolio().getPaxId());
        bindWriteFolio.setString("folioTransaction", event.getFolio().getFolioTransaction());
        bindWriteFolio.setTimestamp("createdOn",Date.valueOf(LocalDateTime.now().toLocalDate()));
        return CassandraReadSide.completedStatements(Arrays.asList(bindWriteFolio));
    }
    /* ******************* END ****************************/

    /* START: Prepare statement for delete the the Folio from table.
    * This is just creation of prepared statement, we will map this statement with our event
    */

    private CompletionStage<Done> prepareDeleteFolio() {
        return session.prepare(
                "DELETE FROM Folios WHERE shipCode = ? AND sailDate = ? AND bookingId = ? AND paxId = ? "
        ).thenApply(ps -> {
            setDeleteFolios(ps);
            return Done.getInstance();
        });
    }

    private void setDeleteFolios(PreparedStatement deleteFolios) {
        this.deleteFolios = deleteFolios;
    }

    private CompletionStage<List<BoundStatement>> processPostDeleted(FolioDeleted event) {
        BoundStatement bindWriteFolio = deleteFolios.bind();
        bindWriteFolio.setString("shipCode", event.getFolio().getShipCode());
        bindWriteFolio.setString("sailDate", event.getFolio().getSailDate());
        bindWriteFolio.setString("bookingId", event.getFolio().getBookingId());
        bindWriteFolio.setInt("paxId", event.getFolio().getPaxId());
        return CassandraReadSide.completedStatements(Arrays.asList(bindWriteFolio));
    }
    /* ******************* END ****************************/
}
