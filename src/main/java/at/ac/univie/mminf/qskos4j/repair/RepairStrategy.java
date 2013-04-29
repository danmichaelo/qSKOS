package at.ac.univie.mminf.qskos4j.repair;

import org.openrdf.repository.RepositoryConnection;

public abstract class RepairStrategy {

    protected RepositoryConnection repCon;

    public abstract void performRepair();

    public void setRepositoryConnection(RepositoryConnection repCon) {
        this.repCon = repCon;
    }

}
