package edu.uams.clara.lucene.common;

import java.io.File;
import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.search.NRTManager;
import org.apache.lucene.search.NRTManagerReopenThread;
import org.apache.lucene.search.SearcherFactory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NativeFSLockFactory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import edu.uams.clara.lucene.common.indexrules.IndexDocument;

public abstract class AbstractLuceneService {

	private final static Logger logger = LoggerFactory
			.getLogger(AbstractLuceneService.class);

	private NRTManager nrtManager;
	
	private NRTManagerReopenThread reopenThread;

	private NRTManager.TrackingIndexWriter indexWriter;

	private Jaxb2Marshaller jaxb2Marshaller;

	private ResourceLoader resourceLoader;

	private IndexDocument indexDocument;

	private String indexDocumentXml;

	private File indexRootDir;
	private Version luceneVersion;
	private Analyzer luceneAnalyzer;


	public AbstractLuceneService() {
	}

	private void init() throws IOException {	
		
		Source source = new StreamSource(resourceLoader.getResource(
				indexDocumentXml).getFile());
		this.indexDocument = (IndexDocument) jaxb2Marshaller.unmarshal(source);
		
		Resource fileResource = new FileSystemResource(new File(indexRootDir, indexDocument.getIndexLocation()));
		
		File location = fileResource.getFile();
		
		logger.info("index folder for "
					+ indexDocument.getIdentifier() + " at: '"
					+ location.getAbsolutePath() + "'");
		if (!location.exists() || !location.canRead()) {
			logger.warn("Creating lucence index directory....");
			location.mkdirs();
		}
		FSDirectory fsDirectory = FSDirectory.open(location,
				new NativeFSLockFactory());

		IndexWriterConfig indexWriteConfig = new IndexWriterConfig(
				luceneVersion, luceneAnalyzer);
		indexWriteConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
		// Optional: for better indexing performance, if you
		// are indexing many documents, increase the RAM
		// buffer. But if you do this, increase the max heap
		// size to the JVM (eg add -Xmx512m or -Xmx1g):
		//
		// iwc.setRAMBufferSizeMB(256.0);

		// NOTE: if you want to maximize search performance,
		// you can optionally call forceMerge here. This can be
		// a terribly costly operation, so generally it's only
		// worth it when your index is relatively static (ie
		// you're done adding documents to it):
		//
		// writer.forceMerge(1);

		this.indexWriter = new NRTManager.TrackingIndexWriter(new IndexWriter(
				fsDirectory, indexWriteConfig));
		this.nrtManager = new NRTManager(this.indexWriter,
				new SearcherFactory());
		
		NRTManagerReopenThread reopenThread = new NRTManagerReopenThread(this.nrtManager, 5.0, 0.1);
		   reopenThread.setName("NRT Reopen Thread");
		   reopenThread.setPriority(Math.min(Thread.currentThread().getPriority()+2, Thread.MAX_PRIORITY));
		   reopenThread.setDaemon(true);
		   reopenThread.start();
		this.reopenThread = reopenThread;
	}
	
	private void release() throws IOException{
		if (this.nrtManager != null) {			
			this.reopenThread.close();
			this.nrtManager.close();
			this.indexWriter.getIndexWriter().waitForMerges();
			this.indexWriter.getIndexWriter().close();
			this.indexWriter.getIndexWriter().getDirectory().close();
		}
	}

	@PostConstruct
	public void postConstructMethod() throws IOException {
		release();
		init();
	}
	
	
	@PreDestroy
	public void preDestroyMethod(){
		try {
			release();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error("failed to release lucene service for  " + this.getClass().getSimpleName(), e);
		}
	}

	public Jaxb2Marshaller getJaxb2Marshaller() {
		return jaxb2Marshaller;
	}

	@Autowired(required = true)
	public void setJaxb2Marshaller(Jaxb2Marshaller jaxb2Marshaller) {
		this.jaxb2Marshaller = jaxb2Marshaller;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	@Autowired(required = true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public Version getLuceneVersion() {
		return luceneVersion;
	}

	@Autowired(required = true)
	public void setLuceneVersion(Version luceneVersion) {
		this.luceneVersion = luceneVersion;
	}

	public Analyzer getLuceneAnalyzer() {
		return luceneAnalyzer;
	}

	@Autowired(required = true)
	public void setLuceneAnalyzer(Analyzer luceneAnalyzer) {
		this.luceneAnalyzer = luceneAnalyzer;
	}

	public String getIndexDocumentXml() {
		return indexDocumentXml;
	}

	public void setIndexDocumentXml(String indexDocumentXml) {
		this.indexDocumentXml = indexDocumentXml;
	}

	public NRTManagerReopenThread getReopenThread() {
		return reopenThread;
	}

	public void setReopenThread(NRTManagerReopenThread reopenThread) {
		this.reopenThread = reopenThread;
	}

	public File getIndexRootDir() {
		return indexRootDir;
	}

	public void setIndexRootDir(File indexRootDir) {
		this.indexRootDir = indexRootDir;
	}
}
