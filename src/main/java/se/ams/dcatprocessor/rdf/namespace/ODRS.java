/*
 * This file is part of dcat-ap-se-processor.
 *
 * dcat-ap-se-processor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * dcat-ap-se-processor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with dcat-ap-se-processor.  If not, see <https://www.gnu.org/licenses/>.
 */

package se.ams.dcatprocessor.rdf.namespace;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Namespace;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * Constants for the ODRS (Open Data Rights Statement Vocabulary) namespace
 * 
 * @see <a href="http://schema.theodi.org/odrs/">Open Data Rights Statement Vocabulary (ODRS)</a>
 * 
 * @author nacbr
 *
 */
public class ODRS {

	/**
	 * Recommended prefix for the ODRS namespace: "odrs"
	 */
	public static final String PREFIX = "odrs";
	
	/**
	 * The ODRS namespace: http://schema.theodi.org/odrs#
	 */
	public static final String NAMESPACE = "http://schema.theodi.org/odrs#";
	
	/**
	 * An immutable {@link Namespace} constant that represents the ODRS namespace.
	 */
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);

	// Classes
	/** odrs:attributionText */
	public static final IRI ATTRIBUTION_TEXT;

	/** odrs:attributionURL */
	public static final IRI ATTRIBUTION_URL;
	
	/** odrs:copyrightNotice */
	public static final IRI COPYRIGHT_NOTICE;
	
	/** odrs:copyrightStatement */
	public static final IRI COPYRIGHT_STATEMENT;
	
	/** odrs:copyrightYear */
	public static final IRI COPYRIGHT_YEAR;
	
	/** odrs:copyrightHolder */
	public static final IRI COPYRIGHT_HOLDER;
	
	/** odrs:jurisdiction */
	public static final IRI JURISDICTION;
	
	/** odrs:reuserGuidelines */
	public static final IRI REUSER_GUIDELINES;
	
	/** odrs:RightsStatement */
	public static final IRI RIGHTS_STATEMENT;
	
	//Add more below as needed

	static {
		ATTRIBUTION_TEXT = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "attributionText");
		ATTRIBUTION_URL = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "attributionURL");
		COPYRIGHT_NOTICE = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "copyrightNotice");
		COPYRIGHT_STATEMENT = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "copyrightStatement");
		COPYRIGHT_YEAR = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "copyrightYear");
		COPYRIGHT_HOLDER = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "copyrightHolder");
		JURISDICTION = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "jurisdiction");
		REUSER_GUIDELINES = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "reuserGuidelines");
		RIGHTS_STATEMENT = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "RightsStatement");
	}
}
