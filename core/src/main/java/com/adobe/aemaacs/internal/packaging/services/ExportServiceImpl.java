package com.adobe.aemaacs.internal.packaging.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.jackrabbit.vault.fs.api.PathFilterSet;
import org.apache.jackrabbit.vault.fs.config.DefaultWorkspaceFilter;
import org.apache.jackrabbit.vault.fs.io.Archive;
import org.apache.jackrabbit.vault.fs.io.Archive.Entry;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageDefinition;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.PackageException;
import org.apache.jackrabbit.vault.packaging.Packaging;
import org.apache.jackrabbit.vault.util.DefaultProgressListener;
import org.apache.sling.api.resource.ResourceResolver;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.adobe.aemaacs.external.packaging.services.ExportService;
import com.adobe.aemaacs.external.packaging.services.ImpexException;

@Component(immediate = true, service = ExportService.class)
public class ExportServiceImpl implements ExportService {

	@Reference
	private transient Packaging packagingService;

	@Override
	public JcrPackage buildPackage(List<String> filters, ResourceResolver resolver) {
		DefaultWorkspaceFilter defaultWorkspaceFilter = new DefaultWorkspaceFilter();
		filters.forEach(item -> defaultWorkspaceFilter.add(new PathFilterSet(item)));

		Session session = resolver.adaptTo(Session.class);
		final JcrPackageManager jcrPackageManager = this.packagingService.getPackageManager(session);
		try {
			JcrPackage jcrPackage = jcrPackageManager.create("demo", "content");
			JcrPackageDefinition jcrPackageDefinition = jcrPackage.getDefinition();
			jcrPackageDefinition.setFilter(defaultWorkspaceFilter, false);
			DefaultProgressListener progressListener = new DefaultProgressListener(new PrintWriter(System.out));
			jcrPackageManager.assemble(jcrPackage, progressListener);
			return jcrPackage;
		} catch (RepositoryException | IOException | PackageException e) {
			throw new ImpexException(e.getMessage(), "IMPEX101");
		}
	}


	@Override
	public Archive getPackageArchive(JcrPackage jcrPackage) {
		try {
			return jcrPackage.getPackage().getArchive();
		} catch (RepositoryException | IOException e) {
			throw new ImpexException(e.getMessage(), "IMPEX102");
		}
	}

	
	@Override
	public void deserializeEntry(Archive archive, String filter, String sourceCodeWorkspace) {
		try {
			Entry entry = archive.getEntry("jcr_root".concat(filter).concat("/.content.xml"));
			if (null == entry) {
				throw new IOException("Unable to find entry in the archive");
			}
			try (InputStream inputStream = archive.openInputStream(entry);) {
				if (null == inputStream) {
					throw new IOException("Unable to read entry in the archive");
				}
				// Create Parent Path.
				File parentPath = new File(
						sourceCodeWorkspace.concat("/ui.content/src/main/content/jcr_root").concat(filter));
				parentPath.mkdirs();

				Files.copy(inputStream, Path.of(parentPath.getPath(), ".content.xml"),
						StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			throw new ImpexException(e.getMessage(), "IMPEX103");
		}
	}

	@Override
	public void deserializeEnteries(Archive archive, List<String> filterList, String sourceCodeWorkspace) {
		for (String filter : filterList) {
			try {
				deserializeEntry(archive, filter, sourceCodeWorkspace);
			} catch (ImpexException e) {
				throw new ImpexException(e.getMessage(), "IMPEX103");
			}
		}
	}
}
