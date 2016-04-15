package mx.com.gunix.framework.config;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.MongoClient;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.Paths;
import de.flapdoodle.embed.mongo.config.DownloadConfigBuilder;
import de.flapdoodle.embed.mongo.config.ExtractedArtifactStoreBuilder;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.builder.AbstractBuilder;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.store.FileType;
import de.flapdoodle.embed.process.distribution.Distribution;
import de.flapdoodle.embed.process.extract.IExtractedFileSet;
import de.flapdoodle.embed.process.extract.ITempNaming;
import de.flapdoodle.embed.process.extract.ImmutableExtractedFileSet;
import de.flapdoodle.embed.process.extract.UserTempNaming;
import de.flapdoodle.embed.process.io.directories.FixedPath;
import de.flapdoodle.embed.process.io.directories.IDirectory;
import de.flapdoodle.embed.process.io.file.Files;
import de.flapdoodle.embed.process.runtime.Network;
import de.flapdoodle.embed.process.store.IArtifactStore;
import de.flapdoodle.embed.process.store.StaticArtifactStoreBuilder;


@Configuration
@EnableMongoRepositories(basePackages = "mx.com.gunix.domain.persistence.mongo")
@ComponentScan("mx.com.gunix.domain.persistence.mongo")
public class MongoDBConfig {

	public @Bean MongoDbFactory mongoDbFactory() throws Exception {
		MongoClient mongoClient = null;

		int mongoPort = (System.getenv("MONGO_PORT") != null && !System.getenv("MONGO_PORT").isEmpty() ? Integer.parseInt(System.getenv("MONGO_PORT")) : 27017);
		String mongoDB = System.getenv("MONGO_DB_NAME");
		
		try {
			mongoClient = getMongoClient(mongoPort);
		} catch (Exception ignorar) {
			startMongoDB(mongoPort);
			mongoClient = getMongoClient(mongoPort);
		}

		return new SimpleMongoDbFactory(mongoClient, mongoDB);
	}

	public @Bean MongoTemplate mongoTemplate() throws Exception {
		MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory());

		return mongoTemplate;
	}
    
	private MongoClient getMongoClient(int mongoPort) throws Exception{
		MongoClient mongoClient = null;
		
		mongoClient = new MongoClient("localhost", mongoPort);
		mongoClient.getDatabaseNames();
		return mongoClient;
	}
    
    private void startMongoDB(int mongoPort) throws Exception{
		Logger logger = LoggerFactory.getLogger(getClass().getName());
    	Command command = Command.MongoD;
    	String mongoInstallationDir = System.getenv("MONGO_INSTALL_PATH") != null ? System.getenv("MONGO_INSTALL_PATH") : System.getProperty("user.home") + File.separatorChar + ".embeddedMongodb";
		
		ITempNaming executableNaming = new UserTempNaming();
		Distribution distribution= Distribution.detectFor(Version.Main.PRODUCTION);	
		String executable = new Paths(command).getFileSet(distribution)
				.entries()
				.stream()
				.filter(entry-> (entry.type() == FileType.Executable))
				.map(entry->{
					return entry.destination();
				}).findFirst().orElse(null);
		
		File executableFile = new File(mongoInstallationDir, executableNaming.nameFor("extract", "extract" + executable));
    	
        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
        	.defaultsWithLogger(command, logger)
            .artifactStore(getArtifactStore(mongoInstallationDir, command, executableFile, distribution, executableNaming))
            .daemonProcess(false)
            .build();
        
        IMongodConfig mongodConfig = new MongodConfigBuilder()
            .version(Version.Main.PRODUCTION)
            .net(new Net(mongoPort, Network.localhostIsIPv6()))
            .replication(new Storage(mongoInstallationDir + File.separatorChar + "data", null, 0))
            .build();

        MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);

        MongodExecutable mongodExecutable = null;

        mongodExecutable = runtime.prepare(mongodConfig);
        mongodExecutable.start();
    }

	private String executableBaseName(String name) {
		int idx = name.lastIndexOf('.');
		if (idx != -1) {
			return name.substring(0, idx);
		}
		return name;
	}

	private AbstractBuilder<IArtifactStore> getArtifactStore(String mongoInstallationDir, Command command, File executableFile, Distribution distribution, ITempNaming executableNaming) throws IOException {
		IDirectory artifactStorePath = new FixedPath(mongoInstallationDir);
		if(artifactStorePath.asFile().exists()){
	    	new File(mongoInstallationDir, executableBaseName(executableFile.getName()) + ".pid").delete();
			IExtractedFileSet fileSet=ImmutableExtractedFileSet.builder(Files.createOrCheckDir(artifactStorePath.asFile()))
					.baseDirIsGenerated(false)
					.executable(executableFile)
					.build();
			
			return new StaticArtifactStoreBuilder().fileSet(distribution,fileSet );
		}else{
			return new ExtractedArtifactStoreBuilder()
            .defaults(command)
            .extractDir(artifactStorePath)
            .tempDir(artifactStorePath)
            .download(new DownloadConfigBuilder()
                    .defaultsForCommand(command)
                    .artifactStorePath(artifactStorePath)
                    .build())
            .executableNaming(executableNaming);
		}
	}

}
