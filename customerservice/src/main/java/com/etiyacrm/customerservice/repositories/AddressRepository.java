package com.etiyacrm.customerservice.repositories;

import com.etiyacrm.customerservice.entities.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AddressRepository extends JpaRepository<Address,Long> {
    List<Address> findByDeletedDate(LocalDateTime deletedDate);
}