package entities;

/* This is base class for every creature in game */
public class Pawn extends Entity {
	
	private int maxHealth = 0;
	private int currentHealth = 0;
	private String subName = "";
	private int normalSpeed = 0;
	private int currentSpeed = 0;
	
	public Pawn(){
	}

	public int getMaxHealth() {
		return maxHealth;
	}

	public void setMaxHealth(int maxHealth) {
		this.maxHealth = maxHealth;
	}

	public String getSubName() {
		return subName;
	}

	public void setSubName(String subName) {
		this.subName = subName;
	}

	public int getCurrentHealth() {
		return currentHealth;
	}

	public void setCurrentHealth(int currentHealth) {
		this.currentHealth = currentHealth;
	}

	public int getNormalSpeed() {
		return normalSpeed;
	}

	public void setNormalSpeed(int normalSpeed) {
		this.normalSpeed = normalSpeed;
	}

	public int getCurrentSpeed() {
		return currentSpeed;
	}

	public void setCurrentSpeed(int currentSpeed) {
		this.currentSpeed = currentSpeed;
	}
}
