package com.adobe.aemaacs.internal.packaging.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.FilenameUtils;
import org.apache.jackrabbit.vault.fs.api.PathFilterSet;
import org.apache.jackrabbit.vault.fs.config.DefaultWorkspaceFilter;
import org.apache.jackrabbit.vault.fs.io.Archive;
import org.apache.jackrabbit.vault.fs.io.Archive.Entry;
import org.apache.jackrabbit.vault.packaging.JcrPackage;
import org.apache.jackrabbit.vault.packaging.JcrPackageDefinition;
import org.apache.jackrabbit.vault.packaging.JcrPackageManager;
import org.apache.jackrabbit.vault.packaging.PackageException;
import org.apache.jackrabbit.vault.packaging.PackageId;
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
	public PackageId buildPackage(List<String> filters, ResourceResolver resolver, String packageName,
			String packageGroup) {
		DefaultWorkspaceFilter defaultWorkspaceFilter = new DefaultWorkspaceFilter();
		filters.forEach(item -> defaultWorkspaceFilter.add(new PathFilterSet(item)));

		Session session = resolver.adaptTo(Session.class);
		final JcrPackageManager jcrPackageManager = this.packagingService.getPackageManager(session);
		JcrPackage jcrPackage = null;
		try {
			jcrPackage = jcrPackageManager.create(packageGroup, packageName);
			if (null == jcrPackage) {
				throw new IOException("Unable to create JCR Package");
			}
			JcrPackageDefinition jcrPackageDefinition = jcrPackage.getDefinition();
			if (null == jcrPackageDefinition) {
				throw new IOException("Unable to create JCR Package");
			}
			jcrPackageDefinition.setFilter(defaultWorkspaceFilter, false);
			DefaultProgressListener progressListener = new DefaultProgressListener(new PrintWriter(System.out));
			jcrPackageManager.assemble(jcrPackage, progressListener);
			return jcrPackage.getPackage().getId();
		} catch (RepositoryException | IOException | PackageException e) {
			throw new ImpexException(e.getMessage(), "IMPEX101");
		}finally {
			if(null != jcrPackage) {
				jcrPackage.close();
			}
		}
	}

	@Override
	public Archive getPackageArchive(PackageId packageId, ResourceResolver resolver) {
		try {
			Session session = resolver.adaptTo(Session.class);
			final JcrPackageManager jcrPackageManager = this.packagingService.getPackageManager(session);
			return jcrPackageManager.open(packageId).getPackage().getArchive();
		} catch (RepositoryException | IOException e) {
			throw new ImpexException(e.getMessage(), "IMPEX102");
		}
	}

	@Override
	public void deserializeEntry(Archive archive, String filter, String sourceCodeWorkspace, String intermediatePath,
			String name) {
		try {
			Entry entry = archive.getEntry("jcr_root".concat(filter).concat("/").concat(intermediatePath).concat(name));
			if (null == entry) {
				throw new IOException("Unable to find entry in the archive");
			}
			try (InputStream inputStream = archive.openInputStream(entry);) {
				if (null == inputStream) {
					throw new IOException("Unable to read entry in the archive");
				}
				// Create Parent Path.
				File parentPath = Paths
						.get(FilenameUtils.getFullPath(sourceCodeWorkspace), FilenameUtils.getName(sourceCodeWorkspace),
								"/ui.content/src/main/content/jcr_root", FilenameUtils.getFullPath(filter),
								FilenameUtils.getName(filter), "/", FilenameUtils.getFullPath(intermediatePath))
						.toFile();
				parentPath.mkdirs();

				Files.copy(inputStream,
						Paths.get(FilenameUtils.getFullPath(parentPath.getPath()),
								FilenameUtils.getName(parentPath.getPath()), FilenameUtils.getName(name)),
						StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException e) {
			throw new ImpexException(e.getMessage(), "IMPEX103");
		}
	}

	@Override
	public void deserializeEnteries(Archive archive, List<String> filterList, String sourceCodeWorkspace,
			String intermediatePath, String suffix) {
		for (String filter : filterList) {
			try {
				deserializeEntry(archive, filter, sourceCodeWorkspace, intermediatePath, suffix);
			} catch (ImpexException e) {
				throw new ImpexException(e.getMessage(), "IMPEX103");
			}
		}
	}

}
