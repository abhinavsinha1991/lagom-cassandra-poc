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
import events.FolioEventProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Optional;
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
                                                       Optional<String> sailDate,Optional<String> bookingId,Optional<Integer> paxId ) {
        return request -> {
            CompletionStage<Optional<Folio>> folioFuture =
                    session.selectAll("SELECT * FROM Folios WHERE shipCode = ? AND sailDate = ? AND bookingId = ? AND paxId = ? ",
                            shipCode.get(),sailDate.get(),bookingId.get(),paxId.get())
                            .thenApply(rows ->
                                    rows.stream()
                                            .map(row -> Folio.builder().shipCode(row.getString("Ship_Code"))
                                                    .sailDate(row.getString("Sail_Date")).bookingId(row.getString("Booking_ID"))
                                                    .paxId(row.get("Payer_PaxID",Integer.class)).transactionId(row.getString("Transaction_ID"))
                                                    .recordType(row.getString("Record_Type")).payerFolioNumber(row.getString("Payer_FolioNumber"))
                                                    .buyerFolioNumber(row.getString("Buyer_FolioNumber")).buyerPaxId(row.getString("Buyer_PaxID"))
                                                    .checkNumber(row.getString("Check_Number")).transactionAmount(row.getDouble("Transaction_Amount"))
                                                    .transactionDateTime(row.getString("Transaction_DateTime")).transactionDescription(row.getString("Transaction_Description"))
                                                    .transactionType(row.getString("Transaction_Type")).departmentId(row.getString("Department_ID"))
                                                    .sourceRecordTimeStamp(row.getString("Source_Record_TimeStamp"))
                                                    .build()
                                            )
                                            .findFirst()
                            );
            /*try {
                JSONObject json = new JSONObject(folioFuture.toCompletableFuture().get().get());
                LOGGER.info("The jsonobject is :", json);
            }
            catch(Exception e) {

            }*/
            return folioFuture;
        };
    }

    @Override
    public ServiceCall<Folio, Done> newFolio() {
        return folio -> {
            System.out.println("sumit :::::::::::::::::::::::::;inside ");
            PersistentEntityRef<FolioCommand> ref = folioEntityRef(folio);
            return ref.ask(FolioCommand.CreateFolio.builder().folio(folio).build());
        };
    }

    @Override
    public ServiceCall<Folio, Done> updateFolio(Optional<String> shipCode,
                                                Optional<String> sailDate,Optional<String> bookingId,Optional<Integer> paxId ) {
        return folio -> {
            PersistentEntityRef<FolioCommand> ref = folioEntityRef(folio);
            return ref.ask(FolioCommand.UpdateFolio.builder().folio(folio).build());
        };
    }

    @Override
    public ServiceCall<NotUsed, Done> deleteFolio(Optional<String> shipCode,
                                                  Optional<String> sailDate,Optional<String> bookingId,Optional<Integer> paxId ) {
        return request -> {
            Folio folio = Folio.builder().shipCode(shipCode.isPresent() ? shipCode.get() : "")
                    .sailDate(sailDate.isPresent() ? sailDate.get() : "")
                    .bookingId(bookingId.isPresent() ? bookingId.get() : "")
                    .paxId(paxId.isPresent() ? paxId.get() : Integer.getInteger("0"))
                    .build();
            PersistentEntityRef<FolioCommand> ref = folioEntityRef(folio);
            return ref.ask(FolioCommand.DeleteFolio.builder().folio(folio).build());
        };
    }

    private PersistentEntityRef<FolioCommand> folioEntityRef(Folio folio) {
        LOGGER.info(" folioEntityRef method ... ");
        return persistentEntityRegistry.refFor(FolioEntity.class, folio.getShipCode() + folio.getSailDate() + folio.getBookingId() + folio.getPaxId());
    }
}

