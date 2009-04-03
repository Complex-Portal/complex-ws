/**
 * Copyright 2008 The European Bioinformatics Institute, and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.intact.view.webapp.controller.browse;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import uk.ac.ebi.intact.dataexchange.psimi.solr.ontology.OntologySearcher;
import uk.ac.ebi.intact.view.webapp.util.RootTerm;


/**
 * TODO comment that class header
 *
 * @author Prem Anand (prem@ebi.ac.uk)
 * @version $Id$
 * @since 2.0.1-SNAPSHOT
 */
@Controller( "chebiBrowser" )
@Scope( "request" )
public class ChebiBrowserController extends OntologyBrowserController {

    public static final String FIELD_NAME = "chebi_expanded_id";

    @Override
    protected RootTerm createRootTerm(OntologySearcher ontologySearcher) {
        final RootTerm rootTerm = new RootTerm( ontologySearcher, "ChEBI Ontology" );
        rootTerm.addChild("CHEBI:36342", "Subatomic particle");
        rootTerm.addChild("CHEBI:24431", "Molecular structure");
        rootTerm.addChild("CHEBI:50906", "Role");

        return rootTerm;
    }

    @Override
    protected String getFieldName() {
        return FIELD_NAME;
    }
}
