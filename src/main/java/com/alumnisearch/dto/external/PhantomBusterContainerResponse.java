package com.alumnisearch.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhantomBusterContainerResponse {

    private String status;
    private String containerId;
    private String containerStatus;
    private Integer exitCode;
    private String output;
    private Object resultObject;
    private List<Map<String, Object>> data;
}
