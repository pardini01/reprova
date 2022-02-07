package br.ufmg.engsoft.reprova.model;

public class ReprovaClass {
	public final String id;
	public final String code;
	public final String subject;
	public final String semester;
	
	public ReprovaClass(String code, String subject, String semester) {
		this.id = null;
		this.code = code;
		this.subject = subject;
		this.semester = semester;
	}
}
