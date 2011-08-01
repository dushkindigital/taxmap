import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Map;

import java.io.*;

class OntoRead 
{
	private static void printUsage()
	{
		System.out.println("OntoRead Usage:");
		System.out.println("javac -cp <class-path> OntoRead.java");
		System.out.println("java -cp <class-path> OntoRead owlfiles");
		System.out.println("<class-path> must have owlapi-bin.jar in the path list.");
		System.out.println("owlfiles is the list of URLs and/or filenames referring OWL ontology files.");
		System.out.println();
	}

	public static String extractFileName(String filePathName)
	{
		if (filePathName == null)
			return null;

		int dotPos = filePathName.lastIndexOf('.');
		int slashPos = filePathName.lastIndexOf('\\');
		if ( slashPos == -1 )
			slashPos = filePathName.lastIndexOf('/');

		if (dotPos > slashPos)
		{
			return filePathName.substring(slashPos > 0 ? slashPos + 1 : 0, dotPos);
		}

		return filePathName.substring(slashPos > 0 ? slashPos + 1 : 0);
	}

	private static long wordCount(String line)
	{
		long numWords = 0;
		int index = 0;
		boolean prevWhiteSpace = true;
		int strlen = line.length();
		while (index < strlen)
		{
			char c = line.charAt(index++);
			boolean currWhiteSpace = Character.isWhitespace(c);
			if (prevWhiteSpace && !currWhiteSpace)
			{
				if (c != '.')
					numWords++;
			}
			prevWhiteSpace = currWhiteSpace;
		}
		return numWords;
	}

	private static void readFile(String fileName)
	{
		try
		{
			// Open the file that is the first 
			// command line parameter
			FileInputStream fileStream = new FileInputStream(fileName);
			FileOutputStream outFileStream = new FileOutputStream("textfile.txt");

			// Get the object of DataInputStream
			DataInputStream dataStream = new DataInputStream(fileStream);
			BufferedReader reader = new BufferedReader(new InputStreamReader(dataStream));
			String fileLine;
			//Read File Line By Line
			while ((fileLine = reader.readLine()) != null)	 
			{
				if (fileLine.contains("Original sentence") == true)
				{
					int i = fileLine.indexOf(": ") + 3;
					int j = fileLine.length() - 1;
					String content = fileLine.substring(i, j);

					// Print the content on the console
					// if certain pattern occurs at the beginning of the line
					// System.out.println(strLine);
					if (wordCount(content) > 1)
						System.out.println(content);
					System.out.println();
				}
				else 
				{
					// System.out.println("Original sentence no found");
				}
			}

			// Close the input stream
			dataStream.close();
			fileStream.close();
		}
		catch (Exception e)
		{
			//Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}

		return;
	}

	// private static void parse_load(String[] args) 
	private static void parse_load(String fileName) 
	{
		try 
		{
			// Get hold of an ontology manager
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

			if (fileName.indexOf("http") >= 0)
			{
				// Let's load an ontology from the web
				IRI iri = IRI.create(fileName);
				OWLOntology taxonomy = manager.loadOntologyFromOntologyDocument(iri);
				System.out.println("Loaded ontology: " + taxonomy);
				System.out.println();

				// Remove the ontology so that we can load a local copy.
				manager.removeOntology(taxonomy);
				System.out.println("==> Removed ontology " + taxonomy);
				System.out.println();
			}
			else
			{
				// Create a file object that points to the local copy
				File file = new File(fileName);

				// Now load the local copy
				OWLOntology taxonomy = manager.loadOntologyFromOntologyDocument(file);
				System.out.println();
				System.out.println("==> Loaded ontology: " + taxonomy);

				// We can always obtain the location where an ontology was loaded from
				IRI documentIRI = manager.getOntologyDocumentIRI(taxonomy);
				System.out.println("    from: " + documentIRI);
				System.out.println();

				// Remove the ontology again so we can reload it later
				manager.removeOntology(taxonomy);
				System.out.println("==> Removed ontology " + taxonomy);
				System.out.println();
			}


			// In cases where a local copy of one of more ontologies is used, an ontology IRI mapper can be used
			// to provide a redirection mechanism.  This means that ontologies can be loaded as if they were located
			// on the web.
			// In this example, we simply redirect the loading from http://www.co-ode.org/ontologies/pizza/pizza.owl
			// to our local copy above.
			// ===== manager.addIRIMapper(new SimpleIRIMapper(iri, IRI.create(file)));
			// Load the ontology as if we were loading it from the web (from its ontology IRI)
			// ===== IRI taxonomyIRI = IRI.create("http://www.co-ode.org/ontologies/pizza/pizza.owl");
			// ===== OWLOntology redirectedPizza = manager.loadOntology(taxonomyIRI);
			// ===== System.out.println("Loaded ontology: " + redirectedPizza);
			// ===== System.out.println("    from: " + manager.getOntologyDocumentIRI(redirectedPizza));

			// Note that when imports are loaded an ontology manager will be searched for mappings
		}
		catch (OWLOntologyCreationIOException e) {
			// IOExceptions during loading get wrapped in an OWLOntologyCreationIOException
			IOException ioException = e.getCause();
			if (ioException instanceof FileNotFoundException) {
				System.out.println("Could not load ontology. File not found: " + ioException.getMessage());
				System.out.println();
			}
			else if (ioException instanceof UnknownHostException) {
				System.out.println("Could not load ontology. Unknown host: " + ioException.getMessage());
				System.out.println();
			}
			else {
				System.out.println("Could not load ontology: " + ioException.getClass().getSimpleName() + " " + ioException.getMessage());
				System.out.println();
			}
		}
		catch (UnparsableOntologyException e) {
			// If there was a problem loading an ontology because there are syntax errors in the document (file) that
			// represents the ontology then an UnparsableOntologyException is thrown
			System.out.println("Could not parse the ontology: " + e.getMessage());
				System.out.println();
			// A map of errors can be obtained from the exception
			Map<OWLParser, OWLParserException> exceptions = e.getExceptions();
			// The map describes which parsers were tried and what the errors were
			for (OWLParser parser : exceptions.keySet()) {
				System.out.println("Tried to parse the ontology with the " + parser.getClass().getSimpleName() + " parser");
				System.out.println("Failed because: " + exceptions.get(parser).getMessage());
				System.out.println();
			}
		}
		catch (UnloadableImportException e) {
			// If our ontology contains imports and one or more of the imports could not be loaded then an
			// UnloadableImportException will be thrown (depending on the missing imports handling policy)
			System.out.println("Could not load import: " + e.getImportsDeclaration());
				System.out.println();
			// The reason for this is specified and an OWLOntologyCreationException
			OWLOntologyCreationException cause = e.getOntologyCreationException();
			System.out.println("Reason: " + cause.getMessage());
				System.out.println();
		}
		catch (OWLOntologyCreationException e) {
			System.out.println("Could not load ontology: " + e.getMessage());
				System.out.println();
		}
	}


	public static void main(String args[])
	{
		try
		{
			// Open the metafile that is the first 
			// command line parameter
			String metaFileName = args[0];

			FileInputStream metaFileStream = new FileInputStream(metaFileName);
			// FileInputStream fstream = new FileInputStream("textfile.txt");

			// Get the object of DataInputStream
			DataInputStream metaDataStream = new DataInputStream(metaFileStream);
			BufferedReader metaReader = new BufferedReader(new InputStreamReader(metaDataStream));

			String metaFileLine;

			//Read File Line By Line
			while ((metaFileLine = metaReader.readLine()) != null)	 
			{
				String fileName = metaFileLine;
				System.out.println(fileName);

				// readFile(fileName);
				parse_load(fileName);
			}

			// Close the input stream
			metaDataStream.close();
			metaFileStream.close();
		}
		catch (Exception e)
		{
			//Catch exception if any
			System.err.println("Error: " + e.getMessage());
			if (args.length == 0)
			{
				printUsage();
			}
			System.exit(0);
		}
	}
}

