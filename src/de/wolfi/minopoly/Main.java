package de.wolfi.minopoly;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;

import de.wolfi.minopoly.commands.BankCommand;
import de.wolfi.minopoly.commands.DiceCommand;
import de.wolfi.minopoly.commands.MoveCommand;
import de.wolfi.minopoly.commands.PlayerSelectorCommand;
import de.wolfi.minopoly.commands.SetupCommand;
import de.wolfi.minopoly.components.Minopoly;

public class Main extends JavaPlugin implements Listener {

	public static Main getMain() {
		return JavaPlugin.getPlugin(Main.class);
	}

	private final HashMap<String, Minopoly> games = new HashMap<>();

	public Minopoly getMinopoly(World w) {
		return this.games.get(w.getName());
	}

	public boolean isMinopolyWorld(World w) {
		return new File(w.getWorldFolder(), "minopoly.bin").exists();
	}

	public Minopoly loadMap(World w) {
		if (!this.games.containsKey(w.getName())) {
			final File cfg = new File(w.getWorldFolder(), "minopoly.bin");
			if (cfg.exists())
				try {
					final ObjectInputStream in = new ObjectInputStream(new FileInputStream(cfg));
					final Object o = in.readObject();
					if (o instanceof Minopoly) {
						this.games.put(w.getName(), (Minopoly) o);
						((Minopoly) o).load();
					} else
						cfg.delete();
					in.close();
				} catch (IOException | ClassNotFoundException e) {
					e.printStackTrace();
				}
			else
				this.games.put(w.getName(), new Minopoly(w.getName()));

		}
		return this.getMinopoly(w);
	}

	@Override
	public void onDisable() {
		for(String s : games.keySet()){
			Minopoly m = games.get(s);
			m.unload();
			this.saveMap(s,m);
			
		}
	}
	
	private void saveMap(String s, Minopoly m) {
		final File cfg = new File(Bukkit.getWorld(s).getWorldFolder(), "minopoly.bin");
		try {
			cfg.createNewFile();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
			try {
				ObjectOutputStream st = new ObjectOutputStream(new FileOutputStream(cfg));
				st.writeObject(m);
				st.flush();
				st.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		
	}

	@Override
	public void onEnable() {
		this.getCommand("setupminopoly").setExecutor(new SetupCommand());
		this.getCommand("bank").setExecutor(new BankCommand(this));
		this.getCommand("move").setExecutor(new MoveCommand(this));
		this.getCommand("dice").setExecutor(new DiceCommand(this));
		this.getCommand("playerselector").setExecutor(new PlayerSelectorCommand(this));
		Bukkit.getPluginManager().registerEvents(this, this);
	}
	
	@EventHandler
	public void onWorldLoad(WorldLoadEvent e){
		if(this.isMinopolyWorld(e.getWorld())) this.loadMap(e.getWorld());
	}

}
