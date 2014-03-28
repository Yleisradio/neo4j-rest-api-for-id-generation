package yle.neo4j;

import org.neo4j.graphdb.*;
import org.neo4j.server.plugins.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Description("External id generator")
public class IdGenerator extends ServerPlugin {
    private static final String GENERATOR_ID_PROPERTY = "generatorId";
    private static final String GENERATOR_ID_VALUE = "generatorIdValue";
    private static final String GENERATOR_VALUE_PROPERTY = "val";
    private static Integer LOCK = 1;
    private static Label generatorLabel = DynamicLabel.label("IdGenerator");

    @Name("generateIds")
    @Description("Generate unique ids ")
    @PluginTarget(GraphDatabaseService.class)
    public List<Long> getIds(@Source GraphDatabaseService graphDb,
                             @Description("Number of ids to create")
                             @Parameter(name = "amount", optional = false) Integer amount) {
        synchronized (LOCK) {
            return extractIds(graphDb, amount);
        }
    }

    private List<Long> extractIds(GraphDatabaseService graphDb, Integer amount) {
        Transaction tx = graphDb.beginTx();
        try {
            Node generator = getOrCreateGeneratorNode(graphDb);
            List<Long> ids = generateIds(amount, generator);
            tx.success();
            return ids;
        } catch (Exception e) {
            tx.failure();
            throw new RuntimeException("Id generation failed!", e);
        } finally {
            tx.close();
        }
    }

    private List<Long> generateIds(Integer amount, Node generator) {
        List<Long> ids = new ArrayList();
        Long value = (Long) generator.getProperty(GENERATOR_VALUE_PROPERTY);
        for (int i = 0; i < amount; ++i) {
            ids.add(value + 1 + i);
        }
        generator.setProperty(GENERATOR_VALUE_PROPERTY, value + amount);
        return ids;
    }

    private Node getOrCreateGeneratorNode(GraphDatabaseService graphDb) {
        Iterator<Node> generatorNodeIterator = graphDb.findNodesByLabelAndProperty(generatorLabel, GENERATOR_ID_PROPERTY, GENERATOR_ID_VALUE).iterator();
        Node generator;
        if (generatorNodeIterator.hasNext()) {
            generator = generatorNodeIterator.next();
        } else {
            generator = createIdGeneratorNode(graphDb, generatorLabel);
        }
        return generator;
    }

    private Node createIdGeneratorNode(GraphDatabaseService graphDb, Label generatorLabel) {
        Node generator = graphDb.createNode(generatorLabel);
        generator.setProperty(GENERATOR_ID_PROPERTY, GENERATOR_ID_VALUE);
        generator.setProperty(GENERATOR_VALUE_PROPERTY, 0L);
        return generator;
    }
}
