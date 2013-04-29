package at.ac.univie.mminf.qskos4j.issues;

import at.ac.univie.mminf.qskos4j.issues.language.OmittedOrInvalidLanguageTags;
import at.ac.univie.mminf.qskos4j.util.vocab.RepositoryBuilder;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Statement;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by christian
 * Date: 27.01.13
 * Time: 00:42
 */
public class OmittedOrInvalidLanguageTagsTest {

    private OmittedOrInvalidLanguageTags oiltComponents, oiltLangTags;

    @Before
    public void setUp() throws OpenRDFException, IOException {
        oiltComponents = new OmittedOrInvalidLanguageTags();
        oiltComponents.setRepositoryConnection(new RepositoryBuilder().setUpFromTestResource("components.rdf").getConnection());

        oiltLangTags = new OmittedOrInvalidLanguageTags();
        oiltLangTags.setRepositoryConnection(new RepositoryBuilder().setUpFromTestResource("languageTags.rdf").getConnection());
    }

    @Test
    public void testMissingLangTagInComponents() throws OpenRDFException {
        Collection<Statement> missingLangTags = oiltComponents.getPreparedData();

        Assert.assertTrue(hasOmittedOrInvalidLangTag("conceptB", missingLangTags));
        Assert.assertTrue(hasOmittedOrInvalidLangTag("conceptM", missingLangTags));

        // concept B is reported twice: altLabel and hiddenLabel have no language tags
        Assert.assertEquals(4, missingLangTags.size());
    }

    @Test
    public void testIso639LangTags() throws OpenRDFException {
        Collection<Statement> missingLangTags = oiltLangTags.getPreparedData();

        Assert.assertFalse(hasOmittedOrInvalidLangTag("conceptB", missingLangTags));
        Assert.assertFalse(hasOmittedOrInvalidLangTag("conceptC", missingLangTags));
        Assert.assertFalse(hasOmittedOrInvalidLangTag("conceptD", missingLangTags));
        Assert.assertFalse(hasOmittedOrInvalidLangTag("conceptX", missingLangTags));
        Assert.assertFalse(hasOmittedOrInvalidLangTag("conceptY", missingLangTags));
        Assert.assertFalse(hasOmittedOrInvalidLangTag("conceptZ", missingLangTags));
        Assert.assertFalse(hasOmittedOrInvalidLangTag("conceptV", missingLangTags));
    }

    private boolean hasOmittedOrInvalidLangTag(String uriSuffix, Collection<Statement> missingLangTags) {
        for (Statement statement : missingLangTags) {
            if (statement.getSubject().stringValue().endsWith(uriSuffix)) return true;
        }
        return false;
    }

}
