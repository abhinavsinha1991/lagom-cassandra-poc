package events;

import akka.Done;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.lightbend.lagom.javadsl.persistence.AggregateEventTag;
import com.lightbend.lagom.javadsl.persistence.ReadSideProcessor;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import com.typesafe.config.ConfigFactory;
import events.FolioEvent.FolioCreated;
import events.FolioEvent.FolioDeleted;
import events.FolioEvent.FolioUpdated;
import org.pcollections.PSequence;
import org.pcollections.TreePVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionStage;


public class FolioEventProcessor extends ReadSideProcessor<FolioEvent> {

    String keySpaceName = ConfigFactory.load("application.conf").getString("cassandra-journal.keyspace");


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
                "CREATE TABLE IF NOT EXISTS "+keySpaceName+".Folios ( " +
                        "Ship_Code TEXT, Sail_Date TEXT, Payer_PaxID INT , Booking_ID TEXT ," +
                        "Charge_ID INT, Buyer_PaxID Int, Payer_FolioNumber TEXT, Buyer_FolioNumber TEXT," +
                        "Item_ID Int, Check_Number TEXT, Transaction_Amount DECIMAL, Transaction_DateTime TIMESTAMP, Transaction_Description TEXT," +
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
                "INSERT INTO "+keySpaceName+".Folios (Ship_Code, Sail_Date, Payer_PaxID, Booking_ID, Charge_ID, Buyer_PaxID, Payer_FolioNumber, Buyer_FolioNumber," +
                        "Item_ID, Check_Number, Transaction_Amount, Transaction_DateTime, Transaction_Description," +
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


        return CassandraReadSide.completedStatements(Arrays.asList(bindWriteFolio));
    }
    /* ******************* END ****************************/

    /* START: Prepare statement for delete the the Folio from table.
    * This is just creation of prepared statement, we will map this statement with our event
    */

    private CompletionStage<Done> prepareDeleteFolio() {
        return session.prepare(
                "DELETE FROM "+keySpaceName+".Folios WHERE Ship_Code = ? AND Sail_Date = ? AND Payer_PaxID = ? AND Charge_ID = ?"
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
