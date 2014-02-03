package edu.uams.clara.integration.incoming.billingcodes;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.uams.clara.webapp.protocol.dao.budget.code.CPTCodeDao;
import edu.uams.clara.webapp.protocol.domain.budget.code.CPTCode;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( { "file:src/test/java/edu/uams/clara/webapp/protocol/dao/budget/code/CPTCodeLoaderTest-context.xml" })
public class CPTCodeLoaderTest {
	private final static Logger logger  = LoggerFactory
	.getLogger(CPTCodeLoaderTest.class);

	private ResourceLoader resourceLoader;

	private CPTCodeDao cptCodeDao;

	@Test
	public void loadCPTCodeLongDescription() throws Exception{

		Resource longDescCPTCode = resourceLoader.getResource("cptcode/LONGULT.txt");

		InputStream in = longDescCPTCode.getInputStream();

		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String strLine = null;
		String[] a = null;
		String code = null;
		String description = null;
		CPTCode cptCode = null;


		//getting cpt code and long description from LONGULT.txt, and save into database.
		while((strLine = br.readLine()) != null){
			a = strLine.split("\\t");
			code = a[0].trim();
			description = a[1].trim();


			cptCode = cptCodeDao.findByCode(code);
			if(cptCode == null){
				cptCode = new CPTCode();
			}
			cptCode.setCode(code);
			cptCode.setLongDescription(description);

			cptCode = cptCodeDao.saveOrUpdate(cptCode);

		}

		in.close();

	}

	@Test
	public void loadCPTCodeMedDescription() throws Exception{
		Resource medDescCPTCode = resourceLoader.getResource("cptcode/MEDU.txt");

		InputStream in = medDescCPTCode.getInputStream();

		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String strLine = null;
		String[] a = null;
		String code = null;
		String description = null;
		CPTCode cptCode = null;

		while((strLine = br.readLine()) != null){
			a = strLine.split(" ",2);
			code = a[0].trim();
			description = a[1].trim();

			logger.debug("code: " + code + "; med-desc: " + description);

			cptCode = cptCodeDao.findByCode(code);
			if(cptCode == null){
				cptCode = new CPTCode();
			}
			cptCode.setCode(code);
			cptCode.setMediumDescription(description);

			cptCode = cptCodeDao.saveOrUpdate(cptCode);

		}

		in.close();
	}

		// getting cpt code and shortdescription from shortu.txt, save in database
		@Test
		public void loadCPTCodeSHORTDescription() throws Exception{
			Resource shortDescCPTCode = resourceLoader.getResource("cptcode/SHORTU.txt");

			InputStream in = shortDescCPTCode.getInputStream();

			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine = null;
			String[] a = null;
			String code = null;
			String description = null;
			CPTCode cptCode = null;

			while((strLine = br.readLine()) != null){
				a = strLine.split(" ",2);
				code = a[0].trim();
				description = a[1].trim();

				logger.debug("code: " + code + "; short-desc: " + description);

				cptCode = cptCodeDao.findByCode(code);
				if(cptCode == null){
					cptCode = new CPTCode();
				}
				cptCode.setCode(code);
				cptCode.setShortDescription(description);

				cptCode = cptCodeDao.saveOrUpdate(cptCode);

			}

			in.close();

	}

	@Autowired(required=true)
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public ResourceLoader getResourceLoader() {
		return resourceLoader;
	}

	@Autowired(required=true)
	public void setCptCodeDao(CPTCodeDao cptCodeDao) {
		this.cptCodeDao = cptCodeDao;
	}

	public CPTCodeDao getCptCodeDao() {
		return cptCodeDao;
	}

}