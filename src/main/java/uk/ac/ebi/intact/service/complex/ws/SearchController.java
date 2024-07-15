package uk.ac.ebi.intact.service.complex.ws;

import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.SolrServerException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import psidev.psi.mi.jami.commons.MIWriterOptionFactory;
import psidev.psi.mi.jami.datasource.InteractionWriter;
import psidev.psi.mi.jami.factory.InteractionWriterFactory;
import psidev.psi.mi.jami.json.InteractionViewerJson;
import psidev.psi.mi.jami.json.MIJsonOptionFactory;
import psidev.psi.mi.jami.json.MIJsonType;
import psidev.psi.mi.jami.model.ComplexType;
import psidev.psi.mi.jami.model.InteractionCategory;
import psidev.psi.mi.jami.model.ModelledParticipant;
import psidev.psi.mi.jami.xml.PsiXmlVersion;
import uk.ac.ebi.complex.service.ComplexFinder;
import uk.ac.ebi.complex.service.ComplexFinderResult;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexFieldNames;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexInteractor;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexSearchResults;
import uk.ac.ebi.intact.dataexchange.psimi.xml.IntactPsiXml;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;
import uk.ac.ebi.intact.service.complex.ws.model.ComplexDetails;
import uk.ac.ebi.intact.service.complex.ws.model.ComplexRestResult;
import uk.ac.ebi.intact.service.complex.ws.utils.IntactComplexUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Controller
public class SearchController {

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
    public SearchController(DataProvider dataProvider,
                            @Qualifier("intactDao") IntactDao intactDao) {
        this.dataProvider = dataProvider;
        this.intactDao = intactDao;
        this.complexFinder = new ComplexFinder(intactDao);
    }

    private static final Log log = LogFactory.getLog(SearchController.class);

    /****************************/
    /*      Public methods      */
    /****************************/

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showHomeHelp(HttpServletResponse response){
        enableClacks(response);
        return "home";
    }

    @RequestMapping(value = "/search/", method = RequestMethod.GET)
    public String showSearchHelp(HttpServletResponse response){
        enableClacks(response);
        return "search";
    }

    @RequestMapping(value = "/details/", method = RequestMethod.GET)
    public String showDetailsHelp(HttpServletResponse response){
        enableClacks(response);
        return "details";
    }


    @RequestMapping(value = "/complex/", method = RequestMethod.GET)
    public String showComplexHelp(HttpServletResponse response){
        enableClacks(response);
        return "complex";
    }

    @RequestMapping(value = "/export/", method = RequestMethod.GET)
    public String showExportHelp(HttpServletResponse response){
        enableClacks(response);
        return "export";
    }

    @RequestMapping(value = "/find/", method = RequestMethod.GET)
    public String showFindHelp(HttpServletResponse response){
        enableClacks(response);
        return "find";
    }

    @RequestMapping(value = "/count/{query}", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String count(@PathVariable String query, ModelMap model, HttpServletResponse response) throws SolrServerException {
        enableClacks(response);
        String q = null;
        try {
            q = URIUtil.decode(query);
        } catch (URIException e) {
            e.printStackTrace();
        }
        long total = query(q, null, null, null, null).getTotalNumberOfResults();
        model.addAttribute("count", total);
        return "count";
    }

    /*
     - We can access to that method using:
         http://<servername>:<port>/search/<something to query>
       and
         http://<servername>:<port>/search/<something to query>?format=<type>
     - If we do not use the format parameter we will receive the answer in json
     - Only listen request via GET never via POST.
     - Does not change the query.
     */
    @RequestMapping(value = "/search/{query}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
	public ResponseEntity<String> search(@PathVariable String query,
                                    @RequestParam (required = false) String first,
                                    @RequestParam (required = false) String number,
                                    @RequestParam (required = false) String filters,
                                    @RequestParam (required = false) String facets,
                                    HttpServletResponse response) throws SolrServerException, IOException {
        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        ComplexRestResult searchResult = query(query, first, number, filters, facets);
        try {
            ComplexRestResult enrichedSearchResult = enrichQueryResults(searchResult, query, first, number);
            mapper.writeValue(writer, enrichedSearchResult);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.add("X-Clacks-Overhead", "GNU Terry Pratchett"); //In memory of Sir Terry Pratchett
        enableCORS(headers);
        return new ResponseEntity<String>(writer.toString(), headers, HttpStatus.OK);
	}

    @RequestMapping(value = "/complex-simplified/{ac}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public ResponseEntity<String> retrieveComplexByAcFromSolr(@PathVariable String ac,
                                                              HttpServletResponse response) throws SolrServerException, IOException {
        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        try {
            ComplexSearchResults enrichedSearchResult = getComplexSearchResultsFromSolrOrDb(ac);
            mapper.writeValue(writer, enrichedSearchResult);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.add("X-Clacks-Overhead", "GNU Terry Pratchett"); //In memory of Sir Terry Pratchett
        enableCORS(headers);
        return new ResponseEntity<String>(writer.toString(), headers, HttpStatus.OK);
    }

    /*
     - We can access to that method using:
         http://<servername>:<port>/details/<ac of a complex>
       and
         http://<servername>:<port>/details/<ac of a complex>?format=<type>
     - If we do not use the format parameter we will receive the answer in json
     - Only listen request via GET never via POST.
     - Query the information in our database about the ac of the complex.
     */
    @RequestMapping(value = "/details/{ac}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public ResponseEntity<String> retrieveComplex(@PathVariable String ac,
                                                  HttpServletResponse response) throws Exception {

        IntactComplex complex = intactDao.getComplexDao().getByAc(ac);

        ComplexDetails details = createComplexDetails(complex);

        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(writer, details);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.add("X-Clacks-Overhead", "GNU Terry Pratchett"); //In memory of Sir Terry Pratchett

        enableCORS(headers);

        return new ResponseEntity<String>(writer.toString(), headers, HttpStatus.OK);
    }


    /*
    * Query the complex details from the database given a new complexAc identifier
    * Returns answer in json format
    *
    * */
    @RequestMapping(value = "/complex/{complexAc}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public ResponseEntity<String> retrieveComplexByAc(@PathVariable String complexAc,
                                                      HttpServletResponse response) throws Exception {

        IntactComplex complex = intactDao.getComplexDao().getLatestComplexVersionByComplexAc(complexAc);

        ComplexDetails details = createComplexDetails(complex);

        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(writer, details);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.add("X-Clacks-Overhead", "GNU Terry Pratchett"); //In memory of Sir Terry Pratchett

        enableCORS(headers);

        return new ResponseEntity<String>(writer.toString(), headers, HttpStatus.OK);
    }

    private ComplexDetails createComplexDetails(IntactComplex complex) throws Exception {
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
        details.setAgonists(IntactComplexUtils.getAgonists(complex));
        details.setAntagonists(IntactComplexUtils.getAntagonists(complex));
        details.setComments(IntactComplexUtils.getComments(complex));
        IntactComplexUtils.setParticipants(complex, details);
        IntactComplexUtils.setCrossReferences(complex, details);
        return details;
    }

    @RequestMapping(value = "/export/{query}", method = RequestMethod.GET)
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public ResponseEntity<String> exportComplex(@PathVariable String query,
                                                @RequestParam (required = false) String filters,
                                                @RequestParam (required = false) String format,
                                                HttpServletResponse response) throws Exception {
        Boolean exportAsFile = false;

        List<IntactComplex> complexes;
        if(isQueryASingleId(query)) {
            complexes = new ArrayList<IntactComplex>(1);

            IntactComplex complex;
            if(query.startsWith("EBI-")) {
                complex = intactDao.getComplexDao().getByAc(query);
            } else {
                complex = intactDao.getComplexDao().getLatestComplexVersionByComplexAc(query);

            }

            if (complex != null)
                complexes.add(complex);
        }
        else {
            ComplexRestResult searchResult = query(query, null, null, filters, null);
            complexes = new ArrayList<IntactComplex>(searchResult.getElements().size());
            for (ComplexSearchResults result : searchResult.getElements()) {
                IntactComplex complex = intactDao.getComplexDao().getByAc(result.getComplexAC());
                if (complex != null)
                    complexes.add(complex);
            }
            exportAsFile = true;
        }

        ResponseEntity<String> responseEntity = null;
        if (!complexes.isEmpty()) {
            InteractionWriterFactory writerFactory = InteractionWriterFactory.getInstance();
            if (format != null) {
                switch (ComplexExportFormat.formatOf(format)) {
                    case XML25:
                        responseEntity = createXml25Response(complexes, writerFactory, exportAsFile);
                        break;
                    case XML30:
                        responseEntity = createXml30Response(complexes, writerFactory, exportAsFile);
                        break;
                    case JSON:
                    default:
                        responseEntity = createJsonResponse(complexes, writerFactory, exportAsFile);
                        break;
                }
            }
            else {
                responseEntity = createJsonResponse(complexes, writerFactory, exportAsFile);
            }
            return responseEntity;
        }
        throw new Exception("Export failed " + query + ". No complexes result");
    }
    @RequestMapping(value = "/find", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public ResponseEntity<String> findComplexMatches(@RequestParam("proteinAcs") List<String> proteinAcs,
                                                     HttpServletResponse response) throws Exception {

        List<String> parsedProteinAcs = proteinAcs.stream()
                .flatMap(proteinAc -> Stream.of(proteinAc.split(",")))
                .map(String::trim)
                .collect(Collectors.toList());

        ComplexFinderResult<IntactComplex> complexFinderResult = complexFinder.findComplexWithMatchingProteins(parsedProteinAcs);
        ComplexFinderResult<ComplexDetails> complexFinderResponse = new ComplexFinderResult<>(
                complexFinderResult.getProteins(),
                complexFinderResult.getExactMatches().stream().map(this::mapExactMatch).collect(Collectors.toList()),
                complexFinderResult.getPartialMatches().stream().map(this::mapPartialMatch).collect(Collectors.toList()));

        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(writer, complexFinderResponse);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.add("X-Clacks-Overhead", "GNU Terry Pratchett"); //In memory of Sir Terry Pratchett

        enableCORS(headers);

        return new ResponseEntity<>(writer.toString(), headers, HttpStatus.OK);
    }

    private boolean isQueryASingleId(String query) {
        return (query.startsWith("EBI-") || query.startsWith("CPX-")) && query.trim().split(" ").length == 1;
    }

    private ResponseEntity<String> createXml25Response(List<IntactComplex> complexes, InteractionWriterFactory writerFactory, Boolean exportAsFile) {
        return createXmlResponse(complexes, writerFactory, PsiXmlVersion.v2_5_4, exportAsFile);
    }

    private ResponseEntity<String> createXml30Response(List<IntactComplex> complexes, InteractionWriterFactory writerFactory, Boolean exportAsFile) {
        return createXmlResponse(complexes, writerFactory, PsiXmlVersion.v3_0_0, exportAsFile);
    }

    private ResponseEntity<String> createXmlResponse(List<IntactComplex> complexes, InteractionWriterFactory writerFactory, PsiXmlVersion version, Boolean exportAsFile) {
        IntactPsiXml.initialiseAllIntactXmlWriters();
        MIWriterOptionFactory optionFactory = MIWriterOptionFactory.getInstance();
        StringWriter answer = new StringWriter();
        Map<String, Object> options = optionFactory.getDefaultCompactXmlOptions(answer, InteractionCategory.complex, ComplexType.n_ary, version);
        InteractionWriter writer = writerFactory.getInteractionWriterWith(options) ;
        try {
            writer.start();
            writer.write(complexes);
            writer.end();
        }
        finally {
            writer.close();
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", MediaType.APPLICATION_XML_VALUE);
        httpHeaders.add("X-Clacks-Overhead", "GNU Terry Pratchett"); //In memory of Sir Terry Pratchett
        enableCORS(httpHeaders);
        if (exportAsFile) {
            httpHeaders.set("Content-Disposition", "attachment; filename=" + complexes.toString());
        }
        return new ResponseEntity<String>(answer.toString(), httpHeaders, HttpStatus.OK);
    }

    private ResponseEntity<String> createJsonResponse(List<IntactComplex> complexes, InteractionWriterFactory writerFactory, Boolean exportAsFile) {

        InteractionViewerJson.initialiseAllMIJsonWriters();
        MIJsonOptionFactory optionFactory = MIJsonOptionFactory.getInstance();
        StringWriter answer = new StringWriter();

        Map<String, Object> options = optionFactory.getJsonOptions(answer, InteractionCategory.modelled, ComplexType.n_ary, MIJsonType.n_ary_only, null, null);
        InteractionWriter writer = writerFactory.getInteractionWriterWith(options);

        try {
            writer.start();
            writer.write(complexes);
            writer.end();
        }
        finally {
            writer.close();
        }
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
        httpHeaders.add("X-Clacks-Overhead", "GNU Terry Pratchett"); //In memory of Sir Terry Pratchett
        enableCORS(httpHeaders);
        if (exportAsFile) {
            httpHeaders.set("Content-Disposition", "attachment; filename=" + complexes.toString());
        }
        return new ResponseEntity<String>(answer.toString(), httpHeaders, HttpStatus.OK);
    }

    /*******************************/
    /*      Protected methods      */
    /*******************************/
    // This method controls the first and number parameters and retrieve data
    protected ComplexRestResult query(String query, String first, String number, String filters, String facets) throws SolrServerException {
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
        return this.dataProvider.getData( query, f, n, filters , facets);
    }

    private ComplexSearchResults getComplexSearchResultsFromSolrOrDb(String ac) throws SolrServerException {
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

    private ComplexRestResult enrichQueryResults(ComplexRestResult searchResult,
                                                 String query,
                                                 String first,
                                                 String number) {
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

        long size = searchResult.getSize();

        // TODO: remove this before pushing these changes to prod
        if (size < n) {
            Set<String> complexAcsFromSolr = searchResult.getElements().stream()
                    .map(ComplexSearchResults::getComplexAC)
                    .collect(Collectors.toSet());

            String[] queries = query.split("[,\\s]");
            for (String singleQuery: queries) {
                String trimmedQuery = singleQuery.trim();
                if (trimmedQuery.matches("CPX-[0-9]+")) {
                    if (!complexAcsFromSolr.contains(trimmedQuery)) {
                        IntactComplex complex = intactDao.getComplexDao().getLatestComplexVersionByComplexAc(trimmedQuery);
                        if (complex != null) {
                            searchResult.add(mapComplex(complex));
                        }
                    }
                }
            }
        }

        return searchResult;
    }

    // This method is to force to query only for a list of fields
    protected String improveQuery(String query, List<String> fields) {
        StringBuilder improvedQuery = new StringBuilder();
        for ( String field : fields ) {
            improvedQuery.append(field)
                    .append(":(")
                    .append(query)
                    .append(")");
        }
        return improvedQuery.toString();
    }

    protected void enableCORS(HttpHeaders headers) {
        headers.add("Access-Control-Allow-Origin", "*");
        headers.add("Access-Control-Allow-Methods", "GET");
        headers.add("Access-Control-Max-Age", "3600");
        headers.add("Access-Control-Allow-Headers", "x-requested-with");
    }

    private void enableClacks(HttpServletResponse response) {
        response.addHeader("X-Clacks-Overhead", "GNU Terry Pratchett"); //In memory of Sir Terry Pratchett
    }

    private ComplexSearchResults mapComplex(IntactComplex complex) {
        ComplexSearchResults complexSearchResults = new ComplexSearchResults();
        complexSearchResults.setComplexAC(complex.getComplexAc());
        complexSearchResults.setComplexName(IntactComplexUtils.getComplexName(complex));
        complexSearchResults.setOrganismName(IntactComplexUtils.getSpeciesName(complex));
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

    @ExceptionHandler(SolrServerException.class)
    public ModelAndView handleSolrServerException(SolrServerException e, HttpServletResponse response){
        ModelAndView model = new ModelAndView("error/503");
        response.setStatus(503);
        return model;
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleAllExceptions(Exception e, HttpServletResponse response){
        ModelAndView model = new ModelAndView("error/404");
        response.setStatus(404);
        return model;
    }

}
