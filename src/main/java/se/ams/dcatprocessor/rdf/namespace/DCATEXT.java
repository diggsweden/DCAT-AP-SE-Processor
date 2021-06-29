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

	/** dcat:isVersionOf */
	public static final IRI IS_VERSION_OF;

	static {
		HAS_VERSION = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "hasVersion");
		IS_VERSION_OF = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "isVersionOf");
	}
}
