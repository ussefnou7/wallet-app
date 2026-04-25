package com.wallet.walletapp.support.dto;

import com.wallet.walletapp.support.SupportTicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateSupportTicketRequest {

    @NotBlank
    @Size(max = 255)
    private String subject;

    @NotBlank
    @Size(max = 4000)
    private String description;

    private SupportTicketPriority priority = SupportTicketPriority.MEDIUM;
}
