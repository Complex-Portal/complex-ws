package uk.ac.ebi.intact.service.complex.ws;

import com.google.common.collect.ImmutableList;
import org.apache.solr.client.solrj.response.FacetField;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ebi.intact.service.complex.ws.model.ComplexFacetResults;

import java.util.List;
import java.util.Map;

public class FilterManagerTests {

    @Test
    public void testSpeciesFilter() {
        String filtersInput = "species_f:(\"Homo sapiens\"OR\"Mus musculus\")";
        String[] expectedFilters = new String[]{
                "{!tag=SPECIES}species_f:(\"Homo sapiens\"OR\"Mus musculus\")"
        };
        String[] filters = FilterManager.mapFiltersParam(filtersInput);
        Assert.assertArrayEquals(expectedFilters, filters);
    }

    @Test
    public void testComponentTypeFilter() {
        String filtersInput = "ptype_f:(\"protein\"OR\"small molecule\")";
        String[] expectedFilters = new String[]{
                "{!tag=COMP_TYPE}ptype_f:(\"protein\"OR\"small molecule\")"
        };
        String[] filters = FilterManager.mapFiltersParam(filtersInput);
        Assert.assertArrayEquals(expectedFilters, filters);
    }

    @Test
    public void testBioRoleFilter() {
        String filtersInput = "pbiorole_f:(\"unspecified role\"OR\"enzyme\")";
        String[] expectedFilters = new String[]{
                "{!tag=BIO_ROLE}pbiorole_f:(\"unspecified role\"OR\"enzyme\")"
        };
        String[] filters = FilterManager.mapFiltersParam(filtersInput);
        Assert.assertArrayEquals(expectedFilters, filters);
    }

    @Test
    public void testPredictedFilter() {
        String filtersInput = "predicted_complex_f:(\"false\"OR\"true\")";
        String[] expectedFilters = new String[]{
                "{!tag=PREDICTED}predicted_complex_f:(\"false\"OR\"true\")"
        };
        String[] filters = FilterManager.mapFiltersParam(filtersInput);
        Assert.assertArrayEquals(expectedFilters, filters);
    }

    @Test
    public void testEvidenceTypeFilter() {
        String filtersInput = "evidence_type_f:(\"ECO:0000353\"OR\"ECO:0000314\")";
        String[] expectedFilters = new String[]{
                "{!tag=EVIDENCE_TYPE}evidence_type_f:(\"ECO:0000353\"OR\"ECO:0000314\")"
        };
        String[] filters = FilterManager.mapFiltersParam(filtersInput);
        Assert.assertArrayEquals(expectedFilters, filters);
    }

    @Test
    public void testConfidenceScoreFilter() {
        String filtersInput = "confidence_score_f:(\"3\"OR\"4\")";
        String[] expectedFilters = new String[]{
                "{!tag=EVIDENCE_TYPE}evidence_type_f:(\"ECO:0000353\"OR\"ECO:0005543\"OR\"ECO:0005610\"OR\"ECO:0005544\"OR\"ECO:0005546\"OR\"ECO:0005547\")"
        };
        String[] filters = FilterManager.mapFiltersParam(filtersInput);
        Assert.assertArrayEquals(expectedFilters, filters);
    }

    @Test
    public void testMultipleFilters() {
        String filtersInput = "ptype_f:(\"protein\"OR\"small molecule\"),species_f:(\"Homo sapiens\"OR\"Mus musculus\")";
        String[] expectedFilters = new String[]{
                "{!tag=COMP_TYPE}ptype_f:(\"protein\"OR\"small molecule\")",
                "{!tag=SPECIES}species_f:(\"Homo sapiens\"OR\"Mus musculus\")"
        };
        String[] filters = FilterManager.mapFiltersParam(filtersInput);
        Assert.assertArrayEquals(expectedFilters, filters);
    }

    @Test
    public void testSpeciesFacet() {
        String facetsInput = "species_f";
        String[] expectedFacets = new String[]{
                "{!ex=SPECIES}species_f"
        };
        String[] facets = FilterManager.mapFacetsParam(facetsInput);
        Assert.assertArrayEquals(expectedFacets, facets);
    }

    @Test
    public void testComponentTypeFacet() {
        String facetsInput = "ptype_f";
        String[] expectedFacets = new String[]{
                "{!ex=COMP_TYPE}ptype_f"
        };
        String[] facets = FilterManager.mapFacetsParam(facetsInput);
        Assert.assertArrayEquals(expectedFacets, facets);
    }

    @Test
    public void testBioRoleFacet() {
        String facetsInput = "pbiorole_f";
        String[] expectedFacets = new String[]{
                "{!ex=BIO_ROLE}pbiorole_f"
        };
        String[] facets = FilterManager.mapFacetsParam(facetsInput);
        Assert.assertArrayEquals(expectedFacets, facets);
    }

    @Test
    public void testPredictedFacet() {
        String facetsInput = "predicted_complex_f";
        String[] expectedFacets = new String[]{
                "{!ex=PREDICTED}predicted_complex_f"
        };
        String[] facets = FilterManager.mapFacetsParam(facetsInput);
        Assert.assertArrayEquals(expectedFacets, facets);
    }

    @Test
    public void testEvidenceTypeFacet() {
        String facetsInput = "evidence_type_f";
        String[] expectedFacets = new String[]{
                "{!ex=EVIDENCE_TYPE}evidence_type_f"
        };
        String[] facets = FilterManager.mapFacetsParam(facetsInput);
        Assert.assertArrayEquals(expectedFacets, facets);
    }

    @Test
    public void testConfidenceScoreFacet() {
        String facetsInput = "confidence_score_f";
        String[] expectedFacets = new String[]{
                "{!ex=EVIDENCE_TYPE}evidence_type_f"
        };
        String[] facets = FilterManager.mapFacetsParam(facetsInput);
        Assert.assertArrayEquals(expectedFacets, facets);
    }

    @Test
    public void testMultipleFacets() {
        String facetsInput = "species_f,ptype_f,confidence_score_f";
        String[] expectedFacets = new String[]{
                "{!ex=SPECIES}species_f",
                "{!ex=COMP_TYPE}ptype_f",
                "{!ex=EVIDENCE_TYPE}evidence_type_f"
        };
        String[] facets = FilterManager.mapFacetsParam(facetsInput);
        Assert.assertArrayEquals(expectedFacets, facets);
    }

    @Test
    public void testSpeciesFacetResponse() {
        Map<String, List<FacetField.Count>> facetFields = Map.of(
                "species_f",
                ImmutableList.of(
                        new FacetField.Count(null, "Homo sapiens", 5L),
                        new FacetField.Count(null, "Mus musculus", 2L)));
        Map<String, List<ComplexFacetResults>> expectedFacets = Map.of(
                "species_f",
                ImmutableList.of(
                        new ComplexFacetResults("Homo sapiens", 5L),
                        new ComplexFacetResults("Mus musculus", 2L)));
        Map<String, List<ComplexFacetResults>> facets = FilterManager.mapFacetsResponse(facetFields);
        Assert.assertEquals(expectedFacets, facets);
    }

    @Test
    public void testComponentTypeFacetResponse() {
        Map<String, List<FacetField.Count>> facetFields = Map.of(
                "ptype_f",
                ImmutableList.of(
                       new FacetField.Count(null, "protein", 5L),
                        new FacetField.Count(null, "small molecule", 2L)));
        Map<String, List<ComplexFacetResults>> expectedFacets = Map.of(
                "ptype_f",
                ImmutableList.of(
                        new ComplexFacetResults("protein", 5L),
                        new ComplexFacetResults("small molecule", 2L)));
        Map<String, List<ComplexFacetResults>> facets = FilterManager.mapFacetsResponse(facetFields);
        Assert.assertEquals(expectedFacets, facets);
    }

    @Test
    public void testBioRoleFacetResponse() {
        Map<String, List<FacetField.Count>> facetFields = Map.of(
                "pbiorole_f",
                ImmutableList.of(
                        new FacetField.Count(null, "enzyme", 5L),
                        new FacetField.Count(null, "unspecified role", 2L)));
        Map<String, List<ComplexFacetResults>> expectedFacets = Map.of(
                "pbiorole_f",
                ImmutableList.of(
                        new ComplexFacetResults("enzyme", 5L),
                        new ComplexFacetResults("unspecified role", 2L)));
        Map<String, List<ComplexFacetResults>> facets = FilterManager.mapFacetsResponse(facetFields);
        Assert.assertEquals(expectedFacets, facets);
    }

    @Test
    public void testPredictedComplexFacetResponse() {
        Map<String, List<FacetField.Count>> facetFields = Map.of(
                "predicted_complex_f",
                ImmutableList.of(
                        new FacetField.Count(null, "false", 5L),
                        new FacetField.Count(null, "true", 2L)));
        Map<String, List<ComplexFacetResults>> expectedFacets = Map.of(
                "predicted_complex_f",
                ImmutableList.of(
                        new ComplexFacetResults("false", 5L),
                        new ComplexFacetResults("true", 2L)));
        Map<String, List<ComplexFacetResults>> facets = FilterManager.mapFacetsResponse(facetFields);
        Assert.assertEquals(expectedFacets, facets);
    }

    @Test
    public void testConfidenceScoreFacetResponse() {
        Map<String, List<FacetField.Count>> facetFields = Map.of(
                "evidence_type_f",
                ImmutableList.of(
                        new FacetField.Count(null, "ECO:0000353", 5L),
                        new FacetField.Count(null, "ECO:0005543", 2L),
                        new FacetField.Count(null, "ECO:0005547", 10L)));
        Map<String, List<ComplexFacetResults>> expectedFacets = Map.of(
                "confidence_score_f",
                ImmutableList.of(
                        new ComplexFacetResults("5", 7L),
                        new ComplexFacetResults("3", 10L)));
        Map<String, List<ComplexFacetResults>> facets = FilterManager.mapFacetsResponse(facetFields);
        Assert.assertEquals(expectedFacets, facets);
    }

    @Test
    public void testMultipleFacetsResponse() {
        Map<String, List<FacetField.Count>> facetFields = Map.of(
                "species_f",
                ImmutableList.of(
                        new FacetField.Count(null, "Homo sapiens", 5L),
                        new FacetField.Count(null, "Mus musculus", 2L)),
                "ptype_f",
                ImmutableList.of(
                        new FacetField.Count(null, "protein", 5L),
                        new FacetField.Count(null, "small molecule", 2L)),
                "evidence_type_f",
                ImmutableList.of(
                        new FacetField.Count(null, "ECO:0000353", 5L),
                        new FacetField.Count(null, "ECO:0005543", 2L),
                        new FacetField.Count(null, "ECO:0005547", 10L)));
        Map<String, List<ComplexFacetResults>> expectedFacets = Map.of(
                "species_f",
                ImmutableList.of(
                        new ComplexFacetResults("Homo sapiens", 5L),
                        new ComplexFacetResults("Mus musculus", 2L)),
                "ptype_f",
                ImmutableList.of(
                        new ComplexFacetResults("protein", 5L),
                        new ComplexFacetResults("small molecule", 2L)),
                "confidence_score_f",
                ImmutableList.of(
                        new ComplexFacetResults("5", 7L),
                        new ComplexFacetResults("3", 10L)));
        Map<String, List<ComplexFacetResults>> facets = FilterManager.mapFacetsResponse(facetFields);
        Assert.assertEquals(expectedFacets, facets);
    }
}
