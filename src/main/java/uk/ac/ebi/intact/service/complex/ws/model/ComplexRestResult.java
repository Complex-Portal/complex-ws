package uk.ac.ebi.intact.service.complex.ws.model;

import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexResultIterator;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexSearchResults;
import uk.ac.ebi.intact.service.complex.ws.FilterManager;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.*;

/**
 * @author Oscar Forner (oforner@ebi.ac.uk)
 * @version $Id$
 * @since 08/11/13
 */
@XmlRootElement(name = "Complexes")
public class ComplexRestResult {
    /********************************/
    /*      Private attributes      */
    /********************************/
    private long size;
    private long totalNumberOfResults;
    private List<ComplexSearchResults> elements;
    private Map<String,List<ComplexFacetResults>> facets;

    /*************************/
    /*      Constructor      */
    /*************************/
    public ComplexRestResult( ) {
        this.elements = new LinkedList<ComplexSearchResults>();
        this.size = 0L;
        this.facets = null;
        this.totalNumberOfResults = 0L;
    }
    public void setTotalNumberOfResults(long total) { this.totalNumberOfResults = total; }

    /***************************/
    /*      Public method      */
    /***************************/
    public void add( ComplexResultIterator iterator ) {
        this.size += iterator.getNumberOfResults();
        if ( this.facets == null && iterator.getFacetFields() != null ) {
            this.facets = FilterManager.mapFacetsResponse(iterator.getFacetFields());
        }
        while ( iterator.hasNext() ) {
            this.elements.add( iterator.next() );
        }
    }

    public void add(ComplexSearchResults complexResult) {
        this.size++;
        this.totalNumberOfResults++;
        this.elements.add(complexResult);
    }

    /*********************/
    /*      Getters      */
    /*********************/
    @XmlElement
    public long getSize() { return this.size; }
    @XmlElement
    public List<ComplexSearchResults> getElements() { return this.elements; }
    @XmlElement
    public Map<String,List<ComplexFacetResults>> getFacets() { return this.facets; }
    @XmlElement
    public long getTotalNumberOfResults() { return this.totalNumberOfResults; }
}
