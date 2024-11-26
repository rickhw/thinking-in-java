package com.gtcafe.rws.booter.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gtcafe.rws.booter.dto.AccountDto;
import com.gtcafe.rws.booter.entity.AccountEntity;
import com.gtcafe.rws.booter.exception.ResourceNotFoundException;
import com.gtcafe.rws.booter.payload.request.CreateAccountRequest;
import com.gtcafe.rws.booter.payload.response.RetrieveAccountResponse;
import com.gtcafe.rws.booter.payload.standard.response.StandardResponse;
import com.gtcafe.rws.booter.repository.AccountRepository;

@Service
public class AccountService {

    @Autowired
    private AccountRepository repos;

    @Autowired
    private AccountDto dto;

    public List<AccountEntity> query(String disabled) {
        return repos.findAll();
    }

    public StandardResponse create(CreateAccountRequest request) {

        AccountEntity entity = dto.requestToEntity(request);
        AccountEntity data = repos.save(entity);

        StandardResponse res = new StandardResponse("OK", "Success", data);

        return res;
    }

    public RetrieveAccountResponse retrieveEntityById(Long id) {
        AccountEntity entity = repos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("account not exist with id :" + id));

        RetrieveAccountResponse response = dto.convertToResponse(entity);
        return response;
    }

    public AccountEntity updateEntity(Long id, AccountEntity entityDetails) {

        AccountEntity entity = repos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("account not exist with id :" + id));

        entity.setAccountName(entityDetails.getAccountName());
        entity.setDescription(entityDetails.getDescription());

        AccountEntity updatedEntity = repos.save(entity);

        return updatedEntity;
    }

    public Map<String, Boolean> delete(Long id) {
        AccountEntity entity = repos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("account not exist with id :" + id));

        repos.delete(entity);
        Map<String, Boolean> response = new HashMap<>();
        response.put("deleted", Boolean.TRUE);

        return response;
    }

    public AccountEntity disableEntityById(Long id) {
        AccountEntity entity = repos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("account not exist with id :" + id));

        entity.setDisabled(true);

        AccountEntity updatedEntity = repos.save(entity);

        return updatedEntity;
    }

    public AccountEntity enableEntityById(Long id) {
        AccountEntity entity = repos.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("account not exist with id :" + id));

        entity.setDisabled(false);

        AccountEntity updatedEntity = repos.save(entity);

        return updatedEntity;
    }

}
