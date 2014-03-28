package yle.neo4j;

import org.junit.Before;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.test.TestGraphDatabaseFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static junit.framework.TestCase.assertEquals;

public class IdGeneratorTest {
    private GraphDatabaseService neo4j = null;
    private IdGenerator plugin = null;
    private IdGenerator plugin2 = null;

    @Before
    public void setUpBeforeClass() throws Exception {
        neo4j =  new TestGraphDatabaseFactory().newImpermanentDatabase();
        plugin = new IdGenerator();
        plugin2 = new IdGenerator();
    }

    @Test
    public void returnsGivenAmountOfUniqueIdsInMultiThreadedEnvironmentWithMiltipleInstacesOfPluginOnServer() throws InterruptedException {
        Integer numberOfThreads = 500;
        final CountDownLatch latch = new CountDownLatch(numberOfThreads);
        final List<Long> generatedIds = Collections.synchronizedList(new ArrayList());
        for (int i = 0; i < numberOfThreads; i++) {
            new Thread() {
                public void run() {
                    generatedIds.addAll(plugin.getIds(neo4j,2));
                    generatedIds.addAll(plugin2.getIds(neo4j,2));
                    latch.countDown();
                }
            }.start();
        }
        latch.await();
        assertEquals(generatedIds.size(), new HashSet(generatedIds).size());
    }
}
