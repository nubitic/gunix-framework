package org.openl.rules.activiti.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.impl.context.Context;
import org.openl.rules.activiti.ResourceNotFoundException;
import org.openl.rules.project.IRulesDeploySerializer;
import org.openl.rules.project.model.RulesDeploy;
import org.openl.rules.project.xml.XmlRulesDeploySerializer;
import org.openl.util.FileUtils;
import org.openl.util.IOUtils;

public final class ResourceUtils {
	public static final String RULES_DEPLOY_XML = "rules-deploy.xml";

	private ResourceUtils() {
	}

	private static IRulesDeploySerializer rulesDeploySerializer = new XmlRulesDeploySerializer();

	public static RulesDeploy readRulesDeploy(File openlProjectFolder) throws IOException {
		File rulesDeployXmlFile = new File(openlProjectFolder, RULES_DEPLOY_XML);
		if (rulesDeployXmlFile.exists() && rulesDeployXmlFile.isFile()) {
			return rulesDeploySerializer.deserialize(new FileInputStream(rulesDeployXmlFile));
		}
		return null;
	}

	public static File prepareDeploymentOpenLResource(String deploymentId, String resource) throws IOException {
		RepositoryService repositoryService = Context.getProcessEngineConfiguration().getRepositoryService();
		InputStream inputStream = repositoryService.getResourceAsStream(deploymentId, resource);
		if (inputStream == null) {
			throw new ResourceNotFoundException(String.format("No resource found with name \"%s\"!", resource));
		}

		final File workspaceFolder = FileUtils.createTempDirectory();
		if (resource.endsWith(".zip")) {
			// Unzip
			byte[] buffer = new byte[8192];
			ZipInputStream zis = new ZipInputStream(inputStream);
			try {
				// get the zipped file list entry
				ZipEntry ze = zis.getNextEntry();

				while (ze != null) {

					if (!ze.isDirectory()) {
						String fileName = ze.getName();
						File unzipped = new File(workspaceFolder, fileName);
						// create all non exists folders
						new File(unzipped.getParent()).mkdirs();
						FileOutputStream fos = new FileOutputStream(unzipped);

						IOUtils.copy(zis, fos, buffer);

						fos.close();
					}

					ze = zis.getNextEntry();
				}
			} finally {
				zis.closeEntry();
				IOUtils.closeQuietly(zis);
			}
		} else {
			// Copy
			File resourceFile = new File(workspaceFolder, resource);
			FileOutputStream fos = new FileOutputStream(resourceFile);
			IOUtils.copyAndClose(inputStream, fos);
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				FileUtils.deleteQuietly(workspaceFolder);
			}
		});
		return workspaceFolder;
	}
}
