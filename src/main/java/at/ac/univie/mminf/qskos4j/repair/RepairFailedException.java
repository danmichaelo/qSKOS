package at.ac.univie.mminf.qskos4j.repair;

public class RepairFailedException extends RuntimeException {

    public RepairFailedException(String message) {
        super(message);
    }

    public RepairFailedException(Throwable cause) {
        super(cause);
    }


}
