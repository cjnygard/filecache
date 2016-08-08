package com.github.cjnygard.filecache;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.xpath;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.github.cjnygard.filecache.model.Account;
import com.github.cjnygard.filecache.model.CacheFile;
import com.github.cjnygard.filecache.repository.AccountRepository;
import com.github.cjnygard.filecache.repository.CacheFileRepository;
import com.github.cjnygard.filecache.upstream.CacheResolver;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = SpringFilecacheApplication.class)
@WebAppConfiguration
public class SpringFilecacheApplicationTests {

	private String contentTypeJson = MediaType.APPLICATION_JSON_UTF8_VALUE;
	private MediaType expectJson = MediaType.APPLICATION_JSON_UTF8;

	private String contentTypeXML = MediaType.APPLICATION_XML_VALUE;
	private MediaType expectXML = MediaType.APPLICATION_XML;

	private MockMvc mockMvc;

	private String userName = "bdussault";

	@Value("${filecache.rootdir:/var/cache/filecache}")
	private String rootDir;

	private HttpMessageConverter mappingJackson2HttpMessageConverter;

	private Account account;

	private List<CacheFile> cacheFileList = new ArrayList<>();

	@Autowired
	private CacheFileRepository cacheFileRepository;

	@Mock
	private CacheResolver cacheResolver;

	@Autowired
	private WebApplicationContext webApplicationContext;

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	void setConverters(HttpMessageConverter<?>[] converters) {

		this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream()
				.filter(hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().get();

		Assert.assertNotNull("the JSON message converter must not be null", this.mappingJackson2HttpMessageConverter);
	}

	private CacheFile buildCache(Account account, String hash, String user) {
		return CacheFile.build(account, hash, user);
	}

	@Before
	public void setup() throws Exception {
		MockitoAnnotations.initMocks(this);

		this.mockMvc = webAppContextSetup(webApplicationContext).build();

		this.cacheFileRepository.deleteAllInBatch();
		this.accountRepository.deleteAllInBatch();

		this.account = accountRepository.save(new Account(userName, "password"));
		this.cacheFileList.add(cacheFileRepository.save(buildCache(account, "hash1", userName)));
		this.cacheFileList.add(cacheFileRepository.save(buildCache(account, "hash2", userName)));
	}

	@Test
	public void contextLoads() {
	}

	@Test
	public void overwriteFile() throws Exception {

		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/" + userName + "/cache/file/hashname")
				.file("file", "Test Content".getBytes()).contentType(MediaType.MULTIPART_FORM_DATA).accept(expectJson))
				.andExpect(status().isCreated());

		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/" + userName + "/cache/file/hashname")
				.file("file", "Other Content".getBytes()).contentType(MediaType.MULTIPART_FORM_DATA).accept(expectJson))
				.andExpect(status().isCreated());

		// mockMvc.perform(post("/george/cache/file/somehashcode")
		// .content(this.json(new CacheFile()))
		// .contentType(contentType))
		// .andExpect(status().isNotFound());
	}

	@Test
	public void userNotFoundJson() throws Exception {
		// MockMultipartFile file = new MockMultipartFile("file", "orig", null,
		// "bar".getBytes());

		// mockMvc.perform(MockMvcRequestBuilders.fileUpload("/fileupload").file(file))
		// .andExpect(status().isNotFound());

		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/george/cache/file/hashname")
				.file(new MockMultipartFile("file", "orig1", null, "Test Content".getBytes()))
				// .file("file", "Test Content".getBytes())
				.contentType(MediaType.MULTIPART_FORM_DATA).accept(expectXML)).andExpect(status().isNotFound());
		// mockMvc.perform(post("/george/cache/file/somehashcode")
		// .content(this.json(new CacheFile()))
		// .contentType(contentType))
		// .andExpect(status().isNotFound());
	}

	@Test
	public void userNotFoundXML() throws Exception {
		// MockMultipartFile file = new MockMultipartFile("file", "orig", null,
		// "bar".getBytes());

		// mockMvc.perform(MockMvcRequestBuilders.fileUpload("/fileupload").file(file))
		// .andExpect(status().isNotFound());

		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/george/cache/file/hashname")
				.file(new MockMultipartFile("file", "orig1", null, "Test Content".getBytes()))
				// .file("file", "Test Content".getBytes())
				.contentType(MediaType.MULTIPART_FORM_DATA).accept(expectJson)).andExpect(status().isNotFound());
		// mockMvc.perform(post("/george/cache/file/somehashcode")
		// .content(this.json(new CacheFile()))
		// .contentType(contentType))
		// .andExpect(status().isNotFound());
	}

	@Test
	public void createFileCacheJson() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/" + userName + "/cache/file/hashname")
				.file(new MockMultipartFile("file", "orig2", null, "Test Content".getBytes()))
				// .file("file", "Test Content".getBytes())
				.contentType(MediaType.MULTIPART_FORM_DATA).accept(expectJson)).andExpect(status().isCreated());

		// String fileCacheJson = json(buildCache(this.account,
		// "http://spring.io", this.account.getUsername()));
		// this.mockMvc.perform(post("/" + userName +
		// "/cache/file/somehashcode")
		// .contentType(contentType)
		// .content(fileCacheJson))
		// .andExpect(status().isCreated());
	}

	@Test
	public void createFileCacheXML() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.fileUpload("/" + userName + "/cache/file/hashname")
				.file(new MockMultipartFile("file", "orig2", null, "Test Content".getBytes()))
				// .file("file", "Test Content".getBytes())
				.contentType(MediaType.MULTIPART_FORM_DATA).accept(expectXML)).andExpect(status().isCreated());

		// String fileCacheJson = json(buildCache(this.account,
		// "http://spring.io", this.account.getUsername()));
		// this.mockMvc.perform(post("/" + userName +
		// "/cache/file/somehashcode")
		// .contentType(contentType)
		// .content(fileCacheJson))
		// .andExpect(status().isCreated());
	}

	@Test
	public void readSingleFileCacheJson() throws Exception {
		String url = "/" + userName + "/cache/id/" + this.cacheFileList.get(0).getId();
		mockMvc.perform(get(url).accept(expectJson)).andExpect(status().isOk())
				.andExpect(content().contentType(contentTypeJson))
				.andExpect(jsonPath("$.cacheFile.id", is(this.cacheFileList.get(0).getId().intValue())))
				.andExpect(jsonPath("$.cacheFile.storagePath", is(Paths.get(rootDir, "hash1" + userName).toString())))
				.andExpect(jsonPath("$.cacheFile.filename", is("hash1" + userName)))
				.andExpect(jsonPath("$._links.self.href", containsString(url)));
	}

	@Test
	public void readSingleFileCacheXML() throws Exception {
		String url = "/" + userName + "/cache/id/" + this.cacheFileList.get(0).getId();
		mockMvc.perform(get(url).accept(expectXML)).andExpect(status().isOk())
				.andExpect(content().contentType(contentTypeXML))
				.andExpect(
						xpath("/cacheFile/storagePath").string(is(Paths.get(rootDir, "hash1" + userName).toString())))
				.andExpect(xpath("/cacheFile/filename").string(is("hash1" + userName)))
				.andExpect(xpath("/cacheFile/@id").number(is(this.cacheFileList.get(0).getId().doubleValue())))
		// .andExpect((ResultMatcher) xpath("$._links.self.href",
		// containsString(url)))
		;
	}

	@Test
	public void readUpstreamFileCacheJson() throws Exception {
		String url = "/" + userName + "/cache/id/" + this.cacheFileList.get(0).getId();
		String hash = "hash1" + userName;
		CacheFile res = new CacheFile(this.account, hash, Paths.get(rootDir, hash).toString(), new Long(0));
		// if (null != cacheResolver) {
		Mockito.when(cacheResolver.fetchCacheFromUpstream(userName, this.cacheFileList.get(0).getFilename()))
				.thenReturn(res);
		// }

		mockMvc.perform(get(url).accept(expectJson)).andExpect(status().isOk())
				.andExpect(content().contentType(contentTypeJson))
				.andExpect(jsonPath("$.cacheFile.id", is(this.cacheFileList.get(0).getId().intValue())))
				.andExpect(jsonPath("$.cacheFile.storagePath", is(Paths.get(rootDir, hash).toString())))
				.andExpect(jsonPath("$.cacheFile.filename", is("hash1" + userName)))
				.andExpect(jsonPath("$._links.self.href", containsString(url)));
	}

	@Test
	public void readUpstreamFileCacheXML() throws Exception {
		String url = "/" + userName + "/cache/id/" + this.cacheFileList.get(0).getId();
		String hash = "hash1" + userName;
		CacheFile res = new CacheFile(this.account, hash, Paths.get(rootDir, hash).toString(), new Long(0));
		// if (null != cacheResolver) {
		Mockito.when(cacheResolver.fetchCacheFromUpstream(userName, this.cacheFileList.get(0).getFilename()))
				.thenReturn(res);
		// }

		mockMvc.perform(get(url).accept(expectXML)).andExpect(status().isOk())
				.andExpect(content().contentType(contentTypeXML))
				.andExpect(xpath("/cacheFile/storagePath").string(is(Paths.get(rootDir, hash).toString())))
				.andExpect(xpath("/cacheFile/filename").string(is("hash1" + userName)))
				.andExpect(xpath("/cacheFile/@id").number(is(this.cacheFileList.get(0).getId().doubleValue())))
		// .andExpect(xpath("$._links.self.href").string(containsString(url)))
		;
	}

	@Test
	public void readFileCachesJson() throws Exception {
		mockMvc.perform(get("/" + userName + "/cache").accept(expectJson)).andExpect(status().isOk())
				.andExpect(content().contentType(contentTypeJson))
				.andExpect(jsonPath("$._embedded.cacheFileResourceList", hasSize(2)))
				.andExpect(jsonPath("$._embedded.cacheFileResourceList[0].cacheFile.id",
						is(this.cacheFileList.get(0).getId().intValue())))
				.andExpect(jsonPath("$._embedded.cacheFileResourceList[0].cacheFile.storagePath",
						is(Paths.get(rootDir, "hash1" + userName).toString())))
				.andExpect(jsonPath("$._embedded.cacheFileResourceList[0].cacheFile.filename", is("hash1" + userName)))
				.andExpect(jsonPath("$._embedded.cacheFileResourceList[1].cacheFile.id",
						is(this.cacheFileList.get(1).getId().intValue())))
				.andExpect(jsonPath("$._embedded.cacheFileResourceList[1].cacheFile.storagePath",
						is(Paths.get(rootDir, "hash2" + userName).toString())))
				.andExpect(jsonPath("$._embedded.cacheFileResourceList[1].cacheFile.filename", is("hash2" + userName)));
	}

	@Test
	public void readFileCachesXML() throws Exception {
		mockMvc.perform(get("/" + userName + "/cache").accept(expectXML)).andExpect(status().isOk())
				.andExpect(content().contentType(contentTypeXML))
				.andExpect(xpath("/cacheFileList/cacheFile").nodeCount(2))
				.andExpect(xpath("/cacheFileList/cacheFile[1]/storagePath")
						.string(is(Paths.get(rootDir, "hash1" + userName).toString())))
				.andExpect(xpath("/cacheFileList/cacheFile[1]/filename").string(is("hash1" + userName)))
				.andExpect(xpath("/cacheFileList/cacheFile[1]/@id")
						.number(is(this.cacheFileList.get(0).getId().doubleValue())))
				.andExpect(xpath("/cacheFileList/cacheFile[2]/storagePath")
						.string(is(Paths.get(rootDir, "hash2" + userName).toString())))
				.andExpect(xpath("/cacheFileList/cacheFile[2]/filename").string(is("hash2" + userName)))
				.andExpect(xpath("/cacheFileList/cacheFile[2]/@id")
						.number(is(this.cacheFileList.get(1).getId().doubleValue())));
	}

	protected String json(Object o) throws IOException {
		MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
		this.mappingJackson2HttpMessageConverter.write(o, expectJson, mockHttpOutputMessage);
		return mockHttpOutputMessage.getBodyAsString();
	}

	protected String xml(Object o) throws IOException {
		MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
		this.mappingJackson2HttpMessageConverter.write(o, expectXML, mockHttpOutputMessage);
		return mockHttpOutputMessage.getBodyAsString();
	}
}
