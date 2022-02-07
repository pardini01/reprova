package br.ufmg.engsoft.reprova.model;

import java.util.List;

public class Subject {
	public final String id;
	public final String name;
	public final String code;
	public final List<String> themes;
	public final String description;

	public Subject(String id, String name, String code, List<String> themes, String description) {
		this.id = id;
		this.name = name;
		this.code = code;
		this.themes = themes;
		this.description = description;
	}
}
