//package it;
//
//import akka.Done;
//import com.knoldus.Folio;
//import com.knoldus.FolioService;
//import com.lightbend.lagom.javadsl.testkit.ServiceTest;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import java.util.Collections;
//
//import static com.lightbend.lagom.javadsl.testkit.ServiceTest.defaultSetup;
//import static com.lightbend.lagom.javadsl.testkit.ServiceTest.startServer;
//import static java.util.concurrent.TimeUnit.SECONDS;
//import static org.junit.Assert.assertEquals;
//
//public class ITFolioServiceImplTest {
//    private static ServiceTest.TestServer server;
//
//    @BeforeClass
//    public static void setUp() throws Exception {
//        server = startServer(defaultSetup().withCassandra(true));
//
////                .configureBuilder(builder -> builder.configure
////                ("cassandra-journal.contact-points", Collections.
////                        singletonList("localhost:9042")).configure
////                ("cassandra-journal.session-provider",
////                        "akka.persistence.cassandra.ConfigSessionProvider")
////                .configure("cassandra-snapshot-store.contact-points",
////                        Collections.singletonList("localhost:9042"))
////                .configure("cassandra-snapshot-store.session-provider",
////                        "akka.persistence.cassandra.ConfigSessionProvider")
////                .configure("lagom.persistence.read-side.cassandra.contact-points",
////                        Collections.singletonList("localhost:9042"))
////                .configure("lagom.persistence.read-side.cassandra.session-provider",
////                        "akka.persistence.cassandra." + "ConfigSessionProvider")
////                .configure("cassandra-journal.keyspace","folio_integ" )
////                .configure("cassandra-snapshot-store.keyspace","folio_integ")
////                .configure("lagom.persistence.read-side.cassandra.keyspace", "folio_integ")
////
////        ));
//        //Get the DataStax's Cassandra Session Obj CassandraSession cassandraSession = server.injector().instanceOf(CassandraSession.class); session = cassandraSession.underlying().toCompletableFuture().get(); //Create the required schema. createSchema(session); //Add some fake data for testing purpose. populateData(session); }
////
////    public static void setUp() {
////
////      //  server = startServer(defaultSetup().withCassandra(true).configureBuilder());
////
////    }
//    }
//
//    @AfterClass
//    public static void tearDown() {
//
//        if (server != null) {
//            server.stop();
//            server = null;
//        }
//    }
//
//    @Test
//    public void shouldWelcome() throws Exception {
//        FolioService folioService = server.client(FolioService.class);
//        Folio data = new Folio("1", "Kunal", "male", 250, "123", "111", "121",
//                "11", "121", "111", 123.45, "2017/07/31", "abc",
//                "aa", "11", "aa", "2017/02/08");
//        Done actualResult = folioService.newFolio().invoke(data).toCompletableFuture().get(20, SECONDS);
//        Done expectedResult = Done.getInstance();
//        assertEquals(expectedResult, actualResult);
//    }
//}
//
