/*
 * 	   Created by Daniel Nadeau
 * 	   daniel.nadeau01@gmail.com
 * 	   danielnadeau.blogspot.com
 * 
 * 	   Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package com.echo.holographlibrary;

import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Region;

public class Bar {
	private int mColor = 0;
	private String mName = "";
	private double mValue = 0;
	private String mValueString = null;
	private Path mPath = null;
	private Region mRegion = null;
	
	public int getColor() {
		return mColor;
	}
	public Bar setColor(int color) {
		mColor = color;
        return this;
	}
    public Bar setColor(String color) {
        mColor = Color.parseColor(color);
        return this;
    }
	public String getName() {
		return mName;
	}
	public Bar setName(String name) {
		this.mName = name;
        return this;
	}
	public double getValue() {
		return mValue;
	}
	public Bar setValue(double value) {
		this.mValue = value;
        return this;
	}
	
	public String getValueString()
	{
		if (mValueString != null) {
			return mValueString;
		} else {
			return String.valueOf(mValue);
		}
	}
	
	public Bar setValueString(final String valueString)
	{
		mValueString = valueString;
        return this;
	}
	
	public Path getPath() {
		return mPath;
	}
	public Bar setPath(Path path) {
		this.mPath = path;
        return this;
	}
	public Region getRegion() {
		return mRegion;
	}
	public Bar setRegion(Region region) {
		this.mRegion = region;
        return this;
	}
	
}
