package com.gtcafe.rws.booter.service;

import java.util.Date;
import java.util.UUID;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gtcafe.rws.booter.entity.ETaskState;
import com.gtcafe.rws.booter.entity.TaskHash;
import com.gtcafe.rws.booter.payload.request.CreateTaskRequest;
import com.gtcafe.rws.booter.payload.response.CreateTaskResponse;
import com.gtcafe.rws.booter.payload.response.RetrieveTaskResponse;
import com.gtcafe.rws.booter.repository.TaskRepository;

@Service
public class TaskService {

    @Autowired
	private AmqpTemplate rabbitTemplate;

	@Value("${app.rabbitmq.exchange}")
	private String exchange;

	@Value("${app.rabbitmq.routingkey}")
	private String routingkey;


    @Autowired
    private TaskRepository repos;

    public CreateTaskResponse create(CreateTaskRequest request) {
        String taskId = UUID.randomUUID().toString();
        Date createdAt = new Date();

        CreateTaskResponse res = new CreateTaskResponse();
        res.setId(taskId);
        res.setCraetedAt(createdAt);
        res.setState(ETaskState.CREATING.name());
        res.setData(request.getData());

        // 1. write to redis
        System.out.println(String.format("1. write to redis"));
        TaskHash hash = new TaskHash(taskId, request.getData(), ETaskState.CREATING);
        hash.setCreatedAt(createdAt);
        repos.save(hash);
        System.out.println(String.format("  - TaskId: [%s]", taskId));
        System.out.println(String.format("  - createdAt: [%s]", createdAt));

        // 2. sent to queue
        System.out.println(String.format("2. sent to queue"));
    	// rabbitTemplate.convertAndSend(exchange, routingkey, request.getData());
        rabbitTemplate.convertAndSend(exchange, routingkey, res);
        System.out.println(String.format("  exchange: [%s], routingKey: [%s]", exchange, routingkey));

        // 3. update states
        System.out.println(String.format("3. update states"));
        TaskHash taskHash = repos.findById(taskId).get();
        System.out.println(String.format("  - hash: [%s]", taskHash.getId()));
        taskHash.setState(ETaskState.QUEUE);
        repos.save(taskHash);

        return res;
    }

    public RetrieveTaskResponse retrieveEntityById(String taskId) {

        TaskHash taskHash = repos.findById(taskId).get();
        RetrieveTaskResponse response = new RetrieveTaskResponse();

        response.setId(taskHash.getId());
        response.setState(taskHash.getState().name());
        response.setCraetedAt(taskHash.getCreatedAt());
        response.setFinishedAt(taskHash.getFinishedAt());

        return response;
    }


}
