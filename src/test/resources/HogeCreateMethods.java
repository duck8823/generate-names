package com.github.duck8823;

/**
 * Created by maeda on 2018/05/26.
 */
@GenerateNames(findMethods = true, findSuperclass = false, createAsMethods = true)
public class HogeCreateMethods extends AbstractHoge {

	private String hoge;

	@Override
	public String toString() {
		return "hoge";
	}
}
