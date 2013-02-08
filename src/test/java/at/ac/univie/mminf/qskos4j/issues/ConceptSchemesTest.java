package at.ac.univie.mminf.qskos4j.issues;

import at.ac.univie.mminf.qskos4j.issues.conceptscheme.ConceptSchemes;
import at.ac.univie.mminf.qskos4j.util.QskosTestCase;
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
public class ConceptSchemesTest extends QskosTestCase {

    private ConceptSchemes conceptSchemes;

    @Before
    public void setUp() throws OpenRDFException, IOException {
        conceptSchemes = new ConceptSchemes(setUpRepository("aggregations.rdf"));
    }

    @Test
    public void testLexicalRelationsCount() throws OpenRDFException
    {
        Assert.assertEquals(5, conceptSchemes.getResult().getData().size());
    }
}