package iDPS.gui.menu;

import iDPS.Race;
import iDPS.gear.Enchant;
import iDPS.gear.Setup;
import iDPS.gear.Gem;
import iDPS.gear.Setup.Profession;
import iDPS.gui.MainFrame;
import iDPS.gui.OSXAdapter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

public class MenuBar extends JMenuBar implements ActionListener {
	
	private MainFrame mainFrame;
	
	private JMenu mSetup;
	private ItemSelectGear[] iSetups;
	private JMenuItem iGearNew, iGearRename, iGearDup, iGearSave, iGearDel, iImportArmory;
	
	private JMenu mRaces;
	private ItemSelectRace[] iRaces;
	private ButtonGroup gRaces;
	
	private JMenu mProfessions;
	private ItemSelectProfession[] iProfessions;
	
	private JMenu mFilter;
		
	public MenuBar(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
		
		boolean MAC_OS_X = (System.getProperty("os.name").toLowerCase().startsWith("mac os x"));
        if (MAC_OS_X) {
            try {
                // Generate and register the OSXAdapter, passing it a hash of all the methods we wish to
                // use as delegates for various com.apple.eawt.ApplicationListener methods
                OSXAdapter.setQuitHandler(mainFrame.getApp(), mainFrame.getApp().getClass().getDeclaredMethod("exit", (Class[]) null));
                //OSXAdapter.setAboutHandler(this, getClass().getDeclaredMethod("about", (Class[])null));
                //OSXAdapter.setPreferencesHandler(this, getClass().getDeclaredMethod("preferences", (Class[])null));
                //OSXAdapter.setFileHandler(this, getClass().getDeclaredMethod("loadImageFile", new Class[] { String.class }));
            } catch (Exception e) {
                System.err.println("Error while loading the OSXAdapter:");
                e.printStackTrace();
            }
        }
		
		mSetup = new JMenu("Setups");
		createGearMenu();
		add(mSetup);
		
		mRaces = new JMenu("Race");
		createRacesMenu();
		add(mRaces);
		
		mProfessions = new JMenu("Professions");
		createProfessionsMenu();
		add(mProfessions);
		
		mFilter = new MenuFilter(mainFrame.getApp().getFilterController());
		add(mFilter);
	}
	
	public void createGearMenu() {
		mSetup.removeAll();
		
		iGearNew = new JMenuItem("New");
		iGearNew.addActionListener(this);
		mSetup.add(iGearNew);
		
		iGearDel = new JMenuItem("Delete");
		iGearDel.addActionListener(this);
		mSetup.add(iGearDel);
		
		iGearRename = new JMenuItem("Rename");
		iGearRename.addActionListener(this);
		mSetup.add(iGearRename);
		
		iGearDup = new JMenuItem("Duplicate");
		iGearDup.addActionListener(this);
		mSetup.add(iGearDup);
		
	  iImportArmory = new JMenuItem("Armory...");
	  iImportArmory.addActionListener(this);
	  mSetup.add(iImportArmory);
		
		mSetup.addSeparator();
		
		iGearSave = new JMenuItem("Save all");
		iGearSave.addActionListener(this);
		mSetup.add(iGearSave);
		
		mSetup.addSeparator();
		
		ArrayList<Setup> setups = Setup.getAll();
		Setup curSetup = mainFrame.getApp().getSetup();
		Collections.sort(setups);
		iSetups = new ItemSelectGear[setups.size()];
		ButtonGroup group = new ButtonGroup();
		for (int i=0; i<setups.size(); i++) {
			iSetups[i] = new ItemSelectGear(setups.get(i));
			if (curSetup == setups.get(i))
				iSetups[i].setSelected(true);
			group.add(iSetups[i]);
			mSetup.add(iSetups[i]);
		}
		
		iGearDel.setEnabled(Setup.getAll().size()>1);
	}
	
	public void createRacesMenu() {
		mRaces.removeAll();
		
		ArrayList<Race> races = Race.getAll();
		iRaces = new ItemSelectRace[races.size()];
		gRaces = new ButtonGroup();
		for (int i=0; i<races.size(); i++) {
			iRaces[i] = new ItemSelectRace(races.get(i));
			gRaces.add(iRaces[i]);
			mRaces.add(iRaces[i]);
		}
	}
	
	public void createProfessionsMenu() {
		mProfessions.removeAll();
		
		iProfessions = new ItemSelectProfession[Profession.values().length];
		for (int i=0; i<Profession.values().length; i++) {
			iProfessions[i] = new ItemSelectProfession(Profession.values()[i]);
			mProfessions.add(iProfessions[i]);
		}
	}
	
	public void checkSetup(Setup setup) {
		for (ItemSelectGear iSetup: iSetups)
			iSetup.setSelected(iSetup.getSetup() == setup);
		
		gRaces.clearSelection();
		for (ItemSelectRace iRace: iRaces)
			gRaces.setSelected(iRace.getModel(), (iRace.getRace() == setup.getRace()));
		
		for (ItemSelectProfession iProfession: iProfessions)
			iProfession.setSelected(setup.hasProfession(iProfession.getProfession()));
	}
	
	private void selectGearSetup(Setup setup) {
		mainFrame.getApp().setSetup(setup);
		mainFrame.showGear();
		checkSetup(setup);
	}
	
	private void selectRace(Race race) {
		mainFrame.getApp().getSetup().setRace(race);
		mainFrame.showStats();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == iGearSave)
			mainFrame.getApp().saveAllSetups();
		else if (e.getSource() == iGearDup) {
			Setup g = new Setup(mainFrame.getApp().getSetup());
			g.clearId();
			String s = (String) JOptionPane.showInputDialog(
					mainFrame,
					null, "enter Name", JOptionPane.PLAIN_MESSAGE,
					null, null, g.getName()+" Copy");
			if (s == null || s.trim().isEmpty())
				return;
			g.setName(s);
			Setup.add(g);
			createGearMenu();
		}
		else if (e.getSource() == iGearDel) {
			int really = JOptionPane.showConfirmDialog(
					mainFrame,
					null,
					"Delete Gear Configuration '"+mainFrame.getApp().getSetup().getName()+"'",
					JOptionPane.YES_NO_OPTION);
			if (really == JOptionPane.OK_OPTION) {
				Setup.remove(mainFrame.getApp().getSetup());
				createGearMenu();
				revalidate();
				ArrayList<Setup> gears = Setup.getAll();
				Collections.sort(gears);
				selectGearSetup(gears.get(0));
			}
		}
		else if (e.getSource() == iGearNew) {
			String s = (String) JOptionPane.showInputDialog(
					mainFrame,
					null,
					"enter Name",
					JOptionPane.PLAIN_MESSAGE);
			if (s == null || s.trim().isEmpty())
				return;
			Setup g = new Setup(s.trim());
			Setup.add(g);
			createGearMenu();
			revalidate();
		}
		else if (e.getSource() == iGearRename) {
			String s = (String) JOptionPane.showInputDialog(
					mainFrame,
					null,
					"enter new name",
					JOptionPane.PLAIN_MESSAGE,
					null,
					null,
					mainFrame.getApp().getSetup().getName());
			if (s == null || s.trim().isEmpty())
				return;
			mainFrame.getApp().getSetup().setName(s.trim());
			createGearMenu();
			revalidate();
		}
		else if (e.getSource() == iImportArmory) {
			mainFrame.createAndShowImportFrame();
		}
	}
	
	private class ItemSelectGear extends JRadioButtonMenuItem implements ActionListener {
		
		private Setup setup;
		
		public ItemSelectGear(Setup setup) {
			super(setup.getName());
			this.setup = setup;
			addActionListener(this);
		}
		
		public Setup getSetup() {
			return setup;
		}

		public void actionPerformed(ActionEvent e) {
			selectGearSetup(setup);
		}
		
	}
	
	private class ItemSelectRace extends JRadioButtonMenuItem implements ActionListener {
		
		private Race race;
		
		public ItemSelectRace(Race race) {
			super(race.getName());
			this.race = race;
			addActionListener(this);
		}
		
		public Race getRace() {
			return race;
		}

		public void actionPerformed(ActionEvent e) {
			selectRace(race);
		}
		
	}
	
	private class ItemSelectProfession extends JCheckBoxMenuItem implements ActionListener {
		
		private Profession profession;
		
		public ItemSelectProfession(Profession profession) {
			super(profession.name());
			this.profession = profession;
			addActionListener(this);
		}
		
		public Profession getProfession() {
			return profession;
		}

		public void actionPerformed(ActionEvent e) {
			Setup setup = mainFrame.getApp().getSetup();
			setup.setProfession(profession, isSelected());
			
			switch (profession) {
				case Blacksmithing:
					mainFrame.showGear();
					break;
				case Enchanting:
				case Engineering:
				case Inscription:
				case Leatherworking:
				case Tailoring:
					Enchant.limit();
					break;
				case Jewelcrafting:
					Gem.limit();
					break;
				case Alchemy:
				case Skinning:
					mainFrame.showStats();
					break;
			}
		}
		
	}
	


}
