// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.controller;

public class Result {
    private String result;

    public Result(String result) {
		this.result = result;
	}

	@Override
    public String toString() {
        return result;
    }
}
