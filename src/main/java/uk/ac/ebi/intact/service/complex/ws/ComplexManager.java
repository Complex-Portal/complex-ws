package uk.ac.ebi.intact.service.complex.ws;

import lombok.extern.log4j.Log4j;
import org.apache.solr.client.solrj.SolrServerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import psidev.psi.mi.jami.model.ModelledParticipant;
import uk.ac.ebi.complex.service.ComplexFinder;
import uk.ac.ebi.complex.service.ComplexFinderResult;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexFieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexInteractor;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexSearchResults;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;
import uk.ac.ebi.intact.service.complex.ws.model.ComplexDetails;
import uk.ac.ebi.intact.service.complex.ws.model.ComplexRestResult;
import uk.ac.ebi.intact.service.complex.ws.utils.IntactComplexUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Log4j
@Component
public class ComplexManager {

    /*
     -- BASIC KNOWLEDGE ABOUT SPRING MVC CONTROLLERS --
      * They look like the next one:
      @RequestMapping(value = "/<path to listen>/{<variable>}")
	  public <ResultType> search(@PathVariable String <variable>) {
          ...
	  }

	  * First of all, we have the @RequestMapping annotation where you can
	    use these options:
	     - headers: Same format for any environment: a sequence of
	                "My-Header=myValue" style expressions
	     - method: The HTTP request methods to map to, narrowing the primary
	               mapping: GET, POST, HEAD, OPTIONS, PUT, DELETE, TRACE.
	     - params: Same format for any environment: a sequence of
	               "myParam=myValue" style expressions
	     - value: Ant-style path patterns are also supported (e.g. "/myPath/*.do").

      * Next we have the function signature, with the result type to return,
        the name of the function and the parameters to it. We could see the
        @PathVariable in the parameters it is to say that the content between
        { and } is assigned to this variable. NOTE: They must have the same name

        Moreover, we can have @RequestedParam if we need to read or use a parameter
        provided using "?name=value" way. WE WANT TO DO THAT WITH THE FORMAT,
        BUT THIS PARAMETER IS CONTROLLED BY THE ContentNegotiatingViewResolver BEAN
        IN THE SPRING FILE.
     */

    /********************************/
    /*      Private attributes      */
    /********************************/


    private DataProvider dataProvider;
    private IntactDao intactDao;
    private ComplexFinder complexFinder;

    @Autowired
    public ComplexManager(DataProvider dataProvider,
                          @Qualifier("intactDao") IntactDao intactDao) {
        this.dataProvider = dataProvider;
        this.intactDao = intactDao;
        this.complexFinder = new ComplexFinder(intactDao);
    }

    /****************************/
    /*      Public methods      */
    /****************************/

    public ComplexDetails createComplexDetails(IntactComplex complex) throws Exception {
        if (complex != null) {
            return newComplexDetails(complex);
        } else {
            throw new Exception();
        }
    }

    private ComplexDetails newComplexDetails(IntactComplex complex) {
        ComplexDetails details = new ComplexDetails();
        details.setAc(complex.getAc());
        details.setComplexAc(complex.getComplexAc());
        details.setFunctions(IntactComplexUtils.getFunctions(complex));
        details.setProperties(IntactComplexUtils.getProperties(complex));
        details.setDiseases(IntactComplexUtils.getDiseases(complex));
        details.setLigands(IntactComplexUtils.getLigands(complex));
        details.setComplexAssemblies(IntactComplexUtils.getComplexAssemblies(complex));
        details.setName(IntactComplexUtils.getComplexName(complex));
        details.setSynonyms(IntactComplexUtils.getComplexSynonyms(complex));
        details.setSystematicName(IntactComplexUtils.getSystematicName(complex));
        details.setSpecies(IntactComplexUtils.getSpeciesName(complex) + "; " + IntactComplexUtils.getSpeciesTaxId(complex));
        details.setInstitution(complex.getSource().getShortName());
        details.setInstitutionURL(IntactComplexUtils.getCvTermUrl(complex.getSource()));
        details.setAgonists(IntactComplexUtils.getAgonists(complex));
        details.setAntagonists(IntactComplexUtils.getAntagonists(complex));
        details.setComments(IntactComplexUtils.getComments(complex));
        details.setPredictedComplex(complex.isPredictedComplex());
        details.setEvidenceType(IntactComplexUtils.createEvidenceType(complex.getEvidenceType()));
        IntactComplexUtils.setParticipants(complex, details);
        IntactComplexUtils.setCrossReferences(complex, details);
        return details;
    }

    // This method controls the first and number parameters and retrieve data
    public ComplexRestResult query(String query, String first, String number, String filters, String facets) throws SolrServerException {
        // Get parameters (if we have them)
        int f, n;
        // If we have first parameter parse it to integer
        if ( first != null ) f = Integer.parseInt(first);
            // else set first parameter to 0
        else f = 0;
        // If we have number parameter parse it to integer
        if ( number != null ) n = Integer.parseInt(number);
            // else set number parameter to max integer - first (to avoid problems)
        else n = Integer.MAX_VALUE - f;
        // Retrieve data using that parameters and return it
        return this.dataProvider.getData(
                query,
                f,
                n,
                FilterManager.mapFiltersParam(filters),
                FilterManager.mapFacetsParam(facets));
    }

    public ComplexSearchResults getComplexSearchResultsFromSolrOrDb(String ac) throws SolrServerException {
        // Search in SOLR using COMPLEX_ID (indexed complex AC) to only retrieve the specific complex
        ComplexRestResult searchResult = query(ComplexFieldNames.COMPLEX_ID + ":" + ac, null, null, null, null);

        for (ComplexSearchResults searchResults: searchResult.getElements()) {
            if (searchResults.getComplexAC().equals(ac)) {
                return searchResults;
            }
        }

        // If complex was not found in SOLR, for whatever reason, we load it from the DB
        IntactComplex complex = intactDao.getComplexDao().getLatestComplexVersionByComplexAc(ac);
        if (complex != null) {
            return mapComplex(complex);
        }

        return null;
    }

    public ComplexFinderResult<ComplexDetails> findComplexWithMatchingProteins(List<String> proteinAcs) {
        ComplexFinderResult<IntactComplex> complexFinderResult = complexFinder.findComplexWithMatchingProteins(proteinAcs);
        return new ComplexFinderResult<>(
                complexFinderResult.getProteins(),
                complexFinderResult.getExactMatches().stream().map(this::mapExactMatch).collect(Collectors.toList()),
                complexFinderResult.getPartialMatches().stream().map(this::mapPartialMatch).collect(Collectors.toList()));
    }

    /*******************************/
    /*      Private methods        */
    /*******************************/

    private ComplexSearchResults mapComplex(IntactComplex complex) {
        ComplexSearchResults complexSearchResults = new ComplexSearchResults();
        complexSearchResults.setComplexAC(complex.getComplexAc());
        complexSearchResults.setComplexName(IntactComplexUtils.getComplexName(complex));
        complexSearchResults.setOrganismName(IntactComplexUtils.getSpeciesName(complex));
        complexSearchResults.setPredictedComplex(complex.isPredictedComplex());
        List<String> complexFunctions = IntactComplexUtils.getFunctions(complex);
        if (complexFunctions != null) {
            complexSearchResults.setDescription(String.join(" ", complexFunctions));
        }
        complexSearchResults.setInteractors(createComplexParticipants(complex));
        return complexSearchResults;
    }

    private List<ComplexInteractor> createComplexParticipants(IntactComplex complex) {
        List<ComplexInteractor> interactors = new ArrayList<>();
        for (ModelledParticipant modelledParticipant : IntactComplexUtils.mergeParticipants(complex.getParticipants())) {
            ComplexInteractor interactor = new ComplexInteractor();
            String identifier = IntactComplexUtils.getParticipantIdentifier(modelledParticipant);
            interactor.setIdentifier(identifier);
            interactor.setIdentifierLink(IntactComplexUtils.getParticipantIdentifierLink(modelledParticipant, identifier));
            interactor.setName(IntactComplexUtils.getParticipantName(modelledParticipant));
            interactor.setDescription(modelledParticipant.getInteractor().getFullName());
            interactor.setStochiometry(IntactComplexUtils.getParticipantStoichiometry(modelledParticipant));
            interactor.setInteractorType(modelledParticipant.getInteractor().getInteractorType().getFullName());
            interactors.add(interactor);
        }
        return interactors;
    }

    private ComplexFinderResult.ExactMatch<ComplexDetails> mapExactMatch(
            ComplexFinderResult.ExactMatch<IntactComplex> complexMatch) {

        return new ComplexFinderResult.ExactMatch<>(
                complexMatch.getComplexAc(),
                complexMatch.getMatchType(),
                newComplexDetails(complexMatch.getComplex()));
    }

    private ComplexFinderResult.PartialMatch<ComplexDetails> mapPartialMatch(
            ComplexFinderResult.PartialMatch<IntactComplex> complexMatch) {

        return new ComplexFinderResult.PartialMatch<>(
                complexMatch.getComplexAc(),
                complexMatch.getMatchType(),
                complexMatch.getMatchingProteins(),
                complexMatch.getExtraProteinsInComplex(),
                complexMatch.getProteinMissingInComplex(),
                newComplexDetails(complexMatch.getComplex()));
    }
}
