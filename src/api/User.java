package api;

import java.io.Serializable;

public class User implements Serializable {

	String id;

	Double money;



	public User(String id, Double money) {
		this.id = id;
		this.money = money;
	}

	public User() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Double getMoney() {
		return money;
	}

	public void setMoney(Double money) {
		this.money = money;
	}

	public void addMoney(Double amount) {
		money += amount;
	}

}
