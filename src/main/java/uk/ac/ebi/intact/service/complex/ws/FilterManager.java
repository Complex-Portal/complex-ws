package uk.ac.ebi.intact.service.complex.ws;

import org.apache.solr.client.solrj.response.FacetField;
import uk.ac.ebi.intact.service.complex.ws.model.ComplexFacetResults;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class FilterManager {

    private static final Pattern FILTER_REGEX = Pattern.compile("([a-z_]+):\\(\"(.+)\"\\)");

    private static final String SPECIES_FIELD = "species_f";
    private static final String COMPONENT_TYPE_FIELD = "ptype_f";
    private static final String BIO_ROLE_FIELD = "pbiorole_f";
    private static final String PREDICTED_FIELD = "predicted_complex_f";
    private static final String EVIDENCE_TYPE_FIELD = "evidence_type_f";
    private static final String CONFIDENCE_SCORE_FIELD = "confidence_score_f";

    private static final String SPECIES_TAG = "SPECIES";
    private static final String COMPONENT_TYPE_TAG = "COMP_TYPE";
    private static final String BIO_ROLE_TAG = "BIO_ROLE";
    private static final String PREDICTED_TAG = "PREDICTED";
    private static final String EVIDENCE_TYPE_TAG = "EVIDENCE_TYPE";

    public static String[] mapFiltersParam(String filters) {
        if (filters == null) {
            return null;
        }

        String[] filtersArray = filters.split(",");
        List<String> mappedFilters = new ArrayList<>();
        for (String filter : filtersArray) {
            Matcher matcher = FILTER_REGEX.matcher(filter.trim());
            if (matcher.matches()) {
                String filterName = matcher.group(1);
                String[] filterValues = matcher.group(2).split("\"OR\"");
                mappedFilters.add(buildFilterParam(filterName, filterValues));
            }
        }
        return mappedFilters.toArray(new String[0]);
    }

    public static String[] mapFacetsParam(String facets) {
        if (facets == null) {
            return null;
        }

        String[] facetsArray = facets.split(",");
        List<String> mappedFacets = new ArrayList<>();
        for (String facet : facetsArray) {
            mappedFacets.add(buildFacetParam(facet));
        }
        return mappedFacets.toArray(new String[0]);
    }

    public static Map<String, List<ComplexFacetResults>> mapFacetsResponse(Map<String, List<FacetField.Count>> facetFields) {
        Map<String, List<ComplexFacetResults>> facets = new HashMap<>();
        for (String field: facetFields.keySet()) {
            if (facetFields.get(field) != null) {
                if (EVIDENCE_TYPE_FIELD.equals(field)) {
                    facets.put(CONFIDENCE_SCORE_FIELD, mapEvidenceTypeFacetResults(facetFields.get(field)));
                } else {
                    List<ComplexFacetResults> list = new ArrayList<>();
                    for (FacetField.Count count : facetFields.get(field)) {
                        list.add(new ComplexFacetResults(count.getName(), count.getCount()));
                    }
                    facets.put(field, list);
                }
            }
        }
        return facets;
    }

    private static String buildFilterParam(String filterName, String[] filterValues) {
        if (CONFIDENCE_SCORE_FIELD.equals(filterName)) {
            return mapConfidenceScoreFilterToEvidenceTypeFilter(filterValues);
        }
        String facetTag = getFacetTag(filterName);
        String facetTagPrefix = "";
        if (facetTag != null) {
            facetTagPrefix = "{!tag=" + facetTag + "}";
        }
        return facetTagPrefix + filterName + ":(" + "\"" + String.join("\"OR\"", filterValues) + "\"" + ")";
    }

    private static String buildFacetParam(String facetName) {
        if (CONFIDENCE_SCORE_FIELD.equals(facetName)) {
            return buildFacetParam(EVIDENCE_TYPE_FIELD);
        }
        String facetTag = getFacetTag(facetName);
        String facetTagPrefix = "";
        if (facetTag != null) {
            facetTagPrefix = "{!ex=" + facetTag + "}";
        }
        return facetTagPrefix + facetName;
    }

    private static String mapConfidenceScoreFilterToEvidenceTypeFilter(String[] filterValues) {
        Set<String> ecoCodes = new HashSet<>();
        for (String filterValue : filterValues) {
            try {
                ecoCodes.addAll(EvidenceTypeCode.getEcoCodesForConfidenceScore(Integer.valueOf(filterValue)));
            } catch (NumberFormatException e) {
                // nothing to do here
            }
        }
        return buildFilterParam(EVIDENCE_TYPE_FIELD, ecoCodes.toArray(new String[0]));
    }

    private static String getFacetTag(String filterName) {
        switch (filterName) {
            case SPECIES_FIELD:
                return SPECIES_TAG;
            case COMPONENT_TYPE_FIELD:
                return COMPONENT_TYPE_TAG;
            case BIO_ROLE_FIELD:
                return BIO_ROLE_TAG;
            case PREDICTED_FIELD:
                return PREDICTED_TAG;
            case EVIDENCE_TYPE_FIELD:
                return EVIDENCE_TYPE_TAG;
            default:
                return null;
        }
    }

    private static List<ComplexFacetResults> mapEvidenceTypeFacetResults(List<FacetField.Count> facetCounts) {
        Map<Integer, Long> facetCountByConfidenceScore = new HashMap<>();
        for (FacetField.Count count : facetCounts) {
            String facetValueName = count.getName();
            long facetValueCount = count.getCount();

            Integer confidenceScore = EvidenceTypeCode.getConfidenceScore(facetValueName);
            if (confidenceScore != null) {
                Long accumulatedCount = facetCountByConfidenceScore.getOrDefault(confidenceScore, 0L);
                facetCountByConfidenceScore.put(confidenceScore, accumulatedCount + facetValueCount);
            }
        }

        return facetCountByConfidenceScore.entrySet()
                .stream()
                .map(e -> new ComplexFacetResults(e.getKey().toString(), e.getValue()))
                .sorted(Comparator.comparing(ComplexFacetResults::getName).reversed())
                .collect(Collectors.toList());
    }
}
