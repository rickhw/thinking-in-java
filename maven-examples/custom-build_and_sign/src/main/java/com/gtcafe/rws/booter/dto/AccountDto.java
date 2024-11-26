package com.gtcafe.rws.booter.dto;

import java.util.Date;

import org.springframework.stereotype.Component;

import com.gtcafe.rws.booter.entity.AccountEntity;
import com.gtcafe.rws.booter.entity.EAccountState;
import com.gtcafe.rws.booter.payload.request.CreateAccountRequest;
import com.gtcafe.rws.booter.payload.response.RetrieveAccountResponse;


//          | request | response | entity
// request  |    -    |   -      | requestToEntity
// response |    -    |   -
// entity   |         | entityToResponse |
@Component
public class AccountDto {

    public RetrieveAccountResponse convertToResponse(AccountEntity entity) {
        RetrieveAccountResponse response = new RetrieveAccountResponse();
        response.setId(entity.getId());
        response.setAccountName(entity.getAccountName());
        response.setState(entity.getState().name());

        return response;
    }

    public AccountEntity requestToEntity(CreateAccountRequest request) {
        AccountEntity entity = new AccountEntity(request.getAccountName(), request.getDescription());
        entity.setState(EAccountState.ACTIVE);
        entity.setCreatedAt(new Date());

        return entity;
    }
}
