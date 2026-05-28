// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.models;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

public class DataClass {
    public String about;
    public MultiValuedMap<String, String> dcData = new ArrayListValuedHashMap<>();
    public List<DataClass> documents = new ArrayList<>();
    public List<DataClass> licenseDocuments = new ArrayList<>();
    public List<DataClass> agents = new ArrayList<>();
    public DataClass agent;
}