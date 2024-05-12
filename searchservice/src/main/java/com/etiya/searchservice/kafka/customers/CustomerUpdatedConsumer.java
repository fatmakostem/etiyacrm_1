package com.etiya.searchservice.kafka.customers;

import com.etiya.common.events.customers.CustomerCreatedEvent;
import com.etiya.common.events.customers.CustomerUpdatedEvent;
import com.etiya.searchservice.entities.Customer;
import com.etiya.searchservice.service.abstracts.FilterService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
public class CustomerUpdatedConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerCreatedConsumer.class);
    private FilterService filterService;

    @KafkaListener(topics = "customer-updated", groupId = "update-customer")
    private void consume(CustomerUpdatedEvent customerUpdatedEvent){
        Customer customer = new Customer();
        customer.setNationalityIdentity(customerUpdatedEvent.getNationalityIdentity());
        customer.setId(customerUpdatedEvent.getId());
        customer.setMobilePhone(customerUpdatedEvent.getMobilePhone());
        customer.setFirstName(customerUpdatedEvent.getFirstName());
        customer.setLastName(customerUpdatedEvent.getLastName());

        LOGGER.info(String.format("Customer updated event consumer => %s", customerUpdatedEvent.toString()));
        this.filterService.updateCustomer(customer);
    }
}
