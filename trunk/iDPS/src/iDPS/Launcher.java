package iDPS;

import javax.swing.JOptionPane;

import iDPS.gear.Enchant;
import iDPS.gear.Item;
import iDPS.gear.Setup;
import iDPS.gear.Gem;
import iDPS.gear.Armor;
import iDPS.gui.MainFrame;

public class Launcher {
	
	public static void main(String[] args) {
		
		String version = System.getProperty("java.version");
		System.out.println("Java version "+version+" detected.");
		char major = version.charAt(2);
		if (major < '6') {
		  System.err.println("Java 6 required");
		  JOptionPane.showMessageDialog(null, "Java 6 (1.6) required\r\nYou are using Java "+version, "Your Java Version is too old", JOptionPane.ERROR_MESSAGE); 
		  return;
		}
		
		if (System.getProperty("os.name").startsWith("Mac")) {
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			System.setProperty("com.apple.mrj.application.apple.menu.about.name", "iDPS");
		}
		MainFrame mf = MainFrame.getInstance();
		Persistency.createXML();
		
		Race.load();
		System.out.println("Races loaded.");
		Talents.load();
		System.out.println("Talents loaded.");
		Armor.load();
		System.out.println("Items loaded.");
		Gem.load();
		System.out.println("Gems loaded.");
		Enchant.load();
		System.out.println("Enchants loaded.");
		Setup.load();
		System.out.println("Setups loaded.");
		
		Item.loadFilter();
		mf.getMyMenuBar().createFilterMenu();
		Armor.limit();
		System.out.println("Filters loaded.");
		
		mf.showGear();
		mf.getMyMenuBar().checkSetup(mf.getSetup());
	}

}
