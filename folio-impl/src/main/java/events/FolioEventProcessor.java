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
import java.math.BigDecimal;
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
                        "Ship_Code TEXT, Sail_Date TEXT, Booking_ID TEXT , Payer_PaxID INT ," +
                        "Transaction_ID TIMEUUID, Record_Type TEXT, Payer_FolioNumber TEXT, Buyer_FolioNumber TEXT, Buyer_PaxID TEXT ,"+
                        "Check_Number TEXT, Transaction_Amount DECIMAL, Transaction_DateTime TIMESTAMP, Transaction_Description TEXT,"+
                        "Transaction_Type TEXT, Department_ID TEXT, Department_Description TEXT,  Source_Record_TimeStamp TIMESTAMP, PRIMARY KEY((Ship_Code,Sail_Date),Booking_ID,Payer_PaxID))"
        );
    }

    /*
    * START: Prepare statement for insert Folio values into Folios table.
    * This is just creation of prepared statement, we will map this statement with our event
    */

    private CompletionStage<Done> prepareWriteFolio() {
        LOGGER.info(" prepareWriteFolio method ... ");
        return session.prepare(
                "INSERT INTO Folios (Ship_Code, Sail_Date, Booking_ID, Payer_PaxID, Transaction_ID, Record_type, Payer_FolioNumber, Buyer_FolioNumber, Buyer_PaxID," +
                        "Check_Number, Transaction_Amount, Transaction_DateTime, Transaction_Description,"+
                        "Transaction_Type, Department_ID, Department_Description, Source_Record_TimeStamp) VALUES (?, ?, ?, ?,now(), ?,?,?,?,?,?,dateOf(now()),?,?,?,?,dateOf(now()))"

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
        bindWriteFolio.setString("Ship_Code", event.getFolio().getShipCode());
        bindWriteFolio.setString("Sail_Date", event.getFolio().getSailDate());
        bindWriteFolio.setString("Booking_ID", event.getFolio().getBookingId());
        bindWriteFolio.setInt("Payer_PaxID", event.getFolio().getPaxId());
        bindWriteFolio.setString("Record_Type", event.getFolio().getRecordType());
        bindWriteFolio.setString("Payer_FolioNumber", event.getFolio().getPayerFolioNumber());
        bindWriteFolio.setString("Buyer_FolioNumber", event.getFolio().getBuyerFolioNumber());
        bindWriteFolio.setString("Buyer_PaxID", event.getFolio().getBuyerPaxId());
        bindWriteFolio.setString("Check_Number", event.getFolio().getCheckNumber());
        bindWriteFolio.setDecimal("Transaction_Amount", BigDecimal.valueOf(event.getFolio().getTransactionAmount()));
        /*bindWriteFolio.setString("Transaction_DateTime", event.getFolio().getTransactionDateTime());*/
        bindWriteFolio.setString("Transaction_Description", event.getFolio().getTransactionDescription());
        bindWriteFolio.setString("Transaction_Type", event.getFolio().getTransactionType());
        bindWriteFolio.setString("Department_ID", event.getFolio().getDepartmentId());
        bindWriteFolio.setString("Department_Description", event.getFolio().getDepartmentDescription());


        //        bindWriteFolio.setString("folioTransaction", event.getFolio().getFolioTransaction());
//        bindWriteFolio.setTimestamp("Source_Record_TimeStamp",Date.valueOf(LocalDateTime.now().toLocalDate()));
        return CassandraReadSide.completedStatements(Arrays.asList(bindWriteFolio));
    }
    /* ******************* END ****************************/

    /* START: Prepare statement for update the data in Folios table.
    * This is just creation of prepared statement, we will map this statement with our event
    */

    private CompletionStage<List<BoundStatement>> processPostUpdated(FolioUpdated event) {
        BoundStatement bindWriteFolio = writeFolios.bind();
        bindWriteFolio.setString("Ship_Code", event.getFolio().getShipCode());
        bindWriteFolio.setString("Sail_Date", event.getFolio().getSailDate());
        bindWriteFolio.setString("Booking_ID", event.getFolio().getBookingId());
        bindWriteFolio.setInt("Payer_PaxID", event.getFolio().getPaxId());

//        bindWriteFolio.setUUID("Transaction_ID", UUIDGen.);
        bindWriteFolio.setString("Record_Type", event.getFolio().getRecordType());
        bindWriteFolio.setString("Payer_FolioNumber", event.getFolio().getPayerFolioNumber());
        bindWriteFolio.setString("Buyer_FolioNumber", event.getFolio().getBuyerFolioNumber());
        bindWriteFolio.setString("Buyer_PaxID", event.getFolio().getBuyerPaxId());
        bindWriteFolio.setString("Check_Number", event.getFolio().getCheckNumber());
        bindWriteFolio.setDecimal("Transaction_Amount", BigDecimal.valueOf(event.getFolio().getTransactionAmount()));
        /*bindWriteFolio.setString("Transaction_DateTime", event.getFolio().getTransactionDateTime());*/
        bindWriteFolio.setString("Transaction_Description", event.getFolio().getTransactionDescription());
        bindWriteFolio.setString("Transaction_Type", event.getFolio().getTransactionType());
        bindWriteFolio.setString("Department_ID", event.getFolio().getDepartmentId());
        bindWriteFolio.setString("Department_Description", event.getFolio().getDepartmentDescription());

//        bindWriteFolio.setString("folioTransaction", event.getFolio().getFolioTransaction());
//        bindWriteFolio.setTimestamp("Source_Record_TimeStamp",Date.valueOf(LocalDateTime.now().toLocalDate()));
        return CassandraReadSide.completedStatements(Arrays.asList(bindWriteFolio));
    }
    /* ******************* END ****************************/

    /* START: Prepare statement for delete the the Folio from table.
    * This is just creation of prepared statement, we will map this statement with our event
    */

    private CompletionStage<Done> prepareDeleteFolio() {
        return session.prepare(
                "DELETE FROM Folios WHERE Ship_Code = ? AND Sail_Date = ? AND Booking_ID = ? AND Payer_PaxID = ? "
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
        bindWriteFolio.setString("Ship_Code", event.getFolio().getShipCode());
        bindWriteFolio.setString("Sail_Date", event.getFolio().getSailDate());
        bindWriteFolio.setString("Booking_ID", event.getFolio().getBookingId());
        bindWriteFolio.setInt("Payer_PaxID", event.getFolio().getPaxId());

        return CassandraReadSide.completedStatements(Arrays.asList(bindWriteFolio));
    }
    /* ******************* END ****************************/
}
