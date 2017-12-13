package com.github.duck8823;

/**
 * Created by maeda on 2016/01/09.
 */
@GenerateNames(suffix = "Meta", createAsMethods = true, createAsFields = false)
public class HogeChangingSuffix extends AbstractHoge {

	private String hoge;

	@Override
	public String toString() {
		return "hoge";
	}
}
