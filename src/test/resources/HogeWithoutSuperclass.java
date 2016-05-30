package net.duck8823;

/**
 * Created by maeda on 2016/01/09.
 */
@GenerateNames(findSuperclass = false)
public class HogeWithoutSuperclass extends AbstractHoge {

	private String hoge;

	@Override
	public String toString() {
		return "hoge";
	}
}
