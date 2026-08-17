package com.creatoros.service;

import tools.jackson.databind.JsonNode;

public interface RazorpayWebhookService {

    /** {@code payload} is the already signature-verified webhook body, parsed to a JSON tree. */
    void handleEvent(JsonNode payload);
}
