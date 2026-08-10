package com.abcham.accounts.service;

import com.abcham.accounts.dto.CustomerDetailsDto;

public interface ICustomersService {

    CustomerDetailsDto fetchCustomerDetails(String mobileNumber);

}
