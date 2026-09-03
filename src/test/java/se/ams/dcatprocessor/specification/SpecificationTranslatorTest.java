// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import se.ams.dcatprocessor.rdf.DcatException;

class SpecificationTranslatorTest {

	private final SpecificationTranslator translator = new SpecificationTranslator();

	private Map<String, DcatProperty> translate(String json) throws Exception {
		return translator.translate(new ObjectMapper().readTree(json));
	}

    @Test
	void testThatTranslateReadsAllFieldsOfANode() throws Exception {
		String json = """
			{"templates":[
			  {"id":"parent","items":["child",{"property":"dcat:centroid","datatype":"geo:wktLiteral"}]},
			  {"id":"child","property":"http://www.w3.org/ns/adms#status","nodetype":"URI",
			   "cardinality":{"min":1,"max":1},"pattern":"https?://.+",
			   "choices":[{"value":"a","label":{"sv":"A"}},{"value":"b"}]}
			]}""";

		Map<String, DcatProperty> nodes = translate(json);
		DcatProperty child = nodes.get("child");

		assertEquals(2, nodes.size());
		assertEquals("adms:status", child.getProperty());
		assertEquals("URI", child.getNodetype());
		assertEquals(1, child.getCardinality().getMin());
		assertEquals(1, child.getCardinality().getMax());
		assertEquals("https?://.+", child.getPattern());
		assertEquals(List.of("a", "b"), child.getChoices());

		assertSame(child, nodes.get("parent").getItems().get(0));

		DcatProperty inline = nodes.get("parent").getItems().get(1);
		assertNull(inline.getId());
		assertEquals(List.of("geo:wktLiteral"), inline.getDatatype());
	}

	@Test
	void testThatTranslateFailsWhenBundleHasNoTemplates() {
		DcatException e = assertThrows(DcatException.class, () -> translate("{}"));
		assertTrue(e.getMessage().contains("no templates"));
	}

	@Test
	void testThatTranslateFailsWhenTemplateHasNoId() {
        String json = """
            {"templates":[{"property":"dcterms:title"}]}""";

		assertThrows(DcatException.class, () -> translate(json));
	}

	@Test
	void testThatTranslateFailsOnDuplicateId() {
        String json = """
            {"templates":[{"id":"a"},{"id":"a"}]}""";

		assertThrows(DcatException.class, () -> translate(json));
	}

	@Test
	void testThatTranslateFailsOnUnknownField() {
        String json = """
            {"templates":[{"id":"a","newField":"x"}]}""";

		DcatException e = assertThrows(DcatException.class, () -> translate(json));
		assertTrue(e.getMessage().contains("newField"));
	}

	@Test
	void testThatTranslateFailsWhenAnItemReferencesAnUnknownId() {
        String json = """
            {"templates":[{"id":"parent","items":["missing"]}]}""";

		DcatException e = assertThrows(DcatException.class, () -> translate(json));
		assertTrue(e.getMessage().contains("missing"));
	}
}
