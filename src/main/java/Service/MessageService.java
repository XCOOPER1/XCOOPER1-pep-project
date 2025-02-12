package Service;

import Model.Message;
import Model.Account;
import Util.ConnectionUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MessageService {

    public Message createMessage(Message message, Optional<Account> account) throws ServiceException {
        if (message.getMessage_text() == null || message.getMessage_text().isEmpty()) {
            throw new ServiceException("Message text cannot be blank.");
        }
        if (message.getMessage_text().length() > 255) {
            throw new ServiceException("Message text cannot exceed 255 characters.");
        }

        try (Connection conn = ConnectionUtil.getConnection()) {
            String sql = "INSERT INTO message (posted_by, message_text, time_posted_epoch) VALUES (?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setInt(1, message.getPosted_by());
            pstmt.setString(2, message.getMessage_text());
            pstmt.setLong(3, message.getTime_posted_epoch());
            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int messageId = rs.getInt(1);
                return new Message(messageId, message.getPosted_by(), message.getMessage_text(), message.getTime_posted_epoch());
            }
        } catch (SQLException e) {
            throw new ServiceException("Failed to create message: " + e.getMessage());
        }
        return null;
    }

    public List<Message> getAllMessages() {
        List<Message> messages = new ArrayList<>();
        try (Connection conn = ConnectionUtil.getConnection()) {
            String sql = "SELECT * FROM message";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                messages.add(new Message(
                    rs.getInt("message_id"),
                    rs.getInt("posted_by"),
                    rs.getString("message_text"),
                    rs.getLong("time_posted_epoch")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    public Optional<Message> getMessageById(int id) {
        try (Connection conn = ConnectionUtil.getConnection()) {
            String sql = "SELECT * FROM message WHERE message_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Message message = new Message(
                    rs.getInt("message_id"),
                    rs.getInt("posted_by"),
                    rs.getString("message_text"),
                    rs.getLong("time_posted_epoch")
                );
                return Optional.of(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public Message updateMessage(Message mappedMessage) throws ServiceException {
        if (mappedMessage.getMessage_text() == null || mappedMessage.getMessage_text().isEmpty()) {
            throw new ServiceException("Message text cannot be blank.");
        }
        if (mappedMessage.getMessage_text().length() > 255) {
            throw new ServiceException("Message text cannot exceed 255 characters.");
        }

        try (Connection conn = ConnectionUtil.getConnection()) {
            String checkSql = "SELECT * FROM message WHERE message_id = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setInt(1, mappedMessage.getMessage_id());
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                throw new ServiceException("Message not found.");
            }

            String updateSql = "UPDATE message SET message_text = ? WHERE message_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            updateStmt.setString(1, mappedMessage.getMessage_text());
            updateStmt.setInt(2, mappedMessage.getMessage_id());
            updateStmt.executeUpdate();

            return new Message(
                mappedMessage.getMessage_id(),
                rs.getInt("posted_by"),
                mappedMessage.getMessage_text(),
                rs.getLong("time_posted_epoch")
            );
        } catch (SQLException e) {
            throw new ServiceException("Failed to update message: " + e.getMessage());
        }
    }

    public void deleteMessage(Message message) throws ServiceException {
        try (Connection conn = ConnectionUtil.getConnection()) {
            String deleteSql = "DELETE FROM message WHERE message_id = ?";
            PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
            deleteStmt.setInt(1, message.getMessage_id());
            deleteStmt.executeUpdate();
        } catch (SQLException e) {
            throw new ServiceException("Failed to delete message: " + e.getMessage());
        }
    }

    public List<Message> getMessagesByAccountId(int accountId) {
        List<Message> messages = new ArrayList<>();
        try (Connection conn = ConnectionUtil.getConnection()) {
            String sql = "SELECT * FROM message WHERE posted_by = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                messages.add(new Message(
                    rs.getInt("message_id"),
                    rs.getInt("posted_by"),
                    rs.getString("message_text"),
                    rs.getLong("time_posted_epoch")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }
}
