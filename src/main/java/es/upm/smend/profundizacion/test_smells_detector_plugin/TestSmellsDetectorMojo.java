package es.upm.smend.profundizacion.test_smells_detector_plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

@Mojo(name = "test-smells-counter", defaultPhase = LifecyclePhase.COMPILE)
public class TestSmellsDetectorMojo extends AbstractMojo{
	
	@Parameter(defaultValue = "${project}", required = true, readonly = true)
	MavenProject project;
	@Parameter(property = "scope")
	String scope;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		// Assuming tests are in the 'src/test/java' directory
        File testDir = new File(project.getBasedir(), "src/test/java");
        if (!testDir.exists() || !testDir.isDirectory()) {
            getLog().info("No test directory found.");
            return;
        }else
        	getLog().info("Test Directory found!!!");

        
        /*
         * Recursively add each test file to TsDetect file input
         */
        try {
            Files.walk(testDir.toPath())
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .forEach(this::analyzeTestFile);
        } catch (IOException e) {
            getLog().error("Error while traversing test directory: " + testDir.getAbsolutePath(), e);
        }
        
	}
	
	/**
	 * Method responsible for executing all defined test smell validations on 
	 * the specified test file
	 * @param testFilePath
	 */
	private void analyzeTestFile(Path testFilePath) {
		System.out.println(testFilePath.getFileName());
	}

}
