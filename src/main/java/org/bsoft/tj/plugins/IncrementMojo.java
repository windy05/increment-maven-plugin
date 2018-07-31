package org.bsoft.tj.plugins;

import org.apache.commons.io.FileUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Mojo(name = "release",defaultPhase = LifecyclePhase.COMPILE)
public class IncrementMojo  extends AbstractMojo {

    @Parameter( defaultValue = "${basedir}", readonly = true, required = true )
    private File basedir;

    @Parameter( defaultValue = "${project.build.outputDirectory}", readonly = true, required = true )
    private File outputDirectory;

    @Parameter( defaultValue = "${project.build.sourceDirectory}", readonly = true, required = true )
    private File sourceDirectory;

    @Parameter( required = true )
    private String url ;

    @Parameter( required = true )
    private String userName ;

    @Parameter( required = true )
    private String password ;

    @Parameter( required = true )
    private long startRevision ;

    @Parameter
    private long endRevision ;

    @Parameter( defaultValue = "${project.build.directory}/increment-release", required = true )
    private String incrementRelease;

    @Parameter( required = true )
    private String[] targetPaths;

    @Parameter( defaultValue = "${project.resources}",readonly = true, required = true )
    private List<Resource> resources;

    private List<String> relativePaths = new ArrayList<String>();
    private List<IncrementFileModel> incrementFiles = new ArrayList<IncrementFileModel>();

    public void execute() throws MojoExecutionException, MojoFailureException {

            getRelativePath(sourceDirectory.getPath());
            for (Resource resource:resources) {
                getRelativePath(resource.getDirectory());
            }
        try {
            for (SVNLogEntry entry:getLogs()) {
               for ( Map.Entry<String, SVNLogEntryPath> changedPath:entry.getChangedPaths().entrySet()) {
                   IncrementFileModel fileModel = getIncrementFile(changedPath.getKey());
                   if (fileModel.isUseful()) {
                       incrementFiles.add(fileModel);
                   }
               }
            }
            for (IncrementFileModel model:incrementFiles) {
                copyFile(model.getRelativePath());
            }
        } catch (SVNException e) {
            throw new MojoFailureException(e.getErrorMessage().getMessage());
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage());
        }
    }



    private List<SVNLogEntry> getLogs() throws SVNException {
        DAVRepositoryFactory.setup();
        SVNURL repositoryURL = null;
        repositoryURL =SVNURL.parseURIEncoded(url);
        SVNRepository repository = DAVRepositoryFactory.create(repositoryURL);
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(userName, password);
        repository.setAuthenticationManager(authManager);
        List<SVNLogEntry> logEntries = new ArrayList<SVNLogEntry>();
        if (endRevision == 0) {
            endRevision = repository.getLatestRevision();
        }
        repository.log(targetPaths, logEntries, startRevision, endRevision, true, true);
        return logEntries;
    }

    private void getRelativePath(String fullPath){
        String path = fullPath.replace(basedir.getPath(),"");
        if (!relativePaths.contains(path)){
            getLog().info("add RelativePath '"+path+"'");
            relativePaths.add(path);
        }
    }

    private IncrementFileModel getIncrementFile(String urlPath){
        String filePath;
        filePath = urlPath.replace("/","\\");
        IncrementFileModel model = new IncrementFileModel();
        for (String path:relativePaths) {
            int index = filePath.indexOf(path);
            if (index > 0){
                getLog().info("add IncrementFile '"+filePath.substring(index+path.length())+"'");
               model.setUseful(true);
               model.setRelativePath(filePath.substring(index+path.length()));
            }
        }
        return model;
    }

    private void copyFile(String filePath) throws IOException {
        if (filePath.contains(".java")){
            filePath = filePath.replace(".java",".class");
        }
        File file = new File(outputDirectory.getPath()+filePath);
        if (file.exists()){
            FileUtils.copyFile(file,new File(incrementRelease+filePath));
        }
    }

}
