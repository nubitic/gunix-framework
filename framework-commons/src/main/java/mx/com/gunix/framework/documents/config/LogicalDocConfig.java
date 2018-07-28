package mx.com.gunix.framework.documents.config;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;

import mx.com.gunix.framework.documents.EmbeddedLogicalDocManager;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.cxf.interceptor.Fault;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.hunteron.core.Context;
import com.logicaldoc.webservice.auth.AuthClient;
import com.logicaldoc.webservice.auth.AuthService;
import com.logicaldoc.webservice.document.DocumentClient;
import com.logicaldoc.webservice.document.DocumentService;
import com.logicaldoc.webservice.folder.FolderClient;
import com.logicaldoc.webservice.folder.FolderService;
import com.logicaldoc.webservice.search.SearchClient;
import com.logicaldoc.webservice.search.SearchService;

@Configuration
@ComponentScan("mx.com.gunix.framework.documents")
public class LogicalDocConfig {

	@Bean
	public AuthService ldAuthService() throws Exception {
		AuthService authService = new AuthClient(EmbeddedLogicalDocManager.getLogicalDocURL() + "/services/Auth");
		try {
			authService.logout(authService.login(Context.LOGICALDOC_USER.get(), Context.LOGICALDOC_PASSWORD.get()));
		} catch (Fault ignorar) {
			if (ExceptionUtils.getRootCause(ignorar) instanceof ConnectException) {
				String installHome = Context.LOGICALDOC_EMBEDDED_HOME.get();
				// Si la conexión no se pudo establecer entonces iniciamos/instalamos logicaldoc
				EmbeddedLogicalDocManager.start(System.getProperty("user.home") + File.separator + installHome, getClass().getClassLoader());
			}
		}
		return authService;
	}

	@Bean
	public FolderService ldFolderService() throws IOException {
		return new FolderClient(EmbeddedLogicalDocManager.getLogicalDocURL() + "/services/Folder");
	}

	@Bean
	public DocumentService ldDocumentService() throws IOException {
		return new DocumentClient(EmbeddedLogicalDocManager.getLogicalDocURL() + "/services/Document");
	}

	@Bean
	public SearchService ldSearchService() throws IOException {
		return new SearchClient(EmbeddedLogicalDocManager.getLogicalDocURL() + "/services/Search");
	}

}
