package main;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
/**
 * This class is in charge of listening for file changes
 * It is registered by the startup class on the workbench and then using IResourceDeltas it is possible to get the file that was changed to mark it
 * The job of marking the file is then registered with Eclipse and run when possible
 * @author repineap
 *
 */
public class FileChangeListener implements IResourceChangeListener {

	@Override
	public void resourceChanged(IResourceChangeEvent changeEvent) {
		//The delta is the overall change that is created by essentially anything happening, reading the docs explains better what these are
		IResourceDelta delta = changeEvent.getDelta();
		if (delta != null) {
            try {
            	//Using a visitor, we are able to filter out the correct deltas that we want to check
                delta.accept(new IResourceDeltaVisitor() {
                    @Override
                    public boolean visit(IResourceDelta delta) throws org.eclipse.core.runtime.CoreException {
                        IResource resource = delta.getResource();
                        //Checks if the resource change is in a file, if that file is a java file, and if the changes are not changing markers since this creates infinite looping
                        if (resource.getType() == IResource.FILE && resource.getFileExtension().equals("java") && delta.getFlags() != IResourceDelta.MARKERS) {
                        	//Because of modification trees, the analysis must be run in a workspace job that runs as soon as the tree is open for modifications
                        	WorkspaceJob job = new WorkspaceJob("Top Level Job") {
								
								@Override
								public IStatus runInWorkspace(IProgressMonitor arg0) throws CoreException {

		                            //Get the OS-specific string
		                            String absolutePath = resource.getLocation().toOSString();
		                            //Runs the resource through the FileAnalyzer where the bulk of work is done
		                            FileAnalyzer fileAnalyzer = new FileAnalyzer(absolutePath, resource);
		                            //Cancels the job if there isn't a config file, or there is an error
		                            if (!fileAnalyzer.evaluateMethods()) return Status.CANCEL_STATUS;
		                            return Status.OK_STATUS;
								}
							};
							job.schedule();
                            
                        }
                        return true;
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
	}

}
