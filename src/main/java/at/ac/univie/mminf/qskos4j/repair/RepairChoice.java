package at.ac.univie.mminf.qskos4j.repair;

public abstract interface RepairChoice {

    public void performRepair() throws RepairFailedException;

    public String getMessage();
}
