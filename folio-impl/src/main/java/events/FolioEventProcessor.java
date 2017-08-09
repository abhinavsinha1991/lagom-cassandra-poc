package events;

import akka.Done;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.utils.UUIDs;
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
                        "Ship_Code TEXT, Sail_Date TEXT, Payer_PaxID INT , Booking_ID TEXT ," +
                        "Charge_ID INT, Buyer_PaxID Int, Payer_FolioNumber TEXT, Buyer_FolioNumber TEXT,"+
                        "Item_ID Int, Check_Number TEXT, Transaction_Amount DECIMAL, Transaction_DateTime TIMESTAMP, Transaction_Description TEXT,"+
                        "Charge_Type TEXT, Department_ID TEXT, Department_Description TEXT,  Source_Record_TimeStamp TIMESTAMP, " +
                        "PRIMARY KEY((Ship_Code,Sail_Date), Payer_PaxID, Charge_ID))"
        );
    }

    /*
    * START: Prepare statement for insert Folio values into Folios table.
    * This is just creation of prepared statement, we will map this statement with our event
    */

    private CompletionStage<Done> prepareWriteFolio() {
        LOGGER.info(" prepareWriteFolio method ... ");
        return session.prepare(
                "INSERT INTO Folios (Ship_Code, Sail_Date, Payer_PaxID, Booking_ID, Charge_ID, Buyer_PaxID, Payer_FolioNumber, Buyer_FolioNumber,"+
                        "Item_ID, Check_Number, Transaction_Amount, Transaction_DateTime, Transaction_Description,"+
                        "Charge_Type, Department_ID, Department_Description, Source_Record_TimeStamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,dateOf(now())," +
                        " ?, ?, ?, ?, dateOf(now()))"

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
        bindWriteFolio.setInt("Payer_PaxID", event.getFolio().getPayerPaxId());
        bindWriteFolio.setString("Booking_ID", event.getFolio().getBookingId());
//        LOGGER.info(event.getFolio().getBookingId()+".........>>>>>\n\n\n\n\n");
        bindWriteFolio.setInt("Charge_ID", event.getFolio().getChargeId());
        bindWriteFolio.setInt("Buyer_PaxID", event.getFolio().getBuyerPaxId());
        bindWriteFolio.setString("Payer_FolioNumber", event.getFolio().getPayerFolioNumber());
        bindWriteFolio.setString("Buyer_FolioNumber", event.getFolio().getBuyerFolioNumber());
        bindWriteFolio.setInt("Item_ID", event.getFolio().getItemId());
        bindWriteFolio.setString("Check_Number", event.getFolio().getCheckNumber());
        bindWriteFolio.setDecimal("Transaction_Amount", (event.getFolio().getTransactionAmount()));
        bindWriteFolio.setString("Transaction_Description", event.getFolio().getTransactionDescription());
        bindWriteFolio.setString("Charge_Type", event.getFolio().getChargeType());
        bindWriteFolio.setString("Department_ID", event.getFolio().getDepartmentId());
        bindWriteFolio.setString("Department_Description", event.getFolio().getDepartmentDescription());

//        LOGGER.info(event.getFolio().getPayerPaxId()+".........>>>>>\n\n\n\n\n");
//        UUID uid = UUID.fromString("38400000-8cf0-11bd-b23e-10b96e4ef00d");

        // checking time stamp value
//        LOGGER.info(">>>>>><<<<<<<<<<"+ UUIDs.timeBased());
//        LOGGER.info(".............."+uid.timestamp());
//        bindWriteFolio.setUUID("Transaction_ID", UUIDs.timeBased());

//        bindWriteFolio.setString("Record_Type", event.getFolio().getRecordType());
//        LOGGER.info(event.getFolio().getRecordType()+".........>>>>>\n\n\n\n\n");
//        LOGGER.info(event.getFolio().getPayerFolioNumber()+".........>>>>>\n\n\n\n\n");
//        LOGGER.info(event.getFolio().getBuyerFolioNumber()+".........>>>>>\n\n\n\n\n");
//        LOGGER.info(event.getFolio().getCheckNumber()+"\n\n\n\n\n.........<<<<>>>>>");
//
//        LOGGER.info(event.getFolio().getTransactionAmount()+".........>>>>>\n\n\n\n\n");

        /*bindWriteFolio.setString("Transaction_DateTime", event.getFolio().getTransactionDateTime());*/
//        bindWriteFolio.setString("Transaction_Type", event.getFolio().getTransactionType());


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
        bindWriteFolio.setInt("Payer_PaxID", event.getFolio().getPayerPaxId());
        bindWriteFolio.setString("Booking_ID", event.getFolio().getBookingId());
//        LOGGER.info(event.getFolio().getBookingId()+".........>>>>>\n\n\n\n\n");
        bindWriteFolio.setInt("Charge_ID", event.getFolio().getChargeId());
        bindWriteFolio.setInt("Buyer_PaxID", event.getFolio().getBuyerPaxId());
        bindWriteFolio.setString("Payer_FolioNumber", event.getFolio().getPayerFolioNumber());
        bindWriteFolio.setString("Buyer_FolioNumber", event.getFolio().getBuyerFolioNumber());
        bindWriteFolio.setInt("Item_ID", event.getFolio().getItemId());
        bindWriteFolio.setString("Check_Number", event.getFolio().getCheckNumber());
        bindWriteFolio.setDecimal("Transaction_Amount", (event.getFolio().getTransactionAmount()));
        bindWriteFolio.setString("Transaction_Description", event.getFolio().getTransactionDescription());
        bindWriteFolio.setString("Charge_Type", event.getFolio().getChargeType());
        bindWriteFolio.setString("Department_ID", event.getFolio().getDepartmentId());
        bindWriteFolio.setString("Department_Description", event.getFolio().getDepartmentDescription());


        //        bindWriteFolio.setString("Ship_Code", event.getFolio().getShipCode());
//        bindWriteFolio.setString("Sail_Date", event.getFolio().getSailDate());
//        bindWriteFolio.setString("Booking_ID", event.getFolio().getBookingId());
//        bindWriteFolio.setInt("Payer_PaxID", event.getFolio().getPayerPaxId());
//
////        bindWriteFolio.setUUID("Transaction_ID", UUIDGen.);
////        bindWriteFolio.setString("Record_Type", event.getFolio().getRecordType());
//        bindWriteFolio.setString("Payer_FolioNumber", event.getFolio().getPayerFolioNumber());
//        bindWriteFolio.setString("Buyer_FolioNumber", event.getFolio().getBuyerFolioNumber());
//        bindWriteFolio.setString("Buyer_PaxID", event.getFolio().getBuyerPaxId());
//        bindWriteFolio.setString("Check_Number", event.getFolio().getCheckNumber());
//        bindWriteFolio.setDecimal("Transaction_Amount", event.getFolio().getTransactionAmount());
//        /*bindWriteFolio.setString("Transaction_DateTime", event.getFolio().getTransactionDateTime());*/
//        bindWriteFolio.setString("Transaction_Description", event.getFolio().getTransactionDescription());
//        bindWriteFolio.setString("Transaction_Type", event.getFolio().getTransactionType());
//        bindWriteFolio.setString("Department_ID", event.getFolio().getDepartmentId());
//        bindWriteFolio.setString("Department_Description", event.getFolio().getDepartmentDescription());

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
                "DELETE FROM Folios WHERE Ship_Code = ? AND Sail_Date = ? AND Payer_PaxID = ? AND Charge_ID = ?"
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
        bindWriteFolio.setInt("Payer_PaxID", event.getFolio().getPayerPaxId());
        bindWriteFolio.setInt("Charge_ID", event.getFolio().getChargeId());

        return CassandraReadSide.completedStatements(Arrays.asList(bindWriteFolio));
    }
    /* ******************* END ****************************/
}
