// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.models;

import java.util.ArrayList;
import java.util.List;

public class FileStorage extends DataClass {
    public String fileName;
    public List<DataService> dataService = new ArrayList<>();
    public List<DataSet> dcat_dataset = new ArrayList<>();
    public List<DatasetSeries> dcat_datasetSeries = new ArrayList<>();
}