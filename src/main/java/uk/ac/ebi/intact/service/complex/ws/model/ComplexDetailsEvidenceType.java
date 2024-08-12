package uk.ac.ebi.intact.service.complex.ws.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class ComplexDetailsEvidenceType {

    /********************************/
    /*      Private attributes      */
    /********************************/

    private String identifier;
    private String description;
    private String searchURL;
    private Integer stars;

    /**************************/
    /*      Constructors      */
    /**************************/

    public ComplexDetailsEvidenceType() {
        this.identifier  = null;
        this.description = null;
        this.searchURL = null;
        this.stars = null;
    }

    public ComplexDetailsEvidenceType(String identifier, String description, Integer stars) {
        this.identifier = identifier;
        this.description = description;
        this.stars = stars;
        this.searchURL = null;
    }

    /*********************************/
    /*      Getters and Setters      */
    /*********************************/

    @XmlElement
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @XmlElement
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlElement
    public String getSearchURL() {
        return searchURL;
    }

    public void setSearchURL(String searchURL) {
        this.searchURL = searchURL;
    }

    @XmlElement
    public Integer getStars() {
        return stars;
    }

    public void setStars(Integer stars) {
        this.stars = stars;
    }

}
