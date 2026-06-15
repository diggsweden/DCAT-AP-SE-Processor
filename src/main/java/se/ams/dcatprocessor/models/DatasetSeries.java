// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2
 
package se.ams.dcatprocessor.models;
 
import java.util.ArrayList;
import java.util.List;
 
public class DatasetSeries extends DataService {
    public List<DataClass> spatial = new ArrayList<>();
    public List<DataClass> temporals = new ArrayList<>();
    public List<DataClass> qualifiedRelations = new ArrayList<>();
}
