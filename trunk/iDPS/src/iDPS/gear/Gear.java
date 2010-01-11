package iDPS.gear;

import iDPS.Attributes;
import iDPS.Persistency;
import iDPS.Player;
import iDPS.model.Calculations;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;


public class Gear implements Comparable<Gear> {
	
	private static HashMap<Integer,Gear> map = null;
	private static int nextFreeId = 1;
	
	private Attributes attr;
	private HashMap<Integer,Integer> containsMap;
	private Enchant[] enchants;
	private Gem[][] gems;
	private int id = 0;
	private Item[] items;
	private String name;
	
	private boolean[] socketBonus;
	
	private EnumMap<Item.Tier,Integer> tiers;
	private HashMap<String, int[]> uniqueMap;
	
	public Gear() {
		items = new Item[19];
		gems = new Gem[19][3];
		enchants = new Enchant[19];
		socketBonus = new boolean[19];
		attr = new Attributes();
		containsMap = new HashMap<Integer,Integer>();
		uniqueMap = new HashMap<String,int[]>();
		tiers = new EnumMap<Item.Tier,Integer>(Item.Tier.class);
		for (Item.Tier t: Item.Tier.values())
			tiers.put(t, 0);
	}
	
	@SuppressWarnings("unchecked")
	public Gear(Element element) {
		this();
		try {
			id = Integer.parseInt(element.getAttributeValue("id"));
		} catch(NumberFormatException e) {
			id = 0;
		}
		if (id >= Gear.nextFreeId)
			Gear.nextFreeId = id+1;
		Iterator<Element> iter = element.getChildren().iterator();
		while (iter.hasNext()) {
			Element eGear = iter.next();
			String s = eGear.getName();
			if (s.equals("name"))
				name = eGear.getText();
			else if (s.equals("items")) {
				Iterator<Element> iter2 = eGear.getChildren().iterator();
				while (iter2.hasNext()) {
					Element eItem = iter2.next();
					int iid = Integer.parseInt(eItem.getAttributeValue("id"));
					int slot = Integer.parseInt(eItem.getAttributeValue("slot"));
					Item item = Item.find(iid);
					if (item!=null)
						setItem(slot, item);
					Iterator<Element> iter3 = eItem.getChildren().iterator();
					while (iter3.hasNext()) {
						Element eGem = iter3.next();
						int index = Integer.parseInt(eGem.getAttributeValue("index"));
						int gemId = Integer.parseInt(eGem.getAttributeValue("id"));
						setGem(slot,index,Gem.find(gemId));
					}
				}
			} else if (s.equals("enchants")) {
				Iterator<Element> iter2 = eGear.getChildren().iterator();
				while (iter2.hasNext()) {
					Element eEnchant = iter2.next();
					int id = Integer.parseInt(eEnchant.getAttributeValue("id"));
					int slot = Integer.parseInt(eEnchant.getAttributeValue("slot"));
					Enchant enchant = Enchant.find(id);
					if (enchant!=null)
						setEnchant(slot, enchant);
				}
			}
		}
		//calcAttr();
	}
	
	public Gear(Gear copy) {
		this();
		id = copy.id;
		name = copy.name;
		items = copy.items.clone();
		gems = copy.gems.clone();
		for (int i=0; i<gems.length; i++)
			gems[i] = copy.gems[i].clone();
		enchants = copy.enchants.clone();
		socketBonus = copy.socketBonus.clone();
		attr = copy.attr.clone();
		containsMap = new HashMap<Integer,Integer>(copy.containsMap);
		uniqueMap = new HashMap<String,int[]>();
		for (String s: copy.uniqueMap.keySet()) {
			int[] vect = copy.uniqueMap.get(s).clone();
			uniqueMap.put(s, vect);
		}
		tiers = new EnumMap<Item.Tier,Integer>(copy.tiers);
	}
	
	public Gear(String name) {
		this();
		if (map == null)
			Gear.load();
		
		this.id = Gear.nextFreeId;
		Gear.nextFreeId++;
		this.name = name;
	}
	
	public boolean canAdd(Gem g) {
		if (g.getUniqueLimit()>0) {
			int[] vect;
			if (uniqueMap.containsKey(g.getUniqueName())) {
				vect = uniqueMap.get(g.getUniqueName());
				if (vect[0] >= vect[1])
					return false;
			}
		}
		return true;
	}
	
	public void reset() {
		for (int i=0; i<=18; i++) {
			setItem(i, null);
			setEnchant(i, null);
		}
	}
	
	public Gear clone() {
		return new Gear(this);
	}
	
	public int compareTo(Gear o) {
		return name.compareTo(o.name);
	}
	
	public int contains(int id) {
		if (containsMap.containsKey(id))
			return containsMap.get(id);
		return 0;
	}
	
	public boolean containsAny(int... ids) {
		for (int id: ids) {
			if (containsMap.containsKey(id))
				return true;
		}
		return false;
	}
	
	private void containsDec(Gem g) {
		if (g.getUniqueLimit()>0) {
			int[] vect;
			if (uniqueMap.containsKey(g.getUniqueName())) {
				vect = uniqueMap.get(g.getUniqueName());
				if (vect[0] > 0)
					vect[0]--;
			}
		}
		if (containsMap.containsKey(g.getId())) {
			int c = containsMap.get(g.getId())-1;
			if (c>0)
				containsMap.put(g.getId(), c--);
			else
				containsMap.remove(g.getId());
		}
	}
	
	private void containsDec(int id) {
		if (containsMap.containsKey(id)) {
			int c = containsMap.get(id)-1;
			if (c>0)
				containsMap.put(id, c--);
			else
				containsMap.remove(id);
		}
	}
	
	private boolean containsInc(Gem g) {
		if (g.getUniqueLimit()>0) {
			int[] vect;
			if (uniqueMap.containsKey(g.getUniqueName())) {
				vect = uniqueMap.get(g.getUniqueName());
				if (vect[0] >= vect[1])
					return false;
				vect[0]++;
			} else {
				vect = new int[2];
				vect[0] = 1;
				vect[1] = g.getUniqueLimit();
				uniqueMap.put(g.getUniqueName(), vect);
			}
		}
		int c = (containsMap.containsKey(g.getId())) ? containsMap.get(g.getId()) : 0;
		c++;
		containsMap.put(id, c);
		return true;
	}
	
	/*private Attributes getEnchant(SlotType st) {
		Attributes attr = new Attributes();
		switch (st) {
			case Head:
				attr.incAtp(50);
				attr.incCri(20);
				break;
			case Shoulder:
				attr.incAtp(40);
				attr.incCri(15);
				break;
			case Back:
				attr.incAgi(22);
				break;
			case Chest:
				attr.incAgi(10);
				attr.incStr(10);
				break;
			case Wrist:
				attr.incAtp(130);
				break;
			case Hands:
				attr.incAtp(44);
				break;
			case Legs:
				attr.incAtp(75);
				attr.incCri(22);
				break;
			case Feet:
				attr.incHit(12);
				attr.incCri(12);
				break;
		}
		return attr;
	}*/
	
	private void containsInc(int id) {
		int c = (containsMap.containsKey(id)) ? containsMap.get(id) : 0;
		c++;
		containsMap.put(id, c);
	}
	
	public void gemBest(int slot) {
		Calculations m = Calculations.createInstance();
		Gear gear;
		Item item = getItem(slot);
		if (!item.hasSockets())
			return;
		m.calcEP();
		Gem[] gemsAny = new Gem[3], gemsMatch = new Gem[3], gemsTemp, gemsFinal;
		GemComparison gc;
		float dpsAny = 0, dpsMatch = 0;
		// Iteration 1: Any Color
		gear = this.clone();
		for (int index=0; index<=2; index++) {
			if (item.getSocket(index) == null)
				continue;
			gc = new GemComparison(this, slot, index, true);
			gear.setGem(slot, index, gc.getBestGem());
			gemsAny[index] = gc.getBestGem();
		}
		m.calculate(gear);
		dpsAny = m.getTotalDPS();
		if (isSocketBonusActive(slot))
			dpsMatch = dpsAny;
		else {
			// Iteration 2: Matching Color
			int[][] runs;
			if (this.getItem(slot).getMaxSocketIndex() == 1)
				runs = new int[][] {{0}};
			if (this.getItem(slot).getMaxSocketIndex() == 2)
				runs = new int[][] {{0,1},{1,0}};
			else
				runs = new int[][] {{0,1,2},{0,2,1},{1,0,2},{1,2,0},{2,1,0},{2,0,1}};
			for (int[] run: runs) {
				gemsTemp = new Gem[3];
				gear = this.clone();
				for (int index: run) {
					if (item.getSocket(index) == null)
						continue;
					gc = new GemComparison(this, slot, index, false);
					gear.setGem(slot, index, gc.getBestGem());
					gemsTemp[index] = gc.getBestGem();
				}
				m.calculate(gear);
				if (m.getTotalDPS() > dpsMatch) {
					dpsMatch = m.getTotalDPS();
					gemsMatch = gemsTemp;
				}
			}
		}
		// Pick Best Setup
		if (dpsAny > dpsMatch)
			gemsFinal = gemsAny;
		else
			gemsFinal =  gemsMatch;
		// Apply Gems
		for (int index=0; index<=2; index++) {
			if (gemsFinal[index] == null)
				continue;
			setGem(slot, index, gemsFinal[index]);
		}
	}
	
	public Attributes getAttributes() {
		return attr;
	}
	
	public Enchant getEnchant(int slot) {
		return enchants[slot];
	}
	
	public Gem getGem(int slot, int index) {
		return gems[slot][index];
	}
	
	public Gem[] getGems(int slot) {
		return gems[slot];
	}
	
	public int getId() {
		return id;
	}
	
	public Item getItem(int slot) {
		return items[slot];
	}
	
	public String getName() {
		return name;
	}
	
	public int getTier10() {
		return tiers.get(Item.Tier.Tier10);
	}
	
	public int getTier9() {
		return tiers.get(Item.Tier.Tier9);
	}
	
	public Weapon getWeapon1() {
		Weapon weapon = (Weapon) getItem(16);
		if (weapon == null)
			weapon = new Weapon(Weapon.weaponType.Dagger);
		return weapon;
	}
	
	public Weapon getWeapon2() {
		Weapon weapon = (Weapon) getItem(17);
		if (weapon == null)
			weapon = new Weapon(Weapon.weaponType.Dagger);
		return weapon;
	}
	
	public boolean hasChaoticESD() {
		return (contains(41398)>0);
	}
	
	public boolean isEnchanted(int slot) {
		return (enchants[slot]!=null);
	}
	
	public boolean isSocketBonusActive(int slot) {
		if (!getItem(slot).hasSockets())
			return false;
		Gem[] gems = getGems(slot);
		Socket socket;
		int index = 0;
		do {
			socket = getItem(slot).getSocket(index);
			if (gems[index] == null || !gems[index].isMatch(socket))
				return false;
			index++;
		} while (socket != null);
		return true;
	}
	
	public void print() {
		System.out.println(this);
		for (int i=0; i<=18; i++) {
			if (items[i] != null)
				printItem(i);
			else
				System.out.format("Slot %02d: Empty%n", i);
		}
		System.out.println();
	}
	
	public void printItem(int slot) {
		Item item = getItem(slot);
		if (item == null)
			return;
		System.out.println("Slot "+slot+": "+item);
		for (int index=0; index<=2; index++) {
			Socket socket = item.getSocket(index);
			if (socket == null)
				continue;
			Gem gem = getGem(slot,index);
			if (gem == null)
				System.out.format("%15s: %s%n", socket.toString(), "Empty");
			else
				System.out.format("%15s: %s%n", socket.toString(), gem.toString());
		}
	}
	
	public void setEnchant(int slot, Enchant enchant) {
		Enchant oldenchant = enchants[slot];
		if (oldenchant != null)
			attr.sub(oldenchant.getAttributes());
		if (enchant != null)
			attr.add(enchant.getAttributes());
		enchants[slot] = enchant;
	}
	
	public void setGem(int slot, int index, Gem gem) {
		Item item = getItem(slot);
		if (item ==  null)
			return;
		Socket socket = item.getSocket(index);
		if (socket == null)
			return;
		Gem oldgem = getGem(slot, index);
		if (oldgem != null) {
			attr.sub(oldgem.getAttributes());
			if (socketBonus[slot])
				attr.sub(item.getSocketBonus());
			socketBonus[slot] = false;
			containsDec(oldgem);
			gems[slot][index] = null;
		}
		if (gem != null) {
			// Check if we can equip it
			if (containsInc(gem)) {
				attr.add(gem.getAttributes());
				if (gem.isMatch(socket)) {
					boolean bonus = true;
					Socket s; Gem g;
					for (int i=0; i<gems[slot].length; i++) {
						if (i==index)
							continue;
						s = item.getSocket(i);
						if (s == null) {
							bonus = bonus && i != 0;
							break;
						}
						g = gems[slot][i];
						bonus = g != null && g.isMatch(s);
						if (!bonus)
							break;
					}
					if (bonus)
						attr.add(item.getSocketBonus());
					socketBonus[slot] = bonus;
				}
				gems[slot][index] = gem;
			}
		}
	}
	
	public void setItem(int slot, Item item) {
		Item olditem = items[slot];
		Gem[] oldgems = getGems(slot).clone();
		if (olditem != null) {
			attr.sub(olditem.getAttributes());
			for (int index=0; index<=2; index++)
				setGem(slot, index, null);
			if (olditem.getTier() != null)
				tierDec(olditem.getTier());
			containsDec(olditem.getId());
			items[slot] = null;
		}
		if (item != null) {
			items[slot] = item;
			attr.add(item.getAttributes());
			for (int index=0; index<=item.getMaxSocketIndex(); index++)
				setGem(slot, index, oldgems[index]);
			if (item.getTier() != null)
				tierInc(item.getTier());
			containsInc(item.getId());
		}
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	private void tierDec(Item.Tier t) {
		int c = tiers.get(t);
		if (c > 0) {
			c--;
			tiers.put(t, c);
		}
	}
	
	private void tierInc(Item.Tier t) {
		int c = tiers.get(t);
		c++;
		tiers.put(t, c);
	}
	
	/*public String toString() {
		String s = "iDPS.Gear#"+id+"#"+this.;
		return s;
	}*/
	
	public static void add(Gear s) {
		if (map == null)
			Gear.load();
		if (s.id==0) {
			s.id = Gear.nextFreeId;
			Gear.nextFreeId++;
		}
		map.put(s.id, s);
	}
	
	public static void clear() {
		map.clear();
		nextFreeId = 1;
	}
	
	public static Gear find(int id) {
		if (map == null)
			Gear.load();
		if (map.containsKey(id))
			return map.get(id);
		return null;
	}
	
	public static ArrayList<Gear> getAll() {
		if (map == null)
			Gear.load();
		return new ArrayList<Gear>(map.values());
	}

	@SuppressWarnings("unchecked")
	public static void load() {
		map = new HashMap<Integer,Gear>();
		Document doc = Persistency.openXML(Persistency.FileType.Settings);
		Element root = doc.getRootElement();
		Element gearconfigs = root.getChild("gearconfigs");
		int defGear = Integer.valueOf(gearconfigs.getAttributeValue("default"));
		List<Element> l = gearconfigs.getChildren();
		Iterator<Element> li = l.iterator();
		//TreeSet itemIds = new TreeSet();
		while (li.hasNext()) {
			Element e = li.next();
			Gear s = new Gear(e);
			if (s.getId()>0) {
				map.put(s.getId(), s);
				//itemIds.addAll(s.getItemIds());
				if (s.getId()==defGear)
					Player.getInstance().equipGear(s);
			}
		}
	}

	public static void remove(Gear g) {
		if (map == null)
			Gear.load();
		map.remove(g.id);
	}

	@SuppressWarnings("unchecked")
	public static void save() {
		Element root, gearconfigs;
    Document document = Persistency.openXML(Persistency.FileType.Settings);
		root = document.getRootElement();
		root.removeChild("gearconfigs");
		gearconfigs = new Element("gearconfigs");
		gearconfigs.setAttribute("default", Integer.toString(Player.getInstance().getEquipped().getId()));
		root.getChildren().add(gearconfigs);
		Iterator<Gear> i = map.values().iterator();
		while (i.hasNext())
			saveSetup(gearconfigs, i.next());
		Persistency.saveXML(document, Persistency.FileType.Settings);
	}

	@SuppressWarnings("unchecked")
	private static void saveSetup(Element root, Gear gear) {
		Element eSetup, eName, eItems, eItem, eGem, eEnchant, eEnchants;
		eSetup = new Element("gear");
		eSetup.setAttribute("id", gear.getId()+"");
		eName = new Element("name");
		eName.setText(gear.getName());
		eSetup.getChildren().add(eName);
		eItems = new Element("items");
		for (int i=0; i<=18; i++) {
			if (gear.getItem(i)==null)
				continue;
			eItem = new Element("item");
			eItem.setAttribute("id", gear.getItem(i).getId()+"");
			eItem.setAttribute("slot", i+"");
			for (int j=0; j<=2; j++) {
				Gem gem = gear.getGem(i, j);
				if (gem != null) {
					eGem = new Element("gem");
					eGem.setAttribute("index",j+"");
					eGem.setAttribute("id", gem.getId()+"");
					eItem.getChildren().add(eGem);
				}
			}
			eItems.getChildren().add(eItem);
		}
		eSetup.getChildren().add(eItems);
		eEnchants = new Element("enchants");
		for (int i=0; i<=18; i++) {
			if (gear.getEnchant(i)==null)
				continue;
			eEnchant = new Element("enchant");
			eEnchant.setAttribute("id", gear.getEnchant(i).getId()+"");
			eEnchant.setAttribute("slot", i+"");
			eEnchants.getChildren().add(eEnchant);
		}
		eSetup.getChildren().add(eEnchants);
		root.getChildren().add(eSetup);
	}

}
