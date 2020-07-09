package org.sk.utils.logging;

public class TabFormatter implements Formatter {

	private int tabLevel = 0;
	private String tabSymbol = "\t";

	public void incTabLevel() {
		tabLevel++;
	}

	public void decTabLevel() {
		tabLevel--;
	}

	public int getTabLevel() {
		return tabLevel;
	}

	public void setTabLevel(int tabLevel) {
		this.tabLevel = tabLevel;
	}

	public String getTabSymbol() {
		return tabSymbol;
	}

	public void setTabSymbol(String tabSymbol) {
		this.tabSymbol = tabSymbol;
	}

	@Override
	public void format(StringBuilder message) {
		for (int i = 0; i < tabLevel; i++) {
			message.append(tabSymbol);
		}
	}

}
