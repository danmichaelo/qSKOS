package at.ac.univie.mminf.qskos4j.repairstrategies;

import at.ac.univie.mminf.qskos4j.issues.language.OmittedOrInvalidLanguageTags;
import at.ac.univie.mminf.qskos4j.repair.LanguageTagRepairStrategy;
import at.ac.univie.mminf.qskos4j.repair.RepairChoice;
import at.ac.univie.mminf.qskos4j.util.vocab.RepositoryBuilder;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryConnection;

import java.io.IOException;

public class LanguageTagRepairStrategyTest {

    private RepositoryConnection repCon;
    private OmittedOrInvalidLanguageTags omittedOrInvalidLanguageTags;

    @Before
    public void setUp() throws OpenRDFException, IOException {
        omittedOrInvalidLanguageTags = new OmittedOrInvalidLanguageTags();
        repCon = new RepositoryBuilder().setUpFromTestResource("components.rdf").getConnection();
        omittedOrInvalidLanguageTags.setRepositoryConnection(repCon);
    }

    @Test
    public void applyRepairStrategies() throws OpenRDFException {
        LanguageTagRepairStrategy ltrs = new LanguageTagRepairStrategy(omittedOrInvalidLanguageTags.getPreparedData());
        ltrs.setRepositoryConnection(repCon);

        for (RepairChoice repairChoice : ltrs.getRepairChoices()) {
            repairChoice.performRepair();
        }

        omittedOrInvalidLanguageTags.reset();
        Assert.assertTrue(omittedOrInvalidLanguageTags.getPreparedData().isEmpty());
    }
}
