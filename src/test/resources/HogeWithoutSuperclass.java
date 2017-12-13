package com.github.duck8823;

/**
 * Created by maeda on 2016/01/09.
 */
@GenerateNames(findSuperclass = false, createAsMethods = true, createAsFields = false)
public class HogeWithoutSuperclass extends AbstractHoge {

	private String hoge;

	@Override
	public String toString() {
		return "hoge";
	}
}
