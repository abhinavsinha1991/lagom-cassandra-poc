package impl;

import akka.Done;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import com.knoldus.Folio;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver;
import com.lightbend.lagom.javadsl.testkit.PersistentEntityTestDriver.Outcome;
import commands.FolioCommand;
import entity.FolioEntity;
import events.FolioEvent;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import states.FolioStates;

import java.util.Date;
import java.util.Optional;
import java.math.BigDecimal;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class FolioEntityTest {

    private static ActorSystem system;

    BigDecimal amount = new BigDecimal("1115.37");

    Date date = new Date();

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        JavaTestKit.shutdownActorSystem(system);
        system = null;
    }

    @Test
    public void testAddNewUser() {
        PersistentEntityTestDriver<FolioCommand, FolioEvent, FolioStates> driver =
                new PersistentEntityTestDriver<>(system, new FolioEntity(), "1001");

        Folio data = Folio.builder().shipCode("1001").sailDate("2017/08/02").payerPaxId(123).bookingId("id")
                .chargeId(111).buyerPaxId(121).payerFolioNumber("12345").buyerFolioNumber("121")
                .itemId(11).checkNumber("abc").transactionAmount(amount).transactionDateTime(date)
                .transactionDescription("aaa").chargeType("c").departmentId("id").departmentDescription("abc")
                .sourceRecordTimeStamp(date).build();
        Outcome<FolioEvent, FolioStates> outcome = driver.run(FolioCommand.CreateFolio.builder().folio(data).build());

        assertThat(outcome.events().get(0), is(equalTo(FolioEvent.FolioCreated.builder().folio(data).entityId("1001").build())));
        assertThat(outcome.events().size(), is(equalTo(1)));
        assertThat(outcome.state().getFolio().get(), is(equalTo(data)));

        assertThat(outcome.getReplies().get(0), is(equalTo(Done.getInstance())));
        outcome.issues().stream().forEach(System.out::println);
        assertThat(outcome.issues().isEmpty(), is(true));
    }

    @Test
    public void testUpdateUser() {
        PersistentEntityTestDriver<FolioCommand, FolioEvent, FolioStates> driver =
                new PersistentEntityTestDriver<>(system, new FolioEntity(), "1001");

        Folio data = Folio.builder().shipCode("1001").sailDate("2017/08/02").payerPaxId(123).bookingId("id")
                .chargeId(111).buyerPaxId(121).payerFolioNumber("12345").buyerFolioNumber("121")
                .itemId(11).checkNumber("abc").transactionAmount(amount).transactionDateTime(date)
                .transactionDescription("aaa").chargeType("c").departmentId("id").departmentDescription("abc")
                .sourceRecordTimeStamp(date).build();

        driver.run(FolioCommand.CreateFolio.builder().folio(data).build());

        Folio updateFolio = Folio.builder().shipCode("1001").sailDate("2017/08/02").payerPaxId(123).bookingId("id")
                .chargeId(111).buyerPaxId(121).payerFolioNumber("12345").buyerFolioNumber("121")
                .itemId(11).checkNumber("abc").transactionAmount(amount).transactionDateTime(date)
                .transactionDescription("aaa").chargeType("c").departmentId("id").departmentDescription("abc")
                .sourceRecordTimeStamp(date).build();
        Outcome<FolioEvent, FolioStates> outcome = driver.run(FolioCommand.UpdateFolio.builder().folio(updateFolio).build());

        assertThat(outcome.events().get(0), is(equalTo(FolioEvent.FolioUpdated.builder().folio(updateFolio).entityId("1001").build())));
        assertThat(outcome.events().size(), is(equalTo(1)));
        assertThat(outcome.state().getFolio().get(), is(equalTo(updateFolio)));

        assertThat(outcome.getReplies().get(0), is(equalTo(Done.getInstance())));
        assertThat(outcome.issues().isEmpty(), is(true));
    }

    @Test
    public void testDeleteUser() {
        PersistentEntityTestDriver<FolioCommand, FolioEvent, FolioStates> driver =
                new PersistentEntityTestDriver<>(system, new FolioEntity(), "1001");

        Folio data = Folio.builder().shipCode("1001").sailDate("2017/08/02").payerPaxId(123).bookingId("id")
                .chargeId(111).buyerPaxId(121).payerFolioNumber("12345").buyerFolioNumber("121")
                .itemId(11).checkNumber("abc").transactionAmount(amount).transactionDateTime(date)
                .transactionDescription("aaa").chargeType("c").departmentId("id").departmentDescription("abc")
                .sourceRecordTimeStamp(date).build();

        driver.run(FolioCommand.CreateFolio.builder().folio(data).build());

        Outcome<FolioEvent, FolioStates> outcome = driver.run(FolioCommand.DeleteFolio.builder().folio(data).build());

        assertThat(outcome.events().get(0), is(equalTo(FolioEvent.FolioDeleted.builder().folio(data).entityId("1001").build())));
        assertThat(outcome.events().size(), is(equalTo(1)));

        assertThat(outcome.state().getFolio(), is(equalTo(Optional.empty())));

        assertThat(outcome.getReplies().get(0), is(equalTo(Done.getInstance())));
        assertThat(outcome.issues().size(), is(equalTo(0)));
    }

    @Test
    public void testUserCurrentState() {
        PersistentEntityTestDriver<FolioCommand, FolioEvent, FolioStates> driver =
                new PersistentEntityTestDriver<>(system, new FolioEntity(), "1001");

        Folio data = Folio.builder().shipCode("1001").sailDate("2017/08/02").payerPaxId(123).bookingId("id")
                .chargeId(111).buyerPaxId(121).payerFolioNumber("12345").buyerFolioNumber("121")
                .itemId(11).checkNumber("abc").transactionAmount(amount).transactionDateTime(date)
                .transactionDescription("aaa").chargeType("c").departmentId("id").departmentDescription("abc")
                .sourceRecordTimeStamp(date).build();

        Outcome<FolioEvent, FolioStates> outcome1 = driver.run(FolioCommand.CreateFolio.builder().folio(data).build());

        assertThat(outcome1.events().get(0), is(equalTo(FolioEvent.FolioCreated.builder().folio(data).entityId("1001").build())));
        assertThat(outcome1.state().getFolio().get(), is(equalTo(data)));

        Folio updateFolio = Folio.builder().shipCode("1001").sailDate("2017/08/02").payerPaxId(123).bookingId("id")
                .chargeId(111).buyerPaxId(121).payerFolioNumber("12345").buyerFolioNumber("121")
                .itemId(11).checkNumber("abc").transactionAmount(amount).transactionDateTime(date)
                .transactionDescription("aaa").chargeType("c").departmentId("id").departmentDescription("abc")
                .sourceRecordTimeStamp(date).build();

        Outcome<FolioEvent, FolioStates> outcome2 = driver.run(FolioCommand.UpdateFolio.builder().folio(updateFolio).build());
        assertThat(outcome2.state().getFolio().get(), is(equalTo(updateFolio)));

        Outcome<FolioEvent, FolioStates> outcome3 = driver.run(FolioCommand.DeleteFolio.builder().folio(data).build());
        assertThat(outcome3.state().getFolio(), is(equalTo(Optional.empty())));
    }
}