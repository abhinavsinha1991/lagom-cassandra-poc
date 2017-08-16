package it;

import akka.Done;
import akka.NotUsed;
import com.knoldus.Folio;
import com.knoldus.FolioService;
import com.lightbend.lagom.javadsl.testkit.ServiceTest;
import com.typesafe.config.ConfigFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Date;
import com.lightbend.lagom.javadsl.persistence.cassandra.CassandraSession;
import java.util.Optional;

import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
import static com.lightbend.lagom.javadsl.testkit.ServiceTest.startServer;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;

public class ITFolioServiceImplTest {
    private static ServiceTest.TestServer server;
    static String keyspace = ConfigFactory.load("application.conf").getString("cassandra-journal.keyspace");


    BigDecimal amount = new BigDecimal("1115.37");

    Date date = new Date();

    @BeforeClass
    public static void setUp() throws Exception {
        server = startServer(defaultSetup().withCassandra(true).configureBuilder(builder -> builder.configure
                ("cassandra-journal.contact-points", Collections.
                        singletonList("localhost:9042")).configure
                ("cassandra-journal.session-provider",
                        "akka.persistence.cassandra.ConfigSessionProvider")
                .configure("cassandra-snapshot-store.contact-points",
                        Collections.singletonList("localhost:9042"))
                .configure("cassandra-snapshot-store.session-provider",
                        "akka.persistence.cassandra.ConfigSessionProvider")
                .configure("lagom.persistence.read-side.cassandra.contact-points",
                        Collections.singletonList("localhost:9042"))
                .configure("lagom.persistence.read-side.cassandra.session-provider",
                        "akka.persistence.cassandra." + "ConfigSessionProvider")
                .configure("cassandra-journal.keyspace", keyspace)
                .configure("cassandra-snapshot-store.keyspace", keyspace)
                .configure("lagom.persistence.read-side.cassandra.keyspace", keyspace)

        ));

        /// /Get the DataStax's Cassandra Session Obj CassandraSession cassandraSession = server.injector().instanceOf(CassandraSession.class); session = cassandraSession.underlying().toCompletableFuture().get(); //Create the required schema. createSchema(session); //Add some fake data for testing purpose. populateData(session); }
//
//    public static void setUp() {
//
//      //  server = startServer(defaultSetup().withCassandra(true).configureBuilder());
//
//    }
    }

    @AfterClass
    public static void tearDown() throws Exception{

        Optional<String> shipCode = Optional.of("test-ship-code");

        Optional<String> sailDate = Optional.of("2017/08/09");

        Optional<Integer> payerPaxId= Optional.of(1);

        Optional<Integer> chargeId = Optional.of(1);

        FolioService folioService = server.client(FolioService.class);
       System.out.print("here it will delete the raw data");
       Done res = folioService.deleteFolio(shipCode, sailDate, payerPaxId, chargeId).invoke().toCompletableFuture().get(30, SECONDS);
       System.out.println(res);
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    @Test
    public void shouldCreate() throws Exception {
        FolioService folioService = server.client(FolioService.class);
        Folio data = new Folio("test-ship-code", "2017/08/09", 1, "250", 1, 121,"121",
                "11", 121, "111", amount, date , "abc",
                "aa", "11", "aa", date);
        Done actualResult = folioService.newFolio().invoke(data).toCompletableFuture().get(20, SECONDS);
        Done expectedResult = Done.getInstance();
        assertEquals(expectedResult, actualResult);
    }


//    @Test
//    public void shouldGetFolio() throws Exception {
//        FolioService folioService = server.client(FolioService.class);
//
//        Optional<Folio> expectedResult = Optional.of( new Folio("1", "2017/08/09", 1, "250", 1, 121,"121",
//                "11", 121, "111", amount, date , "abc",
//                "aa", "11", "aa", date));
//
//        Optional<String> shipCode = Optional.of("1");
//
//        Optional<String> sailDate = Optional.of("2017/08/09");
//
//        Optional<Integer> payerPaxId= Optional.of(1);
//
//        Optional<Integer> chargeId = Optional.of(1);
//
//
//            Optional<Folio> actualResult = folioService.folio(shipCode, sailDate, payerPaxId, chargeId).invoke().toCompletableFuture().get(20, SECONDS);
//           System.out.print(actualResult+"\n\n\n\n\n\n");
//        System.out.print(expectedResult+"\n\n\n\n\n\n");
//
//            assertEquals(expectedResult, actualResult);
//    }


//    @Test
//
//    public void shouldUpdate() throws Exception {
//        FolioService folioService = server.client(FolioService.class);
//        Folio data = new Folio("1", "2017/08/09", 2, "250", 1, 121,"121",
//                "11", 121, "111", amount, date , "abc",
//                "aa", "11", "aa", date);
//        Optional<String> a = Optional.of("al");
//        Optional<Integer> id = Optional.of(2);
//
//        Done actualResult = folioService.updateFolio(Optional.of("al"), a, id, id).invoke(data).toCompletableFuture().get(20, SECONDS);
//        Done expectedResult = Done.getInstance();
//        assertEquals(expectedResult, actualResult);
//    }
//
//    @Test
//
//    public void shouldDelete() throws Exception {
//        FolioService folioService = server.client(FolioService.class);
////        Folio data = new Folio("1", "2017/08/09", 2, "250", 1, 121,"121",
////                "11", 121, "111", amount, date , "abc",
////                "aa", "11", "aa", date);
//        Optional<String> a = Optional.of("al");
//        Optional<Integer> id = Optional.of(2);
//
//        Done actualResult = folioService.deleteFolio(Optional.of("al"), a, id, id).invoke().toCompletableFuture().get(20, SECONDS);
//        Done expectedResult = Done.getInstance();
//        assertEquals(expectedResult, actualResult);
//    }



}

