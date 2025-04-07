package org.tan.testIt.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@ToString
@AllArgsConstructor
@Data
public class StepDto {
    @JsonProperty("action")
    private String action;

    @JsonProperty("expected")
    private String expected;

    @JsonProperty("testData")
    private String testData;

    @JsonProperty("comments")
    private String comments;
}

