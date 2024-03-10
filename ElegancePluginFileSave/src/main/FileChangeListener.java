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
		IResourceDelta delta = changeEvent.getDelta();
		if (delta != null) {
            try {
                delta.accept(new IResourceDeltaVisitor() {
                    @Override
                    public boolean visit(IResourceDelta delta) throws org.eclipse.core.runtime.CoreException {
                        IResource resource = delta.getResource();
                        if (resource.getType() == IResource.FILE && resource.getFileExtension().equals("java") && delta.getFlags() != IResourceDelta.MARKERS) {
                        	WorkspaceJob job = new WorkspaceJob("Top Level Job") {
								
								@Override
								public IStatus runInWorkspace(IProgressMonitor arg0) throws CoreException {

		                            // Get the OS-specific string
		                            String absolutePath = resource.getLocation().toOSString();
		                            FileAnalyzer fileAnalyzer = new FileAnalyzer(absolutePath, resource);
		                            
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
