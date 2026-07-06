package com.forgemind.documentation.dto;

import com.forgemind.documentation.entity.DocumentationType;
import jakarta.validation.constraints.Size;

public record GenerateDocumentationRequest(

        DocumentationType type,

        Boolean includeFlowcharts,

        @Size(max = 2000)
        String additionalInstructions
) {
}