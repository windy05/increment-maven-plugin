package org.bsoft.tj.plugins;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

@Mojo(name= "increment")
public class IncrementMojo  extends AbstractMojo {

    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    private MavenProject project;

    /**
     * @since 2.1-alpha-2
     */
    @Parameter( defaultValue = "${session}", readonly = true, required = true )
    private MavenSession session;



    /**
     * Output file name.
     */
    @Parameter( property = "outputFile", defaultValue = "${project.artifactId}.diff" )
    private File outputFile;

    /**
     * The directory where the webapp is built.
     */
    @Parameter( defaultValue = "${project.build.directory}/${project.build.finalName}", required = true )
    private File webappDirectory;

    /**
     * The list of resources we want to transfer.
     */
    @Parameter( defaultValue = "${project.resources}", required = true, readonly = true )
    private List<Resource> resources;

    @Parameter( defaultValue = "${project.build.directory}/increment-release", required = true )
    private String incrementRelease;

    @Parameter( defaultValue = "${project.build.sourceDirectory}", readonly = true, required = true )
    private File sourceDirectory;

    @Parameter( defaultValue = "${basedir}", readonly = true, required = true )
    private File basedir;

    @Parameter( defaultValue = "${basedir}/src/main/webapp", required = true )
    private File warSourceDirectory;

    private String classPath = File.separator + "WEB-INF"+File.separator+"classes";




    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
//        Xpp3Dom scmConfig = (Xpp3Dom) project.getPlugin("org.apache.maven.plugins:maven-scm-plugin").getConfiguration();
//        Xpp3Dom warConfig = (Xpp3Dom)   project.getPlugin("org.apache.maven.plugins:maven-war-plugin").getConfiguration();

        try {
            List<IncrementFileModel> diffFiles = getTargetPath(getIncrementFiles(outputFile,"Index:"));
            for (IncrementFileModel model:diffFiles) {
                if (model.getTagertPath()!=null){
                    copyFile(model.getTagertPath());
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private  List<IncrementFileModel> getTargetPath(List<IncrementFileModel> files ){
        for (IncrementFileModel model:files) {
            File file = new File(basedir.getPath() + File.separator + model.getScmPath().trim());
            String suffix = file.getName().substring(file.getName().lastIndexOf(".") + 1);
            int index = -1;
            if (suffix.equals("java")){
                index = file.getPath().indexOf(sourceDirectory.getPath());
                if (index >= 0 && model.getTagertPath()==null) {
                    model.setTagertPath(file.getPath().substring(index + sourceDirectory.getPath().length()));
                    model.setTagertPath(classPath + model.getTagertPath().substring(0,model.getTagertPath().lastIndexOf("."))+".class");
                }
            }
            else {
                for (Resource resource : resources) {
                    String  resourceTargetPath = "";
                    File resourceFile = new File(resource.getDirectory());
                    if (resource.getTargetPath()!=null){
                        resourceTargetPath = File.separator + resource.getTargetPath();
                    }
                    index = file.getPath().indexOf(resourceFile.getPath());
                    if (index >= 0 && model.getTagertPath() == null) {
                        model.setTagertPath(classPath +resourceTargetPath+ file.getPath().substring(index + resourceFile.getPath().length()));
                    }
                }

                index = file.getPath().indexOf(warSourceDirectory.getPath());
                if (index >= 0 && model.getTagertPath()==null) {
                    model.setTagertPath(file.getPath().substring(index + warSourceDirectory.getPath().length()));
                }

            }
        }
        return files;
    }

    private  List<IncrementFileModel> getIncrementFiles(File file,String prefixs) throws IOException {
        List<IncrementFileModel> fileList = new LinkedList<IncrementFileModel>();
        FileInputStream fis = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String line = null;
        while ((line = br.readLine()) != null) {
            if (line.startsWith(prefixs)){
                IncrementFileModel model = new IncrementFileModel();
                model.setScmPath(line.replace(prefixs,""));
                fileList.add(model);
            }
        }
        br.close();
        return fileList;
    }

    private void copyFile(String filePath) throws IOException {
        File file = new File(webappDirectory.getPath()+filePath);
        if (file.exists()){
            if (!file.isDirectory()) {
                getLog().debug("Copying '" + filePath + "'");
                FileUtils.copyFile(file, new File(incrementRelease + filePath));
            }
            else {
                getLog().debug("Mkdir '" + filePath + "'");
                FileUtils.forceMkdir(new File(incrementRelease + filePath));
            }
        }
        else{
            getLog().warn("'"+webappDirectory.getPath()+filePath+"does not exist");
        }
    }




}
