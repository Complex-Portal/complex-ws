package uk.ac.ebi.intact.service.complex.ws.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import java.util.List;
import java.util.Map;

@XmlRootElement(name = "Complexes")
public class ComplexRestResultV2 {

    private final long size;
    private final long totalNumberOfResults;
    private final List<ComplexSearchResultV2> elements;
    private final Map<String,List<ComplexFacetResults>> facets;

    public ComplexRestResultV2(long size,
                               long totalNumberOfResults,
                               List<ComplexSearchResultV2> elements,
                               Map<String, List<ComplexFacetResults>> facets) {
        this.size = size;
        this.totalNumberOfResults = totalNumberOfResults;
        this.elements = elements;
        this.facets = facets;
    }

    @XmlElement
    public long getSize() { return this.size; }
    @XmlElement
    public List<ComplexSearchResultV2> getElements() { return this.elements; }
    @XmlElement
    public Map<String,List<ComplexFacetResults>> getFacets() { return this.facets; }
    @XmlElement
    public long getTotalNumberOfResults() { return this.totalNumberOfResults; }
}
