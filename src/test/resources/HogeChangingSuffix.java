package com.duck8823;

import com.duck8823.AbstractHoge;
import com.duck8823.GenerateNames;

/**
 * Created by maeda on 2016/01/09.
 */
@GenerateNames(suffix = "Meta")
public class HogeChangingSuffix extends AbstractHoge {

	private String hoge;

	@Override
	public String toString() {
		return "hoge";
	}
}
