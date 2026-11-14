package com.alumnisearch.dto.external;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PhantomBusterLaunchRequest {

    /**
     * Agent (Phantom) ID.
     */
    private String id;

    /**
     * Arguments passed into the Phantom script runtime.
     */
    private Map<String, Object> argument;
}
