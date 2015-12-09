package uk.ac.ebi.intact.service.complex.ws.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *  This class is to map the details of a complex retrieved from the DB
 *
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 09/12/13
 */
@XmlRootElement(name = "ComplexDetails")
@XmlAccessorType(XmlAccessType.NONE)
public class ComplexDetails {

    /********************************/
    /*      Private attributes      */
    /********************************/
    private String systematicName;
    private Collection<String> synonyms;
    private Collection<String> function;
    private Collection<String> properties;
    private String ac;
    private String name;
    private String species;
    private Collection<String> ligand;
    private Collection<String> complexAssembly;
    private Collection<String> disease;
    private Collection<ComplexDetailsParticipants> participants;
    private Collection<ComplexDetailsCrossReferences> crossReferences;
    private String institution;

    /*************************/
    /*      Constructor      */
    /*************************/
    public ComplexDetails() {
        this.synonyms = new LinkedList<String>();
        this.participants = new ArrayList<ComplexDetailsParticipants>();
        this.crossReferences = new ArrayList<ComplexDetailsCrossReferences>();
    }

    /*********************************/
    /*      Getters and Setters      */
    /*********************************/
    public void setSystematicName ( String systematic ) {
        this.systematicName = systematic;
    }
    @XmlElement
    public String getSystematicName () { return this.systematicName; }
    public void setSynonyms ( List<String> syns ) { this.synonyms = syns; }
    public void addSynonym ( String syn ) { this.synonyms.add(syn); }
    @XmlElement
    public Collection<String> getSynonyms() { return this.synonyms; }
    public void setFunction ( List<String> func ) { this.function = func; }
    @XmlElement
    public Collection<String> getFunction () { return this.function; }
    public void setProperties ( List<String> poper ) { this.properties = poper; }
    @XmlElement
    public Collection<String> getProperties () { return this.properties; }
    public void setAc ( String id ) { this.ac = id; }
    @XmlElement
    public String getAc () { return this.ac; }
    public void setName ( String n ) { this.name = n; }
    @XmlElement
    public String getName () { return this.name; }
    public void setSpecies ( String s ) { this.species = s; }
    @XmlElement
    public String getSpecies () { return this.species; }
    @XmlElement
    public Collection<String> getLigand() { return ligand; }
    public void setLigand(List<String> ligand) { this.ligand = ligand; }
    @XmlElement
    public Collection<String> getComplexAssembly() { return complexAssembly; }
    public void setComplexAssembly(List<String> complexAssembly) { this.complexAssembly = complexAssembly; }
    @XmlElement
    public Collection<String> getDisease() { return disease; }
    public void setDisease(List<String> disease) { this.disease = disease; }
    public Collection<ComplexDetailsParticipants> getParticipants() {
        return participants;
    }
    public Collection<ComplexDetailsCrossReferences> getCrossReferences() {
        return crossReferences;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }
}
