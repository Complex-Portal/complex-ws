package uk.ac.ebi.intact.service.complex.ws.model;

import java.util.List;

public class ComplexSearchResultV2 {

    private String complexAC;
    private String complexName;
    private String organismName;
    private String description;
    private List<ComplexParticipant> components;

    public ComplexSearchResultV2( ) {
    }

    public String getComplexAC() {
        return complexAC;
    }

    public void setComplexAC(String complexAC) {
        this.complexAC = complexAC;
    }

    public String getComplexName() {
        return complexName;
    }

    public void setComplexName(String complexName) {
        this.complexName = complexName;
    }

    public String getOrganismName() {
        return organismName;
    }

    public void setOrganismName(String organismName) {
        this.organismName = organismName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<ComplexParticipant> getComponents() {
        return components;
    }

    public void setComponents(List<ComplexParticipant> components) {
        this.components = components;
    }
}
