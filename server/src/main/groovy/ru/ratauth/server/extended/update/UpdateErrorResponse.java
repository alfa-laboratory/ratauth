package ru.ratauth.server.extended.update;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateErrorResponse {

    private String reason;
    private String updateCode;
    private String updateService;
}
