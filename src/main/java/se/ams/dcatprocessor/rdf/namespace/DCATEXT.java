// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf.namespace;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCAT;

/**
 * Extension of org.eclipse.rdf4j.model.vocabulary.DCAT since some
 * classes are missing
 * 
 * {@link org.eclipse.rdf4j.model.vocabulary.DCAT}
 * 
 * @author nacbr
 *
 */
public class DCATEXT extends DCAT {

	// Classes
	/** dcat:hasVersion */
	public static final IRI HAS_VERSION;

	static {
		HAS_VERSION = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "hasVersion");
	}
}
