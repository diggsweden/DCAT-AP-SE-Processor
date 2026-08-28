// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import se.ams.dcatprocessor.rdf.DcatException;

public class SpecificationLoaderTest {

    private static Map<String, DcatProperty> nodes;

    @BeforeAll  
    private static void setup(){
        nodes = new SpecificationLoader().load();
    }

    @Test
    void testThatLoadReadsEveryTemplate() {    
        //DCAT-AP-SE v3.0.1 holds 164 nodes.
        assertEquals(164, nodes.size());
    }

    @Test
    void testThatReadsChoicesFindsAllChoices() {
        // hvdCategory has 96 choices
        assertEquals(96, nodes.get("dcatap:hvdCategory").getChoices().size());
        
        // hvdCategory should have http://data.europa.eu/bna/c_dd313021 as a choice
        assertTrue(nodes.get("dcatap:hvdCategory").getChoices().contains("http://data.europa.eu/bna/c_dd313021"));

        // dcat:format-group_common_choice should have text/csv
        assertTrue(nodes.get("dcat:format-group_common_choices").getChoices().contains("text/csv"));
    }

    @Test
    void testThatLoadFailsWhenBundleIsMissing() {
        DcatException e = assertThrows(DcatException.class,
                () -> new SpecificationLoader("no-such-bundle.json").load());

        assertTrue(e.getMessage().contains("no-such-bundle.json"));
    }

    @Test
    void testThatLoadFailsWhenBundleIsNotJson() {
        // application.properties is not a json file
        DcatException e = assertThrows(DcatException.class,
                () -> new SpecificationLoader("application.properties").load());

        assertTrue(e.getMessage().contains("application.properties"));
    }
}