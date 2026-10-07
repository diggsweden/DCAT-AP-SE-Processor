// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.specification;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import se.ams.dcatprocessor.testutil.TestHelper;

public class DcatSpecificationTest {

    private static DcatSpecification specification;

    @BeforeAll
    static void setup() throws IOException {
        SpecificationLoader specificationLoader = new SpecificationLoader(TestHelper.bundlePathFromApplicationProperties());
        specification = new DcatSpecification(specificationLoader);
    }
    
    @Test
    void testThatCardinalityOfTakesCardinalityFromExtendedNode() {
        DcatProperty node = specification.node("dcatap:hvdCategory_da");

        DcatCardinality cardinality = specification.cardinalityOf(node);

        assertEquals(1, cardinality.getMin());
    }

    @Test
    void testThatCardinalityOfNodeWithoutCardinalityIsOptionalUnboundAndNotRecommended() {
        DcatProperty node = specification.node("dcat:dcterms:hasPart_ca");

        DcatCardinality cardinality = specification.cardinalityOf(node);

        assertEquals(0, cardinality.getMin());
        assertEquals(DcatCardinality.MAX, cardinality.getMax());
        assertEquals(0, cardinality.getPref());
    }

    @Test
    void testThatRecommendedForDatasetContainsContactPoint() {
        List<DcatProperty> recommended = specification.recommendedFor("dcat:Dataset");

        assertTrue(containsId(recommended, "dcat:contactPoint_da"));
    }

    @Test
    void testThatRecommendedForDatasetDoesNotContainMandatory() {
        List<DcatProperty> recommended = specification.recommendedFor("dcat:Dataset");

        assertFalse(containsId(recommended, "dcat:dcterms:title_da"));
    }

    private static boolean containsId(List<DcatProperty> nodes, String id) {
        for (DcatProperty node : nodes) {
            if (id.equals(node.getId())) {
                return true;
            }
        }
        return false;
    }
}
