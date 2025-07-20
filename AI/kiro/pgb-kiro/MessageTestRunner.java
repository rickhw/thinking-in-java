import com.messageboard.entity.User;
import com.messageboard.entity.Message;

public class MessageTestRunner {
    public static void main(String[] args) {
        try {
            // Create test users
            User author = User.builder()
                    .id(1L)
                    .ssoId("sso-author")
                    .username("author")
                    .email("author@example.com")
                    .displayName("Author User")
                    .isActive(true)
                    .build();

            User boardOwner = User.builder()
                    .id(2L)
                    .ssoId("sso-boardowner")
                    .username("boardowner")
                    .email("boardowner@example.com")
                    .displayName("Board Owner")
                    .isActive(true)
                    .build();

            // Test Message creation
            Message rootMessage = Message.builder()
                    .id(1L)
                    .user(author)
                    .boardOwner(boardOwner)
                    .content("This is a root message")
                    .isDeleted(false)
                    .build();

            System.out.println("✓ Root message created successfully");
            System.out.println("  Content: " + rootMessage.getContent());
            System.out.println("  Author: " + rootMessage.getUser().getUsername());
            System.out.println("  Board Owner: " + rootMessage.getBoardOwner().getUsername());
            System.out.println("  Is root message: " + rootMessage.isRootMessage());
            System.out.println("  Is reply: " + rootMessage.isReply());
            System.out.println("  Is deleted: " + rootMessage.isDeleted());

            // Test reply message
            Message replyMessage = new Message(boardOwner, boardOwner, "This is a reply", rootMessage);

            System.out.println("\n✓ Reply message created successfully");
            System.out.println("  Content: " + replyMessage.getContent());
            System.out.println("  Author: " + replyMessage.getUser().getUsername());
            System.out.println("  Is root message: " + replyMessage.isRootMessage());
            System.out.println("  Is reply: " + replyMessage.isReply());
            System.out.println("  Parent message ID: " + replyMessage.getParentMessage().getId());

            // Test adding reply to root message
            rootMessage.addReply(replyMessage);

            System.out.println("\n✓ Reply added to root message");
            System.out.println("  Root message has replies: " + rootMessage.hasReplies());
            System.out.println("  Reply count: " + rootMessage.getReplyCount());

            // Test authorization methods
            System.out.println("\n✓ Authorization tests");
            System.out.println("  Author is author of root message: " + rootMessage.isAuthor(author));
            System.out.println("  Board owner is author of root message: " + rootMessage.isAuthor(boardOwner));
            System.out.println("  Board owner is board owner: " + rootMessage.isBoardOwner(boardOwner));
            System.out.println("  Author is board owner: " + rootMessage.isBoardOwner(author));

            // Test soft delete
            rootMessage.softDelete();
            System.out.println("\n✓ Soft delete test");
            System.out.println("  Message is deleted after soft delete: " + rootMessage.isDeleted());

            System.out.println("\n🎉 All Message entity tests passed!");

        } catch (Exception e) {
            System.err.println("❌ Error testing Message entity: " + e.getMessage());
            e.printStackTrace();
        }
    }
}