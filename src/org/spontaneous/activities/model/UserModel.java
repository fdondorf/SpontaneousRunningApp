package org.spontaneous.activities.model;

/**
 * Model containing the data of a user
 * @author fdondorf
 *
 */
public class UserModel {

	private Long id;
	private String firstname;
	private String lastname;
	private String email;
	private String password;
	
	public UserModel(Long id, String firstname, String lastname, 
			String email, String password) {
		super();
		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
		this.password = password;
	}

	public UserModel(Long id, String firstname, String lastname, 
			String email) {
		super();
		this.id = id;
		this.firstname = firstname;
		this.lastname = lastname;
		this.email = email;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFirstname() {
		return firstname;
	}

	public void setFirstname(String firstname) {
		this.firstname = firstname;
	}

	public String getLastname() {
		return lastname;
	}

	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	
}
