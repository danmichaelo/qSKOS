package at.ac.univie.mminf.qskos4j.repair;

import org.openrdf.repository.RepositoryConnection;

import java.util.List;

public abstract class RepairStrategy {

    protected RepositoryConnection repCon;

    public abstract List<RepairChoice> getRepairChoices();

    public void setRepositoryConnection(RepositoryConnection repCon) {
        this.repCon = repCon;
    }

}
