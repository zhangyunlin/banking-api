package com.tsb.banking.api;

import com.tsb.banking.api.dto.TransferRequestDto;
import com.tsb.banking.api.dto.TransferResponseDto;
import com.tsb.banking.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * @author zhangyunlin
 *
 * Make a transfer between two accounts belong to a customer
 */
@RestController
@RequestMapping("/transfers")
public class TransferController {

    private final TransferService service;

    public TransferController(TransferService service) {
        this.service = service;
    }

    @Operation(summary = "Make a transfer between two accounts that belong to a customer")
    @PostMapping
    public TransferResponseDto transfer(@Valid @RequestBody TransferRequestDto req) {
        return service.transfer(req);
    }
}
