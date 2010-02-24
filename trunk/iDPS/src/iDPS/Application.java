package iDPS;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import iDPS.BuffController.Debuff;
import iDPS.gear.Enchant;
import iDPS.gear.Gem;
import iDPS.gear.Setup;
import iDPS.gui.MainFrame;

public class Application {
	
	private MainFrame mainFrame;
	
	private final PropertyChangeSupport pcs;
	private final BuffController buffController;
	private final FilterController filterController;
	private final TalentsController talentsController;
	private final GlyphsController glyphsController;
	
	private Setup setup;
	private boolean useTotT;
	private boolean useRupture;
	private boolean useExpose;
	
	public Application() {
		pcs = new PropertyChangeSupport(this);
		// Create Buff Controller
		buffController = new BuffController(this);
		// Create Filter Controller
		filterController = new FilterController();
		// Create Talents Controller
		talentsController = new TalentsController(this);
		// Create Glyphs Controller
		glyphsController = new GlyphsController(this);
	}
	
	public void initialize() {
		Talents.load();
		System.out.println("Talents generated.");
		
		Setup.load(this);
		System.out.println("Setups loaded.");
		
		// Load Filters
		filterController.load();
		System.out.println("Filters loaded.");
		
		// Start GUI
		mainFrame = new MainFrame(this);
		mainFrame.getMyMenuBar().createGearMenu();
		mainFrame.getMyMenuBar().createRacesMenu();
		
		mainFrame.showGear();
		mainFrame.getMyMenuBar().checkSetup(getSetup());
	}
	
	public BuffController getBuffController() {
		return buffController;
	}
	
	public FilterController getFilterController() {
		return filterController;
	}
	
	public TalentsController getTalentsController() {
		return talentsController;
	}
	
	public GlyphsController getGlyphsController() {
		return glyphsController;
	}
	
	public Setup getSetup() {
		return setup;
	}
	
	public void setSetup(Setup newSetup) {
		Setup oldSetup = this.setup;
		this.setup = newSetup;
		pcs.firePropertyChange("setup", oldSetup, newSetup);
		setUseRupture(setup.isUseRupture());
		setUseTotT(setup.isUseTotT());
		setUseExpose(setup.isUseExpose());
		// Limit Gems and Enchants to our Professions
		Gem.limit();
		Enchant.limit();
	}
	
	public boolean getUseRupture() {
		return useRupture;
	}
	
	public void setUseRupture(boolean newValue) {
		boolean oldValue = useRupture;
		useRupture = newValue;
		if (setup != null)
			setup.setUseRupture(newValue);
		pcs.firePropertyChange("useRupture", oldValue, newValue);
	}
	
	public boolean getUseTotT() {
		return useTotT;
	}
	
	public void setUseTotT(boolean newValue) {
		boolean oldValue = useTotT;
		useTotT = newValue;
		if (setup != null)
			setup.setUseTotT(newValue);
		pcs.firePropertyChange("useTotT", oldValue, newValue);
	}
	
	public boolean getUseExpose() {
		return useExpose;
	}
	
	public void setUseExpose(boolean newValue) {
		boolean oldValue = useExpose;
		useExpose = newValue;
		if (setup != null)
			setup.setUseExpose(newValue);
		pcs.firePropertyChange("useExpose", oldValue, newValue);
		if (newValue) {
			buffController.setDebuff(Debuff.armorMajor, newValue);
		}
	}
	
	public void saveAllSetups() {
		Setup.save(this);
	}
	
	public void exit() {
		System.out.println("Bye!");
		filterController.save();
		System.exit(0);
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

	public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

}
