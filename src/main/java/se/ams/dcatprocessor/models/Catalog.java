// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.models;

import java.util.ArrayList;
import java.util.List;

public class Catalog extends DataClass {
    public String fileName;
    public DataClass publisher;
    public List<DataClass> spatial = new ArrayList<>();
    public DataClass rights;
}


