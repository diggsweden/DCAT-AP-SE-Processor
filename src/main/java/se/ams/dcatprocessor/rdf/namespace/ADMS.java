// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf.namespace;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Constants for the ADMS (Asset Description Metadata Schema) namespace
 * 
 * @see <a href="http://www.w3.org/ns/adms">Asset Description Metadata Schema (ADMS)</a>
 * 
 * @author nacbr
 *
 */
public class ADMS {

	/**
	 * Recommended prefix for the ADMS namespace: "adms"
	 */
	public static final String PREFIX = "adms";
	
	/**
	 * The ADMS namespace: http://www.w3.org/ns/adms/
	 */
	public static final String NAMESPACE = "http://www.w3.org/ns/adms#";
	
	/**
	 * An immutable {@link Namespace} constant that represents the ADMS namespace.
	 */
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);

	// Classes
	/** adms:identifier */
	public static final IRI IDENTIFIER;

	/** adms:status */
	public static final IRI STATUS;
	
	/** adms:versionNotes */
	public static final IRI VERSION_NOTES;
	
	//Add more below as needed
	
	static {
		IDENTIFIER = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "identifier");
		STATUS = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "status");
		VERSION_NOTES = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "versionNotes");
	}
}
