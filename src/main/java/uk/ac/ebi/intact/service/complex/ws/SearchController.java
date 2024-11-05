package uk.ac.ebi.intact.service.complex.ws;

import lombok.extern.log4j.Log4j;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.util.URIUtil;
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
import psidev.psi.mi.jami.xml.PsiXmlVersion;
import uk.ac.ebi.complex.service.ComplexFinderResult;
import uk.ac.ebi.intact.dataexchange.psimi.solr.complex.ComplexSearchResults;
import uk.ac.ebi.intact.dataexchange.psimi.xml.IntactPsiXml;
import uk.ac.ebi.intact.export.complex.tab.exception.ComplexExportException;
import uk.ac.ebi.intact.export.complex.tab.writer.ComplexFlatWriter;
import uk.ac.ebi.intact.jami.dao.IntactDao;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;
import uk.ac.ebi.intact.service.complex.ws.model.ComplexDetails;
import uk.ac.ebi.intact.service.complex.ws.model.ComplexRestResult;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Log4j
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

    private IntactDao intactDao;
    private ComplexManager complexManager;

    @Autowired
    public SearchController(@Qualifier("intactDao") IntactDao intactDao,
                            ComplexManager complexManager) {
        this.intactDao = intactDao;
        this.complexManager = complexManager;
    }

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
        long total = complexManager.query(q, null, null, null, null).getTotalNumberOfResults();
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
    @RequestMapping(value = "/search/{query:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
	public ResponseEntity<String> search(@PathVariable String query,
                                    @RequestParam (required = false) String first,
                                    @RequestParam (required = false) String number,
                                    @RequestParam (required = false) String filters,
                                    @RequestParam (required = false) String facets,
                                    HttpServletResponse response) throws SolrServerException, IOException {
        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        ComplexRestResult searchResult = complexManager.query(query, first, number, filters, facets);
        mapper.writeValue(writer, searchResult);
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
            ComplexSearchResults enrichedSearchResult = complexManager.getComplexSearchResultsFromSolrOrDb(ac);
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

        ComplexDetails details = complexManager.createComplexDetails(complex);

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

        ComplexDetails details = complexManager.createComplexDetails(complex);

        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(writer, details);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.add("X-Clacks-Overhead", "GNU Terry Pratchett"); //In memory of Sir Terry Pratchett

        enableCORS(headers);

        return new ResponseEntity<String>(writer.toString(), headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/export/{query}", method = RequestMethod.GET)
    @Transactional(readOnly = true, propagation = Propagation.REQUIRED, value = "jamiTransactionManager")
    public ResponseEntity<String> exportComplex(@PathVariable String query,
                                                @RequestParam (required = false) String filters,
                                                @RequestParam (required = false) String format,
                                                @RequestParam (required = false) String filename,
                                                HttpServletResponse response) throws Exception {

        List<IntactComplex> complexes;
        if (isQueryASingleId(query)) {
            complexes = new ArrayList<IntactComplex>(1);

            IntactComplex complex;
            if (query.startsWith("EBI-")) {
                complex = intactDao.getComplexDao().getByAc(query);
            } else {
                complex = intactDao.getComplexDao().getLatestComplexVersionByComplexAc(query);

            }

            if (complex != null)
                complexes.add(complex);
        } else {
            ComplexRestResult searchResult = complexManager.query(query, null, null, filters, null);
            complexes = new ArrayList<IntactComplex>(searchResult.getElements().size());
            for (ComplexSearchResults result : searchResult.getElements()) {
                IntactComplex complex = intactDao.getComplexDao().getLatestComplexVersionByComplexAc(result.getComplexAC());
                if (complex != null)
                    complexes.add(complex);
            }
        }

        ResponseEntity<String> responseEntity = null;
        if (!complexes.isEmpty()) {
            InteractionWriterFactory writerFactory = InteractionWriterFactory.getInstance();
            if (format != null) {
                switch (ComplexExportFormat.formatOf(format)) {
                    case XML25:
                        responseEntity = createXml25Response(complexes, writerFactory, filename);
                        break;
                    case XML30:
                        responseEntity = createXml30Response(complexes, writerFactory, filename);
                        break;
                    case TSV:
                        responseEntity = createComplexTabResponse(complexes, filename);
                        break;
                    case JSON:
                    default:
                        responseEntity = createJsonResponse(complexes, writerFactory, filename);
                        break;
                }
            } else {
                responseEntity = createJsonResponse(complexes, writerFactory, filename);
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

        ComplexFinderResult<ComplexDetails> complexFinderResult = complexManager.findComplexWithMatchingProteins(parsedProteinAcs);

        StringWriter writer = new StringWriter();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(writer, complexFinderResult);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
        headers.add("X-Clacks-Overhead", "GNU Terry Pratchett"); //In memory of Sir Terry Pratchett

        enableCORS(headers);

        return new ResponseEntity<>(writer.toString(), headers, HttpStatus.OK);
    }

    private boolean isQueryASingleId(String query) {
        return (query.startsWith("EBI-") || query.startsWith("CPX-")) && query.trim().split(" ").length == 1;
    }

    private ResponseEntity<String> createXml25Response(List<IntactComplex> complexes, InteractionWriterFactory writerFactory, String filename) {
        return createXmlResponse(complexes, writerFactory, PsiXmlVersion.v2_5_4, filename);
    }

    private ResponseEntity<String> createXml30Response(List<IntactComplex> complexes, InteractionWriterFactory writerFactory, String filename) {
        return createXmlResponse(complexes, writerFactory, PsiXmlVersion.v3_0_0, filename);
    }

    private ResponseEntity<String> createXmlResponse(List<IntactComplex> complexes, InteractionWriterFactory writerFactory, PsiXmlVersion version, String filename) {
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
        if (filename != null && !filename.isEmpty()) {
            httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        }
        return new ResponseEntity<String>(answer.toString(), httpHeaders, HttpStatus.OK);
    }

    private ResponseEntity<String> createJsonResponse(List<IntactComplex> complexes, InteractionWriterFactory writerFactory, String filename) {

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
        if (filename != null && !filename.isEmpty()) {
            httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        }
        return new ResponseEntity<String>(answer.toString(), httpHeaders, HttpStatus.OK);
    }

    private ResponseEntity<String> createComplexTabResponse(List<IntactComplex> complexes, String filename) throws IOException, ComplexExportException {
        StringWriter answer = new StringWriter();
        ComplexFlatWriter complexFlatWriter = new ComplexFlatWriter(answer);
        for (IntactComplex complex : complexes) {
            complexFlatWriter.writeComplex(complex);
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", MediaType.APPLICATION_JSON_UTF8_VALUE);
        httpHeaders.add("X-Clacks-Overhead", "GNU Terry Pratchett"); //In memory of Sir Terry Pratchett
        enableCORS(httpHeaders);
        if (filename != null && !filename.isEmpty()) {
            httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        }
        return new ResponseEntity<>(answer.toString(), httpHeaders, HttpStatus.OK);
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
