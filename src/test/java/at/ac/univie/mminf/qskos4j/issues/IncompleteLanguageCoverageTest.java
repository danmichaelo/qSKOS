package at.ac.univie.mminf.qskos4j.issues;

import at.ac.univie.mminf.qskos4j.issues.concepts.InvolvedConcepts;
import at.ac.univie.mminf.qskos4j.issues.language.IncompleteLanguageCoverage;
import at.ac.univie.mminf.qskos4j.util.vocab.RepositoryBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Value;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by christian
 * Date: 27.01.13
 * Time: 00:54
 */
public class IncompleteLanguageCoverageTest {

    private IncompleteLanguageCoverage incompleteLanguageCoverage;

    @Before
    public void setUp() throws OpenRDFException, IOException {
        incompleteLanguageCoverage = new IncompleteLanguageCoverage(new InvolvedConcepts());
        incompleteLanguageCoverage.setRepositoryConnection(new RepositoryBuilder().setUpFromTestResource("components.rdf").getConnection());
    }

    @Test
    public void testIncompleteLanguageCoverageCount()
            throws OpenRDFException
    {
        Map<Value, Collection<String>> incompleteLangCoverage = incompleteLanguageCoverage.getResult();
        Assert.assertEquals(13, incompleteLangCoverage.size());
    }

    @Test
    public void testExistResourcesNotHavingEnglishLabels()
            throws OpenRDFException
    {
        Map<Value, Collection<String>> incompleteLangCoverage = incompleteLanguageCoverage.getResult();

        boolean englishTagFound = false;
        for (Collection<String> missingLanguages : incompleteLangCoverage.values()) {
            englishTagFound = missingLanguages.contains("en");
            if (englishTagFound) break;
        }

        Assert.assertFalse(englishTagFound);
    }

    @Test
    public void testResourcesMissingOnlyFrenchLabelsCount()
            throws OpenRDFException
    {
        Map<Value, Collection<String>> incompleteLangCoverage = incompleteLanguageCoverage.getResult();

        List<Value> foundResources = new ArrayList<Value>();
        for (Value resource : incompleteLangCoverage.keySet()) {
            Collection<String> missingLanguages = incompleteLangCoverage.get(resource);
            if (missingLanguages.size() == 1 && missingLanguages.contains("fr")) {
                foundResources.add(resource);
            }
        }

        Assert.assertEquals(2, foundResources.size());
    }
}
