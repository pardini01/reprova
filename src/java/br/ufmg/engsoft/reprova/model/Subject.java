package br.ufmg.engsoft.reprova.model;

public class Subject {
	public final String id;
	public final String name;
	public final String code;
	public final String theme;
	public final String description;

	public Subject(String name, String code, String theme, String description) {
		this.id = null;
		this.name = name;
		this.code = code;
		this.theme = theme;
		this.description = description;
	}
}
