package com.example.messageboard.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import com.gtcafe.messageboard.model.Message;
import com.gtcafe.messageboard.model.Task;
import com.gtcafe.messageboard.model.TaskStatus;
import com.gtcafe.messageboard.repository.MessageRepository;
import com.gtcafe.messageboard.service.MessageService;
import com.gtcafe.messageboard.service.TaskService;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;

@SpringBootTest
@CucumberContextConfiguration
@ActiveProfiles("test") // 可以使用一個專門的測試 profile
@Transactional // 確保每個 Scenario 都是獨立的事務
public class MessageManagementSteps {

    @Autowired
    private MessageService messageService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private MessageRepository messageRepository; // 用於設置初始數據

    private String latestTaskId;
    private Page<Message> retrievedMessagesPage;

    @Given("the message board is running")
    public void theMessageBoardIsRunning() {
        // SpringBootTest 已經確保應用程式上下文已啟動
        assertNotNull(messageService);
        assertNotNull(taskService);
    }

    @When("a user {string} creates a message with content {string}")
    public void aUserCreatesAMessageWithContent(String userId, String content) throws ExecutionException, InterruptedException, TimeoutException {
        Message message = new Message();
        message.setUserId(userId);
        message.setContent(content);
        latestTaskId = messageService.createMessage(message).get(5, TimeUnit.SECONDS); // 等待非同步任務完成
        assertNotNull(latestTaskId);
    }

    @Then("the message should be successfully created")
    public void theMessageShouldBeSuccessfullyCreated() {
        // 由於是非同步，我們需要檢查任務狀態
        Task task = taskService.getTask(latestTaskId).orElse(null);
        assertNotNull(task);
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
    }

    @Then("the task status for message creation should be COMPLETED")
    public void theTaskStatusForMessageCreationShouldBeCOMPLETED() {
        Task task = taskService.getTask(latestTaskId).orElse(null);
        assertNotNull(task);
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
    }

    @Given("the message board has messages from {string} and {string}")
    public void theMessageBoardHasMessagesFromAnd(String userA, String userB, io.cucumber.datatable.DataTable dataTable) {
        List<List<String>> rows = dataTable.asLists(String.class);
        for (int i = 1; i < rows.size(); i++) { // Skip header row
            List<String> columns = rows.get(i);
            Message message = new Message();
            message.setUserId(columns.get(0));
            message.setContent(columns.get(1));
            messageRepository.save(message); // 直接保存，不走非同步服務
        }
    }

    @When("I request messages for user {string} with page {int} and size {int}")
    public void iRequestMessagesForUserWithPageAndSize(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        retrievedMessagesPage = messageService.getMessagesByUserId(userId, pageable);
    }

    @Then("I should receive {int} messages")
    public void iShouldReceiveMessages(int expectedCount) {
        assertNotNull(retrievedMessagesPage);
        assertEquals(expectedCount, retrievedMessagesPage.getContent().size());
    }

    @Then("the first message content should be {string}")
    public void theFirstMessageContentShouldBe(String expectedContent) {
        assertFalse(retrievedMessagesPage.getContent().isEmpty());
        assertEquals(expectedContent, retrievedMessagesPage.getContent().get(0).getContent());
    }
}