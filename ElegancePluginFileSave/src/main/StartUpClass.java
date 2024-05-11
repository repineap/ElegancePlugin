package main;


import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IStartup;
/*
 * Registers the listener to the workspace, allowing the plugin to work and run the code when files are changed or save
 */
public class StartUpClass implements IStartup {

	@Override
	public void earlyStartup() {
		//A workspace is the folder in which Java programming is done, set on the launch of Eclipse
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(new FileChangeListener());
		
	}
}