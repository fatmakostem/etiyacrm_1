package com.etiyacrm.customerservice.services.dtos.responses.contactMediumResponses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetContactMediumResponse {
    private String email;

    private String mobilePhone;

    private String homePhone;

    private String fax;
}
