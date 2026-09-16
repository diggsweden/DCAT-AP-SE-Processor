// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.models;

/*
 * One raw API specification as submitted by the caller: the name of the file together with
 * the unparsed file content (JSON, RAML or YAML).
 */
public record ApiSource(String name, String content) { }
