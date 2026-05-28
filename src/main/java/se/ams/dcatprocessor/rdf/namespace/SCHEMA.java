// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf.namespace;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Constants for the Schema namespace
 * 
 * @see <a href="https://schema.org/docs/developers.html">Schema.org for Developers</a>
 * 
 * @author nacbr
 *
 */
public class SCHEMA {

	/**
	 * Recommended prefix for the Schema namespace: "schema"
	 */
	public static final String PREFIX = "schema";
	
	/**
	 * The Schema namespace: https://schema.org/
	 */
	public static final String NAMESPACE = "http://schema.org/";
	
	/**
	 * An immutable {@link Namespace} constant that represents the Schema Vocabulary namespace.
	 */
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);

	// Classes
	/** schema:Offer */
	public static final IRI OFFER;

	/** schema:offers */
	public static final IRI OFFERS;

	/** schema:mainEntityOfPage */
	public static final IRI MAIN_ENTITY_OF_PAGE;

	/** schema:description */
	public static final IRI DESCRIPTION;

	static {
		OFFER = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "Offer");
		OFFERS = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "offers");
		MAIN_ENTITY_OF_PAGE = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "mainEntityOfPage");
		DESCRIPTION = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "description");
	}
}
