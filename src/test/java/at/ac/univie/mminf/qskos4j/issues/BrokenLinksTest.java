package at.ac.univie.mminf.qskos4j.issues;

import at.ac.univie.mminf.qskos4j.issues.outlinks.BrokenLinks;
import at.ac.univie.mminf.qskos4j.issues.outlinks.HttpURIs;
import at.ac.univie.mminf.qskos4j.util.vocab.RepositoryBuilder;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.OpenRDFException;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

/**
 * Created by christian
 * Date: 26.01.13
 * Time: 16:44
 */
public class BrokenLinksTest {

    private BrokenLinks brokenLinks;

    @Before
    public void setUp() throws OpenRDFException, IOException {
        brokenLinks = new BrokenLinks(new HttpURIs());
        brokenLinks.setRepositoryConnection(new RepositoryBuilder().setUpFromTestResource("resources.rdf").getConnection());
        brokenLinks.setExtAccessDelayMillis(0);
    }

    @Test
    public void testBrokenLinks() throws OpenRDFException {
        Collection<URL> brokenLinkURLs = brokenLinks.getResult();
        Assert.assertEquals(1, brokenLinkURLs.size());
    }

}
