// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.models;

import java.util.ArrayList;
import java.util.List;

public class Distribution extends DataClass {
	public List<DataService> dataServices = new ArrayList<>();
	public List<DataClass> conformsTo = new ArrayList<>();
	public DataClass checksum;
	public DataClass rights;
}


