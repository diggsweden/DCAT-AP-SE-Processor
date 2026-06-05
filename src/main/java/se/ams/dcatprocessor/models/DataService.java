// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.models;

import java.util.ArrayList;
import java.util.List;


public class DataService extends DataClass {
    public List<Organization> organizations = new ArrayList<>();
    public List<DataClass> conformsTo = new ArrayList<>();
    public DataClass agent;
}