package uk.ac.ebi.intact.service.complex.ws.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComplexFacetResults {
    private String name;
    private Long count;
}
