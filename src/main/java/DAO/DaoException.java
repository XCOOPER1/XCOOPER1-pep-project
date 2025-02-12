package DAO;

/**
 * DaoException is a custom unchecked exception class that encapsulates and
 * handles exceptions that may occur within the DAO layer.
 * Extending RuntimeException allows DaoException to be an unchecked exception,
 * which does not have to be declared in method signatures
 * or explicitly caught in the code.
 */
public class DaoException extends RuntimeException {

    /**
     * Serialization in Java is the process of converting an object into a byte
     * stream
     * which can be saved to memory, a database, or a file, or can be transmitted
     * over the network.
     * Its primary purpose is to preserve the state of an object for later use.
     * The reverse process of converting the byte stream back into its original
     * object is called deserialization.
     *
     * serialVersionUID is a version control identifier used in serialization and
     * deserialization.
     * It's essential for ensuring that the sender and receiver of a serialized
     * object
     * maintain a compatible version of the class. Incompatibility in this version
     * ID
     * between the sender and receiver will lead to an InvalidClassException during
     * deserialization.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new DaoException with the specified error message.
     *
     * @param message The detailed message for the exception. This is saved for
     *                later retrieval by the getMessage() method.
     */
    public DaoException(String message) {
        super(message);
    }

    /**
     * Constructs a new DaoException with the specified error message and cause.
     * Note that the detail message associated with cause is not automatically
     * incorporated into this exception's detail message.
     *
     * @param message The detailed message for the exception. This is saved for
     *                later retrieval by the getMessage() method.
     * @param cause   The cause of the exception (which is saved for later retrieval
     *                by the getCause() method).
     *                A null value is permitted, and indicates that the cause is
     *                nonexistent or
     *                unknown.
     */
    public DaoException(String message, Throwable cause) {
        super(message, cause);
    }
}