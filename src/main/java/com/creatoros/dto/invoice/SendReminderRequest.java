package com.creatoros.dto.invoice;

import jakarta.validation.constraints.Pattern;

public record SendReminderRequest(
        @Pattern(regexp = "email|whatsapp", message = "Channel must be email or whatsapp")
        String channel,

        @Pattern(regexp = "polite|firm|urgent", message = "Tone must be polite, firm or urgent")
        String tone) {
}
