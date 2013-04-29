package at.ac.univie.mminf.qskos4j.repair;

import org.openrdf.model.Statement;

import java.util.Collection;

public class LanguageTagRepairStrategy extends RepairStrategy {

    private Collection<Statement> invalidLangTagStatements;

    public LanguageTagRepairStrategy(Collection<Statement> invalidLangTagStatements) {
        this.invalidLangTagStatements = invalidLangTagStatements;
    }

    @Override
    public void performRepair() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

}
