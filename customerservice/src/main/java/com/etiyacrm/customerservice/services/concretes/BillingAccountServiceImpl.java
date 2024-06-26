package com.etiyacrm.customerservice.services.concretes;

import com.etiya.common.events.billingAccounts.BillingAccountCreatedEvent;
import com.etiya.common.events.billingAccounts.BillingAccountUpdatedEvent;
import com.etiyacrm.customerservice.core.business.paging.PageInfo;
import com.etiyacrm.customerservice.core.business.paging.PageInfoResponse;
import com.etiyacrm.customerservice.entities.BillingAccount;
import com.etiyacrm.customerservice.kafka.producers.billingAccounts.BillingAccountProducer;
import com.etiyacrm.customerservice.repositories.BillingAccountRepository;
import com.etiyacrm.customerservice.services.abstracts.BillingAccountService;
import com.etiyacrm.customerservice.services.dtos.requests.billingAccountRequests.CreateBillingAccountRequest;
import com.etiyacrm.customerservice.services.dtos.requests.billingAccountRequests.UpdateBillingAccountRequest;
import com.etiyacrm.customerservice.services.dtos.responses.billingAccountResponses.*;
import com.etiyacrm.customerservice.services.mappers.BillingAccountMapper;
import com.etiyacrm.customerservice.services.rules.BillingAccountBusinessRules;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BillingAccountServiceImpl implements BillingAccountService {
    private BillingAccountRepository billingAccountRepository;
    private BillingAccountBusinessRules billingAccountBusinessRules;
    private BillingAccountProducer billingAccountProducer;
    @Override
    public PageInfoResponse<GetAllBillingAccountResponse> getAllPage(PageInfo pageInfo) {
        Pageable pageable = PageRequest.of(pageInfo.getPage(), pageInfo.getSize());

        Page<BillingAccount> response = billingAccountRepository.findAll(pageable);
        Page<GetAllBillingAccountResponse> responsePage =response.map(billingAccount ->
            BillingAccountMapper.INSTANCE.getAllBillingAccountResponse(billingAccount));
        return new PageInfoResponse<>(responsePage);
    }

    @Override
    public List<GetAllBillingAccountResponse> getAll() {
        List<BillingAccount> response = billingAccountRepository.findAll();
        List<GetAllBillingAccountResponse> responsePage = response.stream()
                .map(billingAccount -> BillingAccountMapper.INSTANCE.getAllBillingAccountResponse(billingAccount))
                .collect(Collectors.toList());
        return responsePage;
    }

    @Override
    public List<GetBillingAccountResponse> getById(String id) {
        List<BillingAccount> billingAccounts = billingAccountRepository.findByCustomerId(id);
        List<GetBillingAccountResponse> getBillingAccountList =
                billingAccounts.stream().map(BillingAccountMapper.INSTANCE::getBillingAccountResponseFromBillingAccount).collect(Collectors.toList());

        return getBillingAccountList;
    }

    @Override
    public CreatedBillingAccountResponse add(CreateBillingAccountRequest createBillingAccountRequest) {
        billingAccountBusinessRules.checkIfCustomerAddressExists(createBillingAccountRequest);

        BillingAccount billingAccount =
                BillingAccountMapper.INSTANCE.billingAccountFromCreateBillingAccountRequest(createBillingAccountRequest);
        String accountNumber = generateRandomNumber();
        billingAccount.setAccountNumber(accountNumber);
        BillingAccount createdBillingAccount =
                billingAccountRepository.save(billingAccount);

        CreatedBillingAccountResponse createdBillingAccountResponse =
                BillingAccountMapper.INSTANCE.createdBillingAccountResponseFromBillingAccount(createdBillingAccount);

        BillingAccountCreatedEvent billingAccountCreatedEvent = new BillingAccountCreatedEvent();
        billingAccountCreatedEvent.setId(createdBillingAccount.getCustomer().getId());
        billingAccountCreatedEvent.setAccountNumber(createdBillingAccount.getAccountNumber());
        billingAccountProducer.sendMessage(billingAccountCreatedEvent);

        return createdBillingAccountResponse;
    }

    @Override
    public UpdatedBillingAccountResponse update(UpdateBillingAccountRequest updateBillingAccountRequest, String id) {

        BillingAccount savedBillingAccount = billingAccountRepository.findById(id).get();

        BillingAccount billingAccount = BillingAccountMapper.INSTANCE.billingAccountFromUpdateBillingAccountRequest(updateBillingAccountRequest);

        billingAccount.setId(id);
        billingAccount.setCustomer(savedBillingAccount.getCustomer());
        billingAccount.setUpdatedDate(LocalDateTime.now());
        BillingAccount updatedBillingAccount = billingAccountRepository.save(billingAccount);

        UpdatedBillingAccountResponse updatedBillingAccountResponse =
                BillingAccountMapper.INSTANCE.updatedBillingAccountResponseFromBillingAccount(updatedBillingAccount);

        BillingAccountUpdatedEvent billingAccountUpdatedEvent = new BillingAccountUpdatedEvent();
        billingAccountUpdatedEvent.setId(updatedBillingAccount.getId());
        billingAccountUpdatedEvent.setAccountNumber(updatedBillingAccount.getAccountNumber());
        billingAccountProducer.sendMessage(billingAccountUpdatedEvent);

        return updatedBillingAccountResponse;
    }

    @Override
    public DeletedBillingAccountResponse delete(String id) {
        BillingAccount billingAccount = billingAccountRepository.findById(id).get();
        //rule
        billingAccount.setId(id);
        billingAccount.setDeletedDate(LocalDateTime.now());
        BillingAccount deletedBillingAccount = billingAccountRepository.save(billingAccount);

        DeletedBillingAccountResponse deletedBillingAccountResponse =
                BillingAccountMapper.INSTANCE.deletedBillingAccountResponseFromBillingAccount(deletedBillingAccount);
        deletedBillingAccountResponse.setDeletedDate(deletedBillingAccount.getDeletedDate());
        return deletedBillingAccountResponse;
    }

    public static String generateRandomNumber() {
        Random rand = new Random();
        String randomNumber = "";
        for (int i = 0; i < 11; i++) {
            int digit = rand.nextInt(10);
            randomNumber += Integer.toString(digit);
        }
        return randomNumber;
    }
}
