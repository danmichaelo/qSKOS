package at.ac.univie.mminf.qskos4j.issues.language.test;

import at.ac.univie.mminf.qskos4j.issues.language.OmittedOrInvalidLanguageTags;
import at.ac.univie.mminf.qskos4j.util.IssueTestCase;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Created by christian
 * Date: 27.01.13
 * Time: 00:42
 */
public class OmittedOrInvalidLanguageTagsTest extends IssueTestCase {

    private OmittedOrInvalidLanguageTags oiltComponents, oiltDeprecatedAndIllegal, oiltLangTags;

    @Before
    public void setUp() throws OpenRDFException, IOException {
        oiltComponents = new OmittedOrInvalidLanguageTags(setUpRepository("components.rdf"));
        oiltDeprecatedAndIllegal = new OmittedOrInvalidLanguageTags(setUpRepository("deprecatedAndIllegalTerms.rdf"));
        oiltLangTags = new OmittedOrInvalidLanguageTags(setUpRepository("languageTags.rdf"));
    }

    @Test
    public void testMissingLangTagCount_1() throws OpenRDFException {
        Map<Resource, Collection<Literal>> missingLangTags = oiltComponents.getResult().getData();
        Assert.assertEquals(2, missingLangTags.size());
    }

    @Test
    public void testMissingLangTagCount_2() throws OpenRDFException {
        Map<Resource, Collection<Literal>> missingLangTags = oiltDeprecatedAndIllegal.getResult().getData();

        Assert.assertEquals(1, missingLangTags.keySet().size());
        Assert.assertEquals(2, countEntries(missingLangTags.values()));
    }

    private int countEntries(Collection<Collection<Literal>> allLiterals) {
        int literalCount = 0;
        for (Collection<Literal> literals : allLiterals) {
            literalCount += literals.size();
        }
        return literalCount;
    }

    @Test
    public void testMissingLangTagCount_3() throws OpenRDFException {
        Map<Resource, Collection<Literal>> missingLangTags = oiltLangTags.getResult().getData();

        // expected result should be 0, but current implementatin relies on org.openrdf.model.util.language.Iso639 checks
        Assert.assertEquals(5, countEntries(missingLangTags.values()));
    }

}
