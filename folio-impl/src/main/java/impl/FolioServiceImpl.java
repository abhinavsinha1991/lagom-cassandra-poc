package impl;

import akka.Done;
import akka.NotUsed;
import com.knoldus.Folio;
import com.knoldus.FolioService;
import com.lightbend.lagom.javadsl.api.ServiceCall;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRef;
import com.lightbend.lagom.javadsl.persistence.PersistentEntityRegistry;
import com.lightbend.lagom.javadsl.persistence.ReadSide;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import commands.FolioCommand;
import entity.FolioEntity;
import events.FolioEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

public class FolioServiceImpl implements FolioService {

    private final PersistentEntityRegistry persistentEntityRegistry;
    private final CassandraSession session;

    private static final Logger LOGGER = LoggerFactory.getLogger(FolioServiceImpl.class);


    @Inject
    public FolioServiceImpl(final PersistentEntityRegistry registry, ReadSide readSide, CassandraSession session) {
        this.persistentEntityRegistry = registry;
        this.session = session;

        persistentEntityRegistry.register(FolioEntity.class);
        readSide.register(FolioEventProcessor.class);
    }

    @Override
    public ServiceCall<NotUsed, Optional<Folio>> folio(Optional<String> shipCode,
                                                       Optional<String> sailDate, Optional<Integer> paxId, Optional<Integer> chargeId) {
        return request -> {
            CompletionStage<Optional<Folio>> folioFuture =
                    session.selectAll("SELECT * FROM Folios WHERE Ship_Code = ? AND Sail_Date = ? AND Payer_PaxID = ? AND Charge_ID = ?",
                            shipCode.get(), sailDate.get(), paxId.get(), chargeId.get())
                            .thenApply(rows ->
                                    rows.stream()
                                            .map(row -> Folio.builder().shipCode(row.getString("Ship_Code"))
                                                    .sailDate(row.getString("Sail_Date")).payerPaxId(row.get("Payer_PaxID", Integer.class))
                                                    .bookingId(row.getString("Booking_ID")).chargeId(row.get("Charge_ID", Integer.class ))
                                                    .buyerPaxId(row.get("Buyer_PaxID", Integer.class)).payerFolioNumber(row.getString("Payer_FolioNumber"))
                                                    .buyerFolioNumber(row.getString("Buyer_FolioNumber")).itemId(row.get("Item_ID", Integer.class))
                                                    .checkNumber(row.getString("Check_Number")).transactionAmount(row.getDecimal("Transaction_Amount"))
                                                    .transactionDateTime(row.getTimestamp("Transaction_DateTime")).transactionDescription(row.getString("Transaction_Description"))
                                                    .chargeType(row.getString("Charge_Type")).departmentId(row.getString("Department_ID"))
                                                    .sourceRecordTimeStamp(row.getTimestamp("Source_Record_TimeStamp"))
                                                    .build()
                                            )
                                            .findFirst()
                            );

            return folioFuture;
        };
    }

    @Override
    public ServiceCall<Folio, Done> newFolio() {
        return folio -> {
            PersistentEntityRef<FolioCommand> ref = folioEntityRef(folio);
            return ref.ask(FolioCommand.CreateFolio.builder().folio(folio).build());
        };
    }

    @Override
    public ServiceCall<Folio, Done> updateFolio(Optional<String> shipCode,
                                                Optional<String> sailDate, Optional<Integer> payerPaxId, Optional<Integer> chargeId) {
        return folio -> {
            PersistentEntityRef<FolioCommand> ref = folioEntityRef(folio);
            return ref.ask(FolioCommand.UpdateFolio.builder().folio(folio).build());
        };
    }

    @Override
    public ServiceCall<NotUsed, Done> deleteFolio(Optional<String> shipCode,
                                                  Optional<String> sailDate, Optional<Integer> payerPaxId, Optional<Integer> chargeId ) {
        return request -> {
            Folio folio = Folio.builder().shipCode(shipCode.isPresent() ? shipCode.get() : "")
                    .sailDate(sailDate.isPresent() ? sailDate.get() : "")
                    .payerPaxId(payerPaxId.isPresent() ? payerPaxId.get() : Integer.getInteger("0"))
                    .chargeId(chargeId.isPresent() ? chargeId.get() : Integer.getInteger("0"))
                    .build();
            PersistentEntityRef<FolioCommand> ref = folioEntityRef(folio);
            return ref.ask(FolioCommand.DeleteFolio.builder().folio(folio).build());
        };
    }

    private PersistentEntityRef<FolioCommand> folioEntityRef(Folio folio) {
        LOGGER.info(" folioEntityRef method ... ");
        return persistentEntityRegistry.refFor(FolioEntity.class, folio.getShipCode() + folio.getSailDate() + folio.getPayerPaxId() + folio.getChargeId());
    }
}

