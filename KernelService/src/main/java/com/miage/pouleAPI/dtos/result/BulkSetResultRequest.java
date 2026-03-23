package com.miage.pouleAPI.dtos.result;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkSetResultRequest {
    @NotNull
    private List<SetResultRequest> results;
}
