package com.cit.transfer;


import com.cit.model.Event;

@lombok.Data
@lombok.Builder
public class ValidationServiceRestResponseDTO {
    private String reason;
    private Event currentEvent;
    private Event previousEvent;
    private boolean validEvent;
}
