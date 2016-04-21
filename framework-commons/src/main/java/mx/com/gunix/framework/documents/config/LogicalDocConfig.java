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
		AuthService authService = new AuthClient(getLogicalDocURL() + "/Auth");
		try {
			authService.logout(authService.login(System.getenv("LOGICALDOC_USER"), System.getenv("LOGICALDOC_PASSWORD")));
		} catch (Fault ignorar) {
			if (ExceptionUtils.getRootCause(ignorar) instanceof ConnectException) {
				// Si la conexión no se pudo establecer entonces iniciamos/instalamos logicaldoc
				EmbeddedLogicalDocManager.start(System.getProperty("user.home") + File.separator + ".logicalDocRepo", getClass().getClassLoader());
			}
		}
		return authService;
	}

	@Bean
	public FolderService ldFolderService() throws IOException {
		return new FolderClient(getLogicalDocURL() + "/Folder");
	}

	@Bean
	public DocumentService ldDocumentService() throws IOException {
		return new DocumentClient(getLogicalDocURL() + "/Document");
	}

	@Bean
	public SearchService ldSearchService() throws IOException {
		return new SearchClient(getLogicalDocURL() + "/Search");
	}

	private String getLogicalDocURL() {
		return System.getenv("LOGICALDOC_HOSTNAME") + (System.getenv("LOGICALDOC_PORT") != null ? ":" + System.getenv("LOGICALDOC_PORT") : "") + "/" + (System.getenv("LOGICALDOC_CONTEXT") != null ? System.getenv("LOGICALDOC_CONTEXT") : "") + "/services";
	}
}
