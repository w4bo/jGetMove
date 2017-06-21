package fr.jgetmove.jgetmove.database;

import fr.jgetmove.jgetmove.exception.ClusterNotExistException;
import fr.jgetmove.jgetmove.exception.MalformedTimeIndexException;
import fr.jgetmove.jgetmove.io.Input;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseTest {

    private Database database;

    @BeforeEach
    void setUp() {

        // TODO : check exception d'initialisation
        try {
            database = new Database(new Input("tests/assets/itemset_check.dat"), new Input("tests/assets/itemset_check_time_index.dat"), 0);
        } catch (IOException | MalformedTimeIndexException | ClusterNotExistException e) {
            e.printStackTrace();
        }
    }

    @Test
    void getAndAddClusters() {

        assertEquals(5, database.getClusters().size());
        assertEquals(5, database.getClusterIds().size());
        assertEquals(1, database.getCluster(1).getTimeId());
        assertNull(database.getCluster(5));

        Cluster cluster1 = new Cluster(5);
        database.add(cluster1);

        assertEquals(cluster1, database.getCluster(5));
        assertEquals(6, database.getClusters().size());
        assertEquals(6, database.getClusterIds().size());
    }

    @Test
    void getAndAddTimes() {

        assertEquals(3, database.getTimes().size());
        assertEquals(3, database.getTimeIds().size());
        assertEquals(2, database.getTime(1).getClusters().size());

        assertNull(database.getTime(4));

        Time time1 = new Time(4);
        database.add(time1);

        assertEquals(time1, database.getTime(4));
        assertEquals(4, database.getTimes().size());
        assertEquals(4, database.getTimeIds().size());
        assertEquals(1, database.getTime(3).getClusters().size());
    }

    @Test
    void getAndAddTransactions() {

        assertEquals(2, database.getTransactions().size());
        assertEquals(2, database.getTransactionIds().size());
        assertEquals(3, database.getTransaction(1).getClusters().size());

        Transaction transaction1 = new Transaction(2);
        database.add(transaction1);

        assertEquals(3, database.getTransactions().size());
        assertEquals(3, database.getTransactionIds().size());
    }

    @Test
    void getClusterTransactions() {
        assertEquals(2, database.getClusterTransactions(4).size());
    }

    @Test
    void getClusterTime() {
        assertEquals(database.getTime(1), database.getClusterTime(1));
    }


    @Test
    void toJson() {
        assertEquals("{\"links\":[{\"id\":0,\"source\":0,\"target\":2,\"value\":1,\"label\":0},{\"id\":0,\"source\":2,\"target\":4,\"value\":1,\"label\":0},{\"id\":1,\"source\":1,\"target\":3,\"value\":1,\"label\":1},{\"id\":1,\"source\":3,\"target\":4,\"value\":1,\"label\":1}],\"nodes\":[{\"id\":0,\"label\":\"0\",\"time\":1},{\"id\":1,\"label\":\"1\",\"time\":1},{\"id\":2,\"label\":\"0\",\"time\":2},{\"id\":3,\"label\":\"1\",\"time\":2},{\"id\":4,\"label\":\"0,1\",\"time\":3}]}", database.toJson().build().toString());

    }

    @Test
    void isClusterInTransactions() {
        assertTrue(database.isClusterInTransactions(database.getTransactionIds(), 4));
        assertFalse(database.isClusterInTransactions(database.getTransactionIds(), 2));
    }

    @Test
    void getFilteredTransactionIdsIfHaveCluster() {
        assertEquals(database.getTransactionIds(), database.getFilteredTransactionIdsIfHaveCluster(database.getTransactionIds(), 4));
        assertEquals(0, database.getFilteredTransactionIdsIfHaveCluster(database.getTransactionIds(), 6).size());
    }

}
