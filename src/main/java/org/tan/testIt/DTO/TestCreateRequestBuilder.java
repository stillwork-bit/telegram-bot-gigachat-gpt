package org.tan.testIt.DTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@ToString
@NoArgsConstructor
@Data
public class TestCreateRequestBuilder {
    private final TestCreateRequest request = new TestCreateRequest();
    private final List<StepDto> steps = new ArrayList<>();
    private final List<StepDto> preconditionSteps = new ArrayList<>();
    private final List<StepDto> postconditionSteps = new ArrayList<>();

    public TestCreateRequestBuilder withName(String name) {
        request.setName(name);
        return this;
    }

    public TestCreateRequestBuilder withDuration(int duration) {
        request.setDuration(duration);
        return this;
    }

    public TestCreateRequestBuilder addStep(String action, String expected, String testData, String comments) {
        steps.add(new StepDto(action,
                expected,
                testData,
                comments
        ));
        return this;
    }

    public TestCreateRequestBuilder addPreconditionStep(String action, String expected, String testData, String comments) {
        preconditionSteps.add(new StepDto(action,
                expected,
                testData,
                comments
        ));
        return this;
    }

    public TestCreateRequestBuilder addPostconditionStep(String action, String expected, String testData, String comments) {
        postconditionSteps.add(new StepDto(action,
                expected,
                testData,
                comments
        ));
        return this;
    }

    public TestCreateRequest build() {
        request.setSteps(steps);
        request.setPreconditionSteps(preconditionSteps);
        request.setPostconditionSteps(postconditionSteps);
        return request;
    }


}
