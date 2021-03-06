package at.ac.univie.mminf.qskos4j.issues;

import at.ac.univie.mminf.qskos4j.issues.count.AggregationRelations;
import at.ac.univie.mminf.qskos4j.util.vocab.RepositoryBuilder;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.OpenRDFException;

import java.io.IOException;

/**
 * Created by christian
 * Date: 26.01.13
 * Time: 14:47
 */
public class AggregationRelationsTest {

    private AggregationRelations aggregationRelations;

    @Before
    public void setUp() throws OpenRDFException, IOException {
        aggregationRelations = new AggregationRelations();
        aggregationRelations.setRepositoryConnection(new RepositoryBuilder().setUpFromTestResource("aggregations.rdf").getConnection());
    }

    @Test
    public void testAggregationRelationsCount() throws OpenRDFException
    {
        Assert.assertEquals(5, aggregationRelations.getResult().longValue());
    }

}
