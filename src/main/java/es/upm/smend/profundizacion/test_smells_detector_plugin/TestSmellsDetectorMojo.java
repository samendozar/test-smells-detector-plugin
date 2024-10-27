package es.upm.smend.profundizacion.test_smells_detector_plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
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
	File tempCsvFile = null;
    FileWriter fileWriter = null;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		
		// Assuming tests are in the 'src/test/java' directory
        File testDir = new File(project.getBasedir(), "src/test/java");
        if (!testDir.exists() || !testDir.isDirectory()) {
            getLog().info("No test directory found.");
            return;
        }else
        	getLog().info("Test Directory found!!!");

        try {
            
        	// Create a temporary file
            tempCsvFile = new File(project.getBasedir(), "testData.csv");
            fileWriter = new FileWriter(tempCsvFile);
            
            getLog().warn(String.format("Temporary CSV file created at: %s", tempCsvFile.getAbsolutePath()));
            
            /*
             * Recursively add each test file to TsDetect file input
             */
            
            Files.walk(testDir.toPath())
            .filter(Files::isRegularFile)
            .filter(path -> path.toString().endsWith(".java"))
            .forEach(this::analyzeTestFile);
            
            // Flush and close the FileWriter
            fileWriter.flush();
            
            if (fileWriter != null) {
                fileWriter.close();
            }
            
            callTsDetect();
            
        } catch (IOException e) {
            getLog().error("Error while traversing test directory: " + testDir.getAbsolutePath(), e);
        }finally {
            // Clean up resources
            try {
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
	private void analyzeTestFile(Path testFilePath) {

        try {
        	
        	String srcClassPathStr = testFilePath.toString().replace("Test", "");
        	getLog().warn(String.format("Anadiendo Clase de Src: %s - Clase de Test: %s", srcClassPathStr, testFilePath.toString()));
            // Write data to the CSV file
            fileWriter.append(String.format("Test-Smells,%s,%s", testFilePath, srcClassPathStr));

        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	@SuppressWarnings("deprecation")
	private void callTsDetect() {
		try {
			
			Process process = Runtime.getRuntime().exec(String.format("java -jar %s/TestSmellDetector.jar testData.csv", project.getBasedir()));

			StringBuilder output = new StringBuilder();

			BufferedReader reader = new BufferedReader(
			new InputStreamReader(process.getInputStream()));

			String line;
			
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}

			process.waitFor();
			
			} catch (IOException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

	}

}
