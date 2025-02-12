package DAO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import Model.Message;
import Util.ConnectionUtil;

/**
 * This class implements the DAO for the Message table in the SocialMedia.sql
 * database.
 * It provides the CRUD (Create, Retrieve, Update, Delete) operations for
 * messages.
 */

public class MessageDao implements BaseDao<Message> {

    // Create a Logger instance for this class.
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageDao.class);

    /**
     * Helper method to log SQLException details and throw a DaoException with a
     * custom error message
     *
     * @param e            The SQLException that occurred.
     * @param sql          The SQL statement that was being executed when the
     *                     SQLException occurred.
     * @param errorMessage The custom error message to use for the DaoException.
     */
    private void handleSQLException(SQLException e, String sql, String errorMessage) {
        LOGGER.error("SQLException Details: {}", e.getMessage());
        LOGGER.error("SQL State: {}", e.getSQLState());
        LOGGER.error("Error Code: {}", e.getErrorCode());
        LOGGER.error("SQL: {}", sql);
        throw new DaoException(errorMessage, e);
    }

    /**
     * Retrieve a specific message by its ID from the database
     *
     * @param id The ID of the message to retrieve.
     * @return An Optional containing the message if found; otherwise, an empty
     *         Optional.
     */
    @Override
    public Optional<Message> getById(int id) {
        // The SQL string is outside the try block as it doesn't require closure like
        // Connection, PreparedStatement, or ResultSet.
        String sql = "SELECT * FROM message WHERE message_id = ?";
        Connection conn = ConnectionUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            // ResultSet is in a separate try block to ensure it gets closed after use,
            // even if an exception is thrown during data processing.
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMessage(rs));
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while retrieving the message with id: " + id);
        }
        return Optional.empty();
    }

    /**
     * Retrieves all messages from the database
     *
     * @return A List of all messages in the database.
     */
    @Override
    public List<Message> getAll() {
        String sql = "SELECT * FROM message";
        Connection conn = ConnectionUtil.getConnection();
        List<Message> messages = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    messages.add(mapResultSetToMessage(rs));
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while retrieving all messages");
        }
        return messages;
    }

    /**
     * Retrieves all messages posted by a specific account from the database
     *
     * @param accountId The ID of the account whose messages to retrieve.
     * @return A List of all messages posted by the specified account.
     */
    public List<Message> getMessagesByAccountId(int accountId) {
        String sql = "SELECT * FROM message WHERE posted_by = ?";
        Connection conn = ConnectionUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                return mapResultSetToList(rs);
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while retrieving a message by account ID: " + accountId);
        }
        return new ArrayList<>();
    }

    /**
     * Insert a new message into the database
     *
     * @param message The message to insert.
     * @return The inserted message, which may include modifications made by the
     *         database, such as an auto-generated ID.
     */
    @Override
    public Message insert(Message message) {
        String sql = "INSERT INTO message(posted_by, message_text, time_posted_epoch) VALUES (?, ?, ?)";
        Connection conn = ConnectionUtil.getConnection();

        // INSERT operation on a table with an auto-incrementing primary key column
        // Database assigns a unique value to the primary key column for the newly
        // inserted row
        // The generatedKeys feature enables us to retrieve the generated key value
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, message.getPosted_by());
            ps.setString(2, message.getMessage_text());
            ps.setLong(3, message.getTime_posted_epoch());

            ps.executeUpdate();

            // After executing the INSERT statement using ps.executeUpdate()
            // The ResultSet object named generatedKeys is obtained by calling
            // ps.getGeneratedKeys()
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {

                // Check if any keys were generated by iterating over the ResultSet using
                // generatedKeys.next()
                if (generatedKeys.next()) {

                    // By iterating over the ResultSet using generatedKeys.next(), we can access
                    // the generated key value(s). In this case, since we expect only one key
                    // (the ID of the inserted message), we use generatedKeys.getInt(1) to retrieve
                    // the value of the first column in the result set, which represents the
                    // generated ID.

                    // Retrieve the generated ID
                    int generatedId = generatedKeys.getInt(1);

                    // Finally, the retrieved ID is used to create a new Message object, combining
                    // it with the other attributes of the inserted message.

                    // Create a new Message object with the generated ID and other attributes
                    return new Message(generatedId, message.getPosted_by(), message.getMessage_text(),
                            message.getTime_posted_epoch());
                } else {
                    throw new DaoException("Failed to insert message, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while inserting a message");
        }
        throw new DaoException("Failed to insert message");
    }

    /**
     * Update an existing message in the database
     *
     * @param message The message to update.
     * @return true if the update was successful; false if the message was not found
     *         in the database.
     */
    @Override
    public boolean update(Message message) {
        String sql = "UPDATE message SET posted_by = ?, message_text = ?, time_posted_epoch = ? WHERE message_id = ?";
        int rowsUpdated = 0;
        Connection conn = ConnectionUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, message.getPosted_by());
            ps.setString(2, message.getMessage_text());
            ps.setLong(3, message.getTime_posted_epoch());
            ps.setInt(4, message.getMessage_id());
            rowsUpdated = ps.executeUpdate();
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while updating the message with id: " + message.getMessage_id());
        }
        return rowsUpdated > 0;
    }

    /**
     * Delete a message from the database
     *
     * @param message The message to delete.
     * @return true if the deletion was successful; false if the message was not
     *         found in the database.
     */
    @Override
    public boolean delete(Message message) {
        String sql = "DELETE FROM message WHERE message_id = ?";
        int rowsUpdated = 0;
        Connection conn = ConnectionUtil.getConnection();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, message.getMessage_id());
            rowsUpdated = ps.executeUpdate();
        } catch (SQLException e) {
            handleSQLException(e, sql, "Error while deleting the message with id: " + message.getMessage_id());
        }
        return rowsUpdated > 0;
    }

    /**
     * Helper method to convert a ResultSet row into a Message object
     *
     * @param rs The ResultSet containing the row to convert.
     * @return The converted Message object.
     * @throws SQLException If an error occurs while processing the ResultSet.
     */
    private Message mapResultSetToMessage(ResultSet rs) throws SQLException {
        int messageId = rs.getInt("message_id");
        int postedBy = rs.getInt("posted_by");
        String messageText = rs.getString("message_text");
        long timePostedEpoch = rs.getLong("time_posted_epoch");
        return new Message(messageId, postedBy, messageText, timePostedEpoch);
    }

    /**
     * Transforms a ResultSet into a List of Message objects.
     * This helper method allows for the convenient transformation of data returned
     * from a SQL query into
     * Java-friendly Message objects. It assumes that the ResultSet contains rows
     * where each row represents a Message.
     *
     * @param rs The ResultSet from a SQL query that needs to be transformed into
     *           Message objects.
     * @return A List of Message objects that represent the data from the ResultSet.
     * @throws SQLException If a database access error occurs or this method is
     *                      called on a closed ResultSet.
     */
    private List<Message> mapResultSetToList(ResultSet rs) throws SQLException {
        List<Message> messages = new ArrayList<>();
        while (rs.next()) {
            messages.add(mapResultSetToMessage(rs));
        }
        return messages;
    }
}