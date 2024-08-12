package uk.ac.ebi.intact.service.complex.ws.utils;

import psidev.psi.mi.jami.model.*;
import psidev.psi.mi.jami.utils.AliasUtils;
import psidev.psi.mi.jami.utils.AnnotationUtils;
import psidev.psi.mi.jami.utils.RangeUtils;
import uk.ac.ebi.intact.jami.model.extension.IntactComplex;
import uk.ac.ebi.intact.jami.model.extension.IntactInteractor;
import uk.ac.ebi.intact.jami.model.extension.IntactModelledParticipant;
import uk.ac.ebi.intact.jami.model.extension.IntactStoichiometry;
import uk.ac.ebi.intact.jami.model.extension.InteractorXref;
import uk.ac.ebi.intact.service.complex.ws.EvidenceTypeCode;
import uk.ac.ebi.intact.service.complex.ws.model.ComplexDetails;
import uk.ac.ebi.intact.service.complex.ws.model.ComplexDetailsCrossReferences;
import uk.ac.ebi.intact.service.complex.ws.model.ComplexDetailsEvidenceType;
import uk.ac.ebi.intact.service.complex.ws.model.ComplexDetailsFeatures;
import uk.ac.ebi.intact.service.complex.ws.model.ComplexDetailsParticipants;

import java.util.*;

/**
 * Created by maitesin on 09/12/2014.
 */
public class IntactComplexUtils {

    public static final String COMPLEX_PROPERTIES = "properties";
    public static final String COMPLEX_PROPERTIES_MI = "MI:0629";

    public static final String COMPLEX_DISEASE = "disease";
    public static final String COMPLEX_DISEASE_MI = "MI:0617";

    public static final String COMPLEX_LIGAND = "ligand";
    public static final String COMPLEX_LIGAND_IA = "IA:2738";

    public static final String COMPLEX_ASSEMBLY = "complex-assembly";
    public static final String COMPLEX_ASSEMBLY_IA = "IA:2783";

    public static final String CURATED_COMPLEX = "curated-complex";
    public static final String CURATED_COMPLEX_IA = "IA:0285";

    public static final String INTACT = "intact";
    public static final String INTACT_MI = "MI:0469";

    public static final String SEARCH = "search-url";
    public static final String SEARCH_MI = "MI:0615";

    public static final String RNA_CENTRAL = "RNAcentral";
    public static final String RNA_CENTRAL_MI = "MI:1357";

    public static final String COMPLEX_AGONIST = "agonist";
    public static final String COMPLEX_AGONIST_MI = "MI:0625";

    public static final String COMPLEX_ANTAGONIST = "antagonist";
    public static final String COMPLEX_ANTAGONIST_MI = "MI:0626";

    public static final String COMPLEX_COMMENT = "comment";
    public static final String COMPLEX_COMMENT_MI = "MI:0612";

    public static List<String> getComplexSynonyms(IntactComplex complex) {
        List<String> synosyms = new ArrayList<String>();
        for (Alias alias : AliasUtils.collectAllAliasesHavingType(complex.getAliases(), Alias.COMPLEX_SYNONYM_MI, Alias.COMPLEX_SYNONYM)) {
            synosyms.add(alias.getName());
        }
        return synosyms;
    }

    public static String getComplexName(IntactComplex complex) {
        String name = complex.getRecommendedName();
        if (name != null) return name;
        name = complex.getSystematicName();
        if (name != null) return name;
        List<String> synonyms = getComplexSynonyms(complex);
        if (!synonyms.isEmpty()) return synonyms.get(0);
        return complex.getShortName();
    }


    // This method fills the cross references table for the view
    public static void setCrossReferences(IntactComplex complex, ComplexDetails details) {
        Collection<ComplexDetailsCrossReferences> crossReferences = details.getCrossReferences();
        ComplexDetailsCrossReferences cross;
        for (Xref xref : complex.getXrefs()) {
            cross = createCrossReference(xref);
            crossReferences.add(cross);
        }
        for (Xref xref : complex.getIdentifiers()) {
            // We does not want to show us a cross references. That does not make sense.
            if (!xref.getDatabase().getShortName().equals(INTACT)) {
                cross = createCrossReference(xref);
                crossReferences.add(cross);
            }
        }
    }

    private static ComplexDetailsCrossReferences createCrossReference(Xref xref) {
        ComplexDetailsCrossReferences cross = new ComplexDetailsCrossReferences();
        if (xref.getDatabase() != null) {
            cross.setDatabase(xref.getDatabase().getFullName());
            if (xref.getDatabase() instanceof OntologyTerm) {
                OntologyTerm ontologyTerm = (OntologyTerm) xref.getDatabase();
                if (ontologyTerm.getDefinition() != null)
                    cross.setDbdefinition(ontologyTerm.getDefinition());
            }
            cross.setDbMI(xref.getDatabase().getMIIdentifier());
        }
        if (xref.getQualifier() != null) {
            cross.setQualifier(xref.getQualifier().getFullName());
            if (xref.getQualifier() instanceof OntologyTerm) {
                OntologyTerm ontologyTerm = (OntologyTerm) xref.getQualifier();
                if (ontologyTerm.getDefinition() != null)
                    cross.setQualifierDefinition(ontologyTerm.getDefinition());
            }
            cross.setQualifierMI(xref.getQualifier().getMIIdentifier());
        }
        cross.setIdentifier(xref.getId());
        String searchUrl = getSearchUrl(xref);
        if (searchUrl != null) {
            cross.setSearchURL(searchUrl);
        }
        if (xref instanceof InteractorXref) {
            InteractorXref interactorXref = (InteractorXref) xref;
            if (interactorXref.getSecondaryId() != null) cross.setDescription(interactorXref.getSecondaryId());
        }
        return cross;
    }

    private static String getSearchUrl(Xref xref) {
        Annotation searchUrl = AnnotationUtils.collectFirstAnnotationWithTopic(xref.getDatabase().getAnnotations(), SEARCH_MI, SEARCH);
        if (searchUrl != null) {
            if (xref.getId().startsWith("PR:")) {
                String modifiedIdentifier = xref.getId().replace("PR:", "");
                return searchUrl.getValue().replaceAll("\\$*\\{ac\\}", modifiedIdentifier);
            } else {
                return searchUrl.getValue().replaceAll("\\$*\\{ac\\}", xref.getId());
            }
        }
        return null;
    }

    public static ComplexDetailsEvidenceType createEvidenceType(CvTerm evidenceType) {
        if (evidenceType != null) {
            Optional<Xref> ecoCodeXrefOp = evidenceType.getIdentifiers().stream()
                    .filter(id -> ModelledInteraction.ECO_MI.equals(id.getDatabase().getMIIdentifier()))
                    .findFirst();
            if (ecoCodeXrefOp.isPresent()) {
                Xref ecoCodeXref = ecoCodeXrefOp.get();
                EvidenceTypeCode evidenceTypeCode = EvidenceTypeCode.getEvidenceTypeCode(ecoCodeXref.getId());
                if (evidenceTypeCode != null) {
                    ComplexDetailsEvidenceType complexDetailsEvidenceType = new ComplexDetailsEvidenceType(
                            evidenceTypeCode.getEcoCode(),
                            evidenceTypeCode.getDisplayLabel(),
                            evidenceTypeCode.getStars());
                    String searchUrl = getSearchUrl(ecoCodeXref);
                    if (searchUrl != null) {
                        complexDetailsEvidenceType.setSearchURL(searchUrl);
                    }
                    return complexDetailsEvidenceType;
                }

            }
        }
        return null;
    }

    // This method fills the participants table for the view
    public static void setParticipants(IntactComplex complex, ComplexDetails details) {
        Collection<ComplexDetailsParticipants> participants = details.getParticipants();
        ComplexDetailsParticipants part;
        for (ModelledParticipant participant : mergeParticipants(complex.getParticipants())) { //Use ModelledParticipant
            part = new ComplexDetailsParticipants();
            Interactor interactor = participant.getInteractor();

            if (interactor != null) {
                //TODO In principle this can now be replace by preferredName
                setInteractorType(part, interactor);
                part.setDescription(interactor.getFullName());
                part.setInteractorAC(((IntactModelledParticipant) participant).getAc());
                String identifier = getParticipantIdentifier(participant);
                part.setName(getParticipantName(participant));
                part.setIdentifier(identifier);
                part.setIdentifierLink(getParticipantIdentifierLink(participant, identifier));
                part.setStochiometry(getParticipantStoichiometry(participant));
                if (participant.getBiologicalRole() != null) {
                    setBiologicalRole(part, participant);
                }
            }
            setFeatures(part, participant);
            participants.add(part);
        }
    }

    public static Collection<ModelledParticipant> mergeParticipants(Collection<ModelledParticipant> participants) {
        if (participants.size() > 1) {
            Comparator<ModelledParticipant> comparator = Comparator.comparing(o -> ((IntactInteractor) o.getInteractor()).getAc());
            List<ModelledParticipant> participantList = (List<ModelledParticipant>) participants;
            participantList.sort(comparator);
            Collection<ModelledParticipant> merged = new ArrayList<>();
            ModelledParticipant aux = participantList.get(0);
            int minStochiometry = 0;
            int maxStochiometry = 0;
            for (ModelledParticipant participant : participantList) {
                if (((IntactInteractor) aux.getInteractor()).getAc().equals(((IntactInteractor) participant.getInteractor()).getAc())) {
                    //Same
                    minStochiometry += participant.getStoichiometry().getMinValue();
                    maxStochiometry += participant.getStoichiometry().getMaxValue();
                } else {
                    //Different
                    aux.setStoichiometry(new IntactStoichiometry(minStochiometry, maxStochiometry));
                    merged.add(aux);
                    aux = participant;
                    minStochiometry = aux.getStoichiometry().getMinValue();
                    maxStochiometry = aux.getStoichiometry().getMaxValue();
                }
            }
            aux.setStoichiometry(new IntactStoichiometry(minStochiometry, maxStochiometry));
            merged.add(aux);
            return merged;
        } else {
            return participants;
        }
    }

    // this method fills the linked features and the other features cells in the participants table
    protected static void setFeatures(ComplexDetailsParticipants part, Participant participant) {
        for (Feature feature : (List<Feature>) participant.getFeatures()) {
            if (feature.getLinkedFeatures().size() != 0) {
                for (Feature linked : (List<Feature>) feature.getLinkedFeatures()) {
                    ComplexDetailsFeatures complexDetailsFeatures = createFeature(linked);
                    part.getLinkedFeatures().add(complexDetailsFeatures);
                }
            } else {
                ComplexDetailsFeatures complexDetailsFeatures = createFeature(feature);
                part.getOtherFeatures().add(complexDetailsFeatures);
            }
        }
    }

    private static ComplexDetailsFeatures createFeature(Feature feature) {
        ComplexDetailsFeatures complexDetailsFeatures = new ComplexDetailsFeatures();
        complexDetailsFeatures.setFeatureType(feature.getType().getShortName());
        if (feature.getType() instanceof OntologyTerm) {
            OntologyTerm ontologyTerm = (OntologyTerm) feature.getType();
            if (ontologyTerm.getDefinition() != null)
                complexDetailsFeatures.setFeatureTypeDefinition(ontologyTerm.getDefinition());
        }
        complexDetailsFeatures.setFeatureTypeMI(feature.getType().getMIIdentifier());
        complexDetailsFeatures.setParticipantId(feature.getParticipant().getInteractor().getPreferredIdentifier().getId());
        for (Range range : (List<Range>) feature.getRanges()) {
            complexDetailsFeatures.getRanges().add(RangeUtils.convertRangeToString(range));
        }
        return complexDetailsFeatures;
    }

    // This method sets the interactor type information
    protected static void setInteractorType(ComplexDetailsParticipants part, Interactor interactor) {
        CvTerm term = interactor.getInteractorType();
        part.setInteractorType(term.getFullName());
        part.setInteractorTypeMI(term.getMIIdentifier());
        if (term instanceof OntologyTerm) {
            OntologyTerm ontologyTerm = (OntologyTerm) term;
            if (ontologyTerm.getDefinition() != null)
                part.setInteractorTypeDefinition(ontologyTerm.getDefinition());
        }
    }

    // This method sets the biological role information
    protected static void setBiologicalRole(ComplexDetailsParticipants part, Participant participant) {
        CvTerm term = participant.getBiologicalRole();
        part.setBioRole(term.getFullName());
        part.setBioRoleMI(term.getMIIdentifier());
        if (term instanceof OntologyTerm) {
            OntologyTerm ontologyTerm = (OntologyTerm) term;
            if (ontologyTerm.getDefinition() != null)
                part.setBioRoleDefinition(ontologyTerm.getDefinition());
        }
    }

    //
    // ALIASES
    //
    public static String getSystematicName(IntactComplex complex) {
        return complex.getSystematicName();
    }

    //Retrieve all the synosyms of the complex
    public static List<String> getSynonyms(IntactComplex complex) {
        List<String> synosyms = new ArrayList<String>();
        for (Alias alias : AliasUtils.collectAllAliasesHavingType(complex.getAliases(), Alias.COMPLEX_SYNONYM_MI, Alias.COMPLEX_SYNONYM)) {
            synosyms.add(alias.getName());
        }
        return synosyms;
    }

    public static String getName(IntactComplex complex) {
        String name = complex.getRecommendedName();
        if (name != null) return name;
        name = complex.getSystematicName();
        if (name != null) return name;
        List<String> synonyms = getSynonyms(complex);
        if (synonyms != Collections.EMPTY_LIST) return synonyms.get(0);
        return complex.getShortName();
    }

    //
    // SPECIES
    //
    //
    public static String getSpeciesName(IntactComplex complex) {
        return complex.getOrganism().getScientificName();
    }

    public static String getSpeciesTaxId(IntactComplex complex) {
        return Integer.toString(complex.getOrganism().getTaxId());
    }

    //
    // ANNOTATIONS
    //
    public static List<String> getProperties(IntactComplex complex) {
        Collection<Annotation> annotations = AnnotationUtils.collectAllAnnotationsHavingTopic(complex.getAnnotations(), COMPLEX_PROPERTIES_MI, COMPLEX_PROPERTIES);
        if (annotations != null) {
            List<String> properties = new ArrayList<String>();
            for (Annotation annotation : annotations) {
                properties.add(annotation.getValue());
            }
            return properties;
        }
        return null;
    }

    public static List<String> getDiseases(IntactComplex complex) {
        Collection<Annotation> annotations = AnnotationUtils.collectAllAnnotationsHavingTopic(complex.getAnnotations(), COMPLEX_DISEASE_MI, COMPLEX_DISEASE);
        if (annotations != null) {
            List<String> diseases = new ArrayList<String>();
            for (Annotation annotation : annotations) {
                diseases.add(annotation.getValue());
            }
            return diseases;
        }
        return null;
    }

    public static List<String> getLigands(IntactComplex complex) {
        Collection<Annotation> annotations = AnnotationUtils.collectAllAnnotationsHavingTopic(complex.getAnnotations(), COMPLEX_LIGAND_IA, COMPLEX_LIGAND);
        if (annotations != null) {
            List<String> ligands = new ArrayList<String>();
            for (Annotation annotation : annotations) {
                ligands.add(annotation.getValue());
            }
            return ligands;
        }
        return null;
    }

    public static List<String> getComplexAssemblies(IntactComplex complex) {
        Collection<Annotation> annotations = AnnotationUtils.collectAllAnnotationsHavingTopic(complex.getAnnotations(), COMPLEX_ASSEMBLY_IA, COMPLEX_ASSEMBLY);
        if (annotations != null) {
            List<String> complexAssemblies = new ArrayList<String>();
            for (Annotation annotation : annotations) {
                complexAssemblies.add(annotation.getValue());
            }
            return complexAssemblies;
        }
        return null;
    }

    public static List<String> getFunctions(IntactComplex complex) {
        Collection<Annotation> annotations = AnnotationUtils.collectAllAnnotationsHavingTopic(complex.getAnnotations(), CURATED_COMPLEX_IA, CURATED_COMPLEX);
        if (annotations != null) {
            List<String> functions = new ArrayList<String>();
            for (Annotation annotation : annotations) {
                functions.add(annotation.getValue());
            }
            return functions;
        }
        return null;
    }

    public static List<String> getAgonists(IntactComplex complex) {
        Collection<Annotation> annotations = AnnotationUtils.collectAllAnnotationsHavingTopic(complex.getAnnotations(), COMPLEX_AGONIST_MI, COMPLEX_AGONIST);
        if (annotations != null) {
            List<String> agonists = new ArrayList<String>();
            for (Annotation annotation : annotations) {
                agonists.add(annotation.getValue());
            }
            return agonists;
        }
        return null;
    }

    public static List<String> getAntagonists(IntactComplex complex) {
        Collection<Annotation> annotations = AnnotationUtils.collectAllAnnotationsHavingTopic(complex.getAnnotations(), COMPLEX_ANTAGONIST_MI, COMPLEX_ANTAGONIST);
        if (annotations != null) {
            List<String> antagonists = new ArrayList<String>();
            for (Annotation annotation : annotations) {
                antagonists.add(annotation.getValue());
            }
            return antagonists;
        }
        return null;
    }

    public static List<String> getComments(IntactComplex complex) {
        Collection<Annotation> annotations = AnnotationUtils.collectAllAnnotationsHavingTopic(complex.getAnnotations(), COMPLEX_COMMENT_MI, COMPLEX_COMMENT);
        if (annotations != null) {
            List<String> comments = new ArrayList<String>();
            for (Annotation annotation : annotations) {
                comments.add(annotation.getValue());
            }
            return comments;
        }
        return null;
    }

    public static String getParticipantName(ModelledParticipant participant) {
        Interactor interactor = participant.getInteractor();
        if (interactor != null) {
            if (interactor instanceof Protein) {
                Protein protein = (Protein) interactor;
                return protein.getGeneName() != null ? protein.getGeneName(): protein.getPreferredName();
            } else if (interactor instanceof BioactiveEntity) {
                BioactiveEntity bioactiveEntity = (BioactiveEntity) interactor;
                return bioactiveEntity.getShortName();
            } else if (interactor instanceof Complex) {
                Complex complexParticipant = (Complex) interactor;
                return complexParticipant.getRecommendedName();
            } else {
                for (Xref x : interactor.getIdentifiers()) {
                    if (x.getDatabase().getMIIdentifier().equals(RNA_CENTRAL_MI)) {
                        return interactor.getShortName();
                    }
                }
            }
            return interactor.getShortName();
        }
        return null;
    }

    public static String getParticipantIdentifier(ModelledParticipant participant) {
        Interactor interactor = participant.getInteractor();
        if (interactor != null) {
            if (interactor instanceof Protein) {
                Protein protein = (Protein) interactor;
                return protein.getPreferredIdentifier().getId();
            } else if (interactor instanceof BioactiveEntity) {
                BioactiveEntity bioactiveEntity = (BioactiveEntity) interactor;
                return bioactiveEntity.getChebi();
            } else if (interactor instanceof Complex) {
                Complex complexParticipant = (Complex) interactor;
                return complexParticipant.getComplexAc();
            } else {
                for (Xref x : interactor.getIdentifiers()) {
                    if (x.getDatabase().getMIIdentifier().equals(RNA_CENTRAL_MI)) {
                        return x.getId();
                    }
                }
            }
            return interactor.getPreferredIdentifier().getId();
        }
        return null;
    }

    public static String getParticipantIdentifierLink(ModelledParticipant participant, String identifier) {
        Interactor interactor = participant.getInteractor();
        if (interactor != null && identifier != null) {
            Annotation searchUrl = AnnotationUtils.collectFirstAnnotationWithTopic(interactor.getPreferredIdentifier().getDatabase().getAnnotations(), SEARCH_MI, SEARCH);
            if (searchUrl != null) {
                return searchUrl.getValue().replaceAll("\\$*\\{ac\\}", identifier);
            }
        }
        return null;
    }

    public static String getParticipantStoichiometry(ModelledParticipant participant) {
        if (participant.getStoichiometry().getMinValue() != 0 || participant.getStoichiometry().getMaxValue() != 0) {
            return participant.getStoichiometry().toString();
        }
        return null;
    }
}
