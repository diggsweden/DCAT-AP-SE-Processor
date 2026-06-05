// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.models;

import java.util.ArrayList;
import java.util.List;

public class Organization extends DataClass{
    public List<DataClass> adress = new ArrayList<>();
    public List<DataClass> phone = new ArrayList<>();
}

