package uk.ac.ebi.intact.service.complex.ws.model;

public class ComplexParticipant {

    private String identifier;
    private String identifierLink;
    private String name;
    private String description;
    private String stochiometry;
    private String interactorType;

    public ComplexParticipant( ) {
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifierLink() {
        return identifierLink;
    }

    public void setIdentifierLink(String identifierLink) {
        this.identifierLink = identifierLink;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStochiometry() {
        return stochiometry;
    }

    public void setStochiometry(String stochiometry) {
        this.stochiometry = stochiometry;
    }

    public String getInteractorType() {
        return interactorType;
    }

    public void setInteractorType(String interactorType) {
        this.interactorType = interactorType;
    }
}
