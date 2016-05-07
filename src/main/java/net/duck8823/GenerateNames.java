package net.duck8823;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 名前クラスを作成する
 * Created by maeda on 5/3/2016.
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface GenerateNames {

	/**
	 * 生成されるクラスの接尾辞<br/>
	 * デフォルト: Names
	 */
	String suffix() default "Names";

	/**
	 * スーパークラスを対象にするかどうか
	 */
	boolean findSuperclass() default true;
}
