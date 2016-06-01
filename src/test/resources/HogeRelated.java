package com.duck8823;

import com.duck8823.*;

import javax.persistence.*;
import java.util.List;

/**
 * Created by maeda on 2016/01/09.
 */
@GenerateNames(createMethods = false, createFields = true)
@Entity
public class HogeRelated extends AbstractHoge {

	private String hoge;

	@Embedded
	private HogeEmbeddable hogeEmbeddable;

	@OneToOne
	private HogeOneToOne hogeOneToOne;

	@OneToMany
	private List<HogeOneToMany> hogeOneToMany;

	@ManyToOne
	private HogeManyToOne hogeManyToOne;

	@ManyToMany
	private List<HogeManyToMany> hogeManyToMany;

	@Override
	public String toString() {
		return "hoge";
	}
}
