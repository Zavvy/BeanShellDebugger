//Copyright (C) 2011  Ryan Michela
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>.

package com.ryanmichela.bshd;

import java.io.File;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import java.net.URLClassLoader;
import java.net.URL;
import java.lang.reflect.Method;

import bsh.Interpreter;

public class BeanShellDebugger extends JavaPlugin {

	private Interpreter bsh;
	private Logger log;
	
	@Override
	public void onDisable() {}

	@Override
	public void onEnable() {
		log = getServer().getLogger();
		log.info("[bshd] Starting BeanShell Debugger");
		
		// Initialize the data folder
		if(!getDataFolder().exists()) {
			getDataFolder().mkdir();
		}

		try {

            URL url = new File(getDataFolder().getPath() + "/bsh-2.0b4.jar").toURI().toURL() ;

            URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Class urlClass = URLClassLoader.class;
            Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            method.invoke(urlClassLoader, new Object[]{url});


            bsh = new Interpreter();
			bsh.set("portnum", 24011);
			
			// Set up debug environment with globals
			bsh.set("pluginLoader", getPluginLoader());
			bsh.set("pluginManager", getServer().getPluginManager());
			bsh.set("server", getServer());
			bsh.set("classLoader", getClassLoader());
			
			// Create an alias for each plugin name using its class name
			for(Plugin p : getServer().getPluginManager().getPlugins()) {
				String[] cn = p.getClass().getName().split("\\.");
				log.info("[bshd] Regisering object " + cn[cn.length-1]);
				bsh.set(cn[cn.length-1], p);
			}
			
			// Source any .bsh files in the plugin directory
			if(getDataFolder().listFiles() != null) {
				for(File f : getDataFolder().listFiles()) {
					if(f.getName().endsWith(".bsh")) {
						log.info("[bshd] Sourcing file " + f.getName());
						bsh.source(f.getPath());
					}
					else {
						log.info("*** skipping " + f.getAbsolutePath());
					}
				}
			}
			
			bsh.eval("setAccessibility(true)"); // turn off access restrictions
			bsh.eval("server(portnum)");
			log.info("[bshd] BeanShell web console at http://localhost:24011");
			log.info("[bshd] BeanShell telnet console at localhost:24012");
			
			// Register the bshd command
			getCommand("bshd").setExecutor(new BshdCommand());
			
		} catch (Exception e) {
			log.severe("[bshd] Error in BeanShell. " + e.toString());
		}
		
	}

	@Override
	public void onLoad() {
		// TODO Auto-generated method stub
		
	}

}
