package Controller;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import Model.Account;
import Model.Message;
import Service.AccountService;
import Service.MessageService;
import Service.ServiceException;
import io.javalin.Javalin;
import io.javalin.http.Context;

public class SocialMediaController {

    private final AccountService accountService;
    private final MessageService messageService;

    public SocialMediaController() {
        this.accountService = new AccountService();
        this.messageService = new MessageService();
    }

    public Javalin startAPI() {
        Javalin app = Javalin.create();
        app.post("/register", this::registerAccount);
        app.post("/login", this::loginAccount);
        app.post("/messages", this::createMessage);
        app.get("/messages", this::getAllMessages);
        app.get("/messages/{message_id}", this::getMessageById);
        app.delete("/messages/{message_id}", this::deleteMessageById);
        app.patch("/messages/{message_id}", this::updateMessageById);
        app.get("/accounts/{account_id}/messages", this::getMessagesByAccountId);

        return app;
    }

    private void registerAccount(Context ctx) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Account account = mapper.readValue(ctx.body(), Account.class);
            Account registeredAccount = accountService.createAccount(account);
            ctx.json(registeredAccount);
        } catch (ServiceException | JsonProcessingException e) {
            ctx.status(400);
        }
    }

    private void loginAccount(Context ctx) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Account account = mapper.readValue(ctx.body(), Account.class);
            Optional<Account> loggedInAccount = accountService.validateLogin(account);
            if (loggedInAccount.isPresent()) {
                ctx.json(loggedInAccount.get());
            } else {
                ctx.status(401);
            }
        } catch (ServiceException | JsonProcessingException e) {
            ctx.status(401);
        }
    }

    private void createMessage(Context ctx) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Message message = mapper.readValue(ctx.body(), Message.class);
            Optional<Account> account = accountService.getAccountById(message.getPosted_by());
            if (account.isPresent()) {
                Message createdMessage = messageService.createMessage(message, account);
                ctx.json(createdMessage);
            } else {
                ctx.status(400);
            }
        } catch (ServiceException | JsonProcessingException e) {
            ctx.status(400);
        }
    }

    private void getAllMessages(Context ctx) {
        List<Message> messages = messageService.getAllMessages();
        ctx.json(messages);  // Return an empty array [] if no messages exist
    }
    

    private void getMessageById(Context ctx) {
        try {
            int messageId = Integer.parseInt(ctx.pathParam("message_id"));
            Optional<Message> message = messageService.getMessageById(messageId);
            if (message.isPresent()) {
                ctx.json(message.get());
            } else {
                ctx.status(200);
            }
        } catch (NumberFormatException e) {
            ctx.status(400);
        }
    }

    private void deleteMessageById(Context ctx) {
        try {
            int messageId = Integer.parseInt(ctx.pathParam("message_id"));
            Optional<Message> message = messageService.getMessageById(messageId);
            if (message.isPresent()) {
                messageService.deleteMessage(message.get());
                ctx.json(message.get()); // Return the deleted message as JSON
                ctx.status(200);  // Return 200 status code for successful deletion
            } else {
                ctx.status(200);  // Return 200 with empty body if message not found
            }
        } catch (ServiceException e) {
            ctx.status(400);  // Handle error case
        } catch (NumberFormatException e) {
            ctx.status(400);  // Invalid message ID format
        }
    }
    
    
    
    private void updateMessageById(Context ctx) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            int messageId = Integer.parseInt(ctx.pathParam("message_id"));
            Message updatedMessage = mapper.readValue(ctx.body(), Message.class);
            updatedMessage.setMessage_id(messageId);
            Message message = messageService.updateMessage(updatedMessage);
            ctx.json(message);
        } catch (NumberFormatException | JsonProcessingException e) {
            ctx.status(400);
        } catch (ServiceException e) {
            ctx.status(400);
        }
    }

    private void getMessagesByAccountId(Context ctx) {
        try {
            int accountId = Integer.parseInt(ctx.pathParam("account_id"));
            List<Message> messages = messageService.getMessagesByAccountId(accountId);
            ctx.json(messages);
        } catch (NumberFormatException e) {
            ctx.status(400);
        }
    }
}
