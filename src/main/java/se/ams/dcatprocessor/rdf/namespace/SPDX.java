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
 * Constants for the SPDX (The Software Package Data Exchange) namespace
 * 
 * @see <a href="http://spdx.org/">The Software Package Data Exchange (SPDX)</a>
 * 
 * @author nacbr
 *
 */
public class SPDX {

	/**
	 * Recommended prefix for the SPDX namespace: "spdx"
	 */
	public static final String PREFIX = "spdx";
	
	/**
	 * The SPDX namespace: http://spdx.org/rdf/terms#
	 */
	public static final String NAMESPACE = "http://spdx.org/rdf/terms#";
	
	/**
	 * An immutable {@link Namespace} constant that represents the SPDX namespace.
	 */
	public static final Namespace NS = new SimpleNamespace(PREFIX, NAMESPACE);
	
	// Classes
	/** spdx:checksum */
	public static final IRI CHECKSUM;

	/** spdx:checksumValue */
	public static final IRI CHECKSUM_VALUE;

	/** spdx:algorithm */
	public static final IRI ALGORITHM;
	
	/** spdx:checksumAlgorithm_sha1 */
	public static final IRI CHECKSUM_ALGORITHM_SHA1;
	
	//Add more below as needed
	
	static {
		CHECKSUM = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "checksum");
		CHECKSUM_VALUE = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "checksumValue");
		ALGORITHM = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "algorithm");
		CHECKSUM_ALGORITHM_SHA1 = SimpleValueFactory.getInstance().createIRI(NAMESPACE, "checksumAlgorithm_sha1");
	}
}
