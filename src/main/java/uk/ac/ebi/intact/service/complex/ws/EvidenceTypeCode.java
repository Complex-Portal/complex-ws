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
    ECO_0000314(
            "ECO:0000314",
            "direct assay evidence used in manual assertion",
            5),
    ECO_0000269(
            "ECO:0000269",
            "experimental evidence used in manual assertion",
            5),
    ECO_0005543(
            "ECO:0005543",
            "mixed species evidence inference",
            5),
    ECO_0005610(
            "ECO:0005610",
            "homology inference",
            4),
    ECO_0005544(
            "ECO:0005544",
            "orthology inference",
            4),
    ECO_0005546(
            "ECO:0005546",
            "paralogy inference",
            4),
    ECO_0005547(
            "ECO:0005547",
            "curator inference",
            3),
    ECO_0007653(
            "ECO:0007653",
            "automatically integrated combinatorial prediction",
            2),
    ECO_0008004(
            "ECO:0008004",
            "machine learning prediction",
            1);


    private final String ecoCode;
    private final String displayLabel;
    private final Integer stars;

    public static EvidenceTypeCode getEvidenceTypeCode(String ecoCode) {
        return Arrays.stream(EvidenceTypeCode.values())
                .filter(evidenceTypeCode -> evidenceTypeCode.ecoCode.equals(ecoCode))
                .findFirst()
                .orElse(null);
    }

    public static Integer getStars(String ecoCode) {
        return Arrays.stream(EvidenceTypeCode.values())
                .filter(evidenceTypeCode -> evidenceTypeCode.ecoCode.equals(ecoCode))
                .map(evidenceTypeCode -> evidenceTypeCode.stars)
                .findFirst()
                .orElse(null);
    }

    public static List<String> getEcoCodesForStar(Integer stars) {
        return Arrays.stream(EvidenceTypeCode.values())
                .filter(evidenceTypeCode -> evidenceTypeCode.stars.equals(stars))
                .map(evidenceTypeCode -> evidenceTypeCode.ecoCode)
                .collect(Collectors.toList());
    }
}
