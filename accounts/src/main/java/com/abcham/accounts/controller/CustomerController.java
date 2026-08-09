package com.abcham.accounts.controller;

import com.abcham.accounts.dto.CustomerDetailsDto;
import com.abcham.accounts.dto.ErrorResponseDto;
import com.abcham.accounts.service.ICustomersService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "REST API for Customers in SecuredBank",
        description = "REST APIs in SecuredBank to FETCH customer details"
)
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/accounts/customers", produces = {MediaType.APPLICATION_JSON_VALUE})
@Slf4j
public class CustomerController {

    private final ICustomersService iCustomersService;

    @Operation(
            summary = "Fetch Customer Details REST API",
            description = "REST API to fetch Customer details based on a mobile number"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "HTTP Status OK"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "HTTP Status Internal Server Error",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponseDto.class)
                    )
            )
    }
    )
    @GetMapping("/fetchCustomerDetails")
    public ResponseEntity<CustomerDetailsDto> fetchCustomerDetails(@RequestHeader("securedbank-correlation-id") String correlationId,
                                                                   @RequestParam
                                                                   @Pattern(regexp = "(^$|[0-9]{10})", message = "Mobile number must be 10 digits")
                                                                   String mobileNumber) {

        log.debug("Correlation ID: {}, Fetching customer details for mobile number: {}", correlationId, mobileNumber);
        CustomerDetailsDto customerDetailsDto = iCustomersService.fetchCustomerDetails(correlationId, mobileNumber);
        return ResponseEntity.ok(customerDetailsDto);

    }

}
