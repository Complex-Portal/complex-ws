package uk.ac.ebi.intact.service.complex.ws;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum EvidenceTypeCode {

    ECO_0000353(
            "ECO:0000353",
            "physical interaction evidence",
            5),
    ECO_0005543(
            "ECO:0005543",
            "experimental evidence from mixed species ",
            5),
    ECO_0005610(
            "ECO:0005610",
            "inferred by homology",
            4),
    ECO_0005544(
            "ECO:0005544",
            "inferred by orthology ",
            4),
    ECO_0005546(
            "ECO:0005546",
            "inferred by paralogy ",
            4),
    ECO_0005547(
            "ECO:0005547",
            "inferred by curator",
            3),
    ECO_0007653(
            "ECO:0007653",
            "machine-learning predicted complex based on combinatorial evidence",
            2),
    ECO_0008004(
            "ECO:0008004",
            "machine-learning predicted complex",
            1);


    private final String ecoCode;
    private final String displayLabel;
    private final Integer confidenceScore;

    public static EvidenceTypeCode getEvidenceTypeCode(String ecoCode) {
        return Arrays.stream(EvidenceTypeCode.values())
                .filter(evidenceTypeCode -> evidenceTypeCode.ecoCode.equals(ecoCode))
                .findFirst()
                .orElse(null);
    }

    public static Integer getConfidenceScore(String ecoCode) {
        return Arrays.stream(EvidenceTypeCode.values())
                .filter(evidenceTypeCode -> evidenceTypeCode.ecoCode.equals(ecoCode))
                .map(evidenceTypeCode -> evidenceTypeCode.confidenceScore)
                .findFirst()
                .orElse(null);
    }

    public static List<String> getEcoCodesForConfidenceScore(Integer confidenceScore) {
        return Arrays.stream(EvidenceTypeCode.values())
                .filter(evidenceTypeCode -> evidenceTypeCode.confidenceScore.equals(confidenceScore))
                .map(evidenceTypeCode -> evidenceTypeCode.ecoCode)
                .collect(Collectors.toList());
    }
}
