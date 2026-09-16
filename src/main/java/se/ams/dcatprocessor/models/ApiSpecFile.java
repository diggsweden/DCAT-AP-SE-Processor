// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.models;

import org.json.JSONObject;

/*
 * One parsed API specification: the DCAT metadata together with the name of the file it came from.
 * Content of a file is always converted to JSON before being processed by a Converter.
 */

public record ApiSpecFile(String name, JSONObject content) { }
