package org.tan.testIt.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.tan.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@ToString
@AllArgsConstructor
@NoArgsConstructor
@Data
public class TestCreateRequest {
    @JsonProperty("name")
    private String name;

    @JsonProperty("entityTypeName")
    private final String entityTypeName = "TestCases";

    @JsonProperty("projectId")
    private final String projectId = Constants.PROJECT_ID;

    @JsonProperty("sectionId")
    private final String sectionId = Constants.SECTION_ID;

    @JsonProperty("duration")
    private int duration;

    @JsonProperty("state")
    private final String state = "NotReady";

    @JsonProperty("priority")
    private final String priority = "Medium";

    @JsonProperty("steps")
    private List<StepDto> steps = new ArrayList<>();

    @JsonProperty("preconditionSteps")
    private List<StepDto> preconditionSteps = new ArrayList<>();

    @JsonProperty("postconditionSteps")
    private List<StepDto> postconditionSteps = new ArrayList<>();

    // Остальные поля с значениями по умолчанию
    @JsonProperty("attachments")
    private final List<Object> attachments = new ArrayList<>();

    @JsonProperty("tags")
    private final List<Object> tags = new ArrayList<>();

    @JsonProperty("description")
    private final String description = "";

    @JsonProperty("attributes")
    private final Map<String, Object> attributes = new HashMap<>();

    @JsonProperty("iterations")
    private final List<Object> iterations = new ArrayList<>();

    @JsonProperty("links")
    private final List<Object> links = new ArrayList<>();

    @JsonProperty("autoTests")
    private final List<Object> autoTests = new ArrayList<>();
}
