package es.upm.smend.profundizacion.test_smells_detector_plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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

        
        // Define the temporary file
        File tempCsvFile = null;
        FileWriter fileWriter = null;
        
        try {
        	
            
        	// Create a temporary file
            tempCsvFile = File.createTempFile("testData", ".csv");
            fileWriter = new FileWriter(tempCsvFile);
            
            getLog().warn(String.format("Temporary CSV file created at: %s", tempCsvFile.getAbsolutePath()));
            
            /*
             * Recursively add each test file to TsDetect file input
             */
            List<Path> pathList = Files.walk(testDir.toPath())
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java")).toList();
            
            for(Path path : pathList) {
            	analyzeTestFile(path, tempCsvFile, fileWriter);
            }
            
            // Flush and close the FileWriter
            fileWriter.flush();
            
        } catch (IOException e) {
            getLog().error("Error while traversing test directory: " + testDir.getAbsolutePath(), e);
        }finally {
            // Clean up resources
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
                if (tempCsvFile != null) {
                    // Optionally delete the temp file after use
                    Files.deleteIfExists(tempCsvFile.toPath());
                    System.out.println("Temporary CSV file deleted.");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
	}
	
	/**
	 * Method responsible for executing all defined test smell validations on 
	 * the specified test file
	 * @param testFilePath
	 */
	private void analyzeTestFile(Path testFilePath, File tempCsvFile, FileWriter fileWriter) {

        try {
        	
        	String srcClassPathStr = testFilePath.toString().strip().replace("Test", "");
        	getLog().warn(String.format("Anadiendo Clase de Src: %s - Clase de Test: %s", srcClassPathStr, testFilePath.toString()));
            // Write data to the CSV file
            fileWriter.append(String.format("Test-Smells,%s,%s", testFilePath, srcClassPathStr));

        } catch (IOException e) {
            e.printStackTrace();
        }
	}

}
