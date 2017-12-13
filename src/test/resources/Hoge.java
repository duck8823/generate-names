package com.github.duck8823;

/**
 * Created by maeda on 2016/01/09.
 */
@GenerateNames(createAsFields = false, createAsMethods = true)
public class Hoge extends AbstractHoge {

	private String hoge;

	@Override
	public String toString() {
		return "hoge";
	}
}
