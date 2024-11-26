package com.gtcafe.rws.booter.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

// @ref: https://howtodoinjava.com/spring-boot2/read-file-from-resources/
@Configuration
public class Utils {

    private static final Logger logger = LoggerFactory.getLogger(Utils.class.getName());

    // approach # 1, not work
    // @Autowired
    // private ResourceLoader _resourceLoader;

    // // approach # 2, not work
    // @Autowired
    // private ApplicationContext ctx;

    // // approach # 3, not work
    @Value("classpath:slogan.txt")
    Resource _slogan;

    @Value("classpath:web-runtime.txt")
    Resource _webRuntime;

    @Value("classpath:user-claim.txt")
    Resource _userClaim;

    @Value("classpath:release-notes.txt")
    Resource _releaseNotes;

    @Autowired
    private Releng releng;

    // private static String CLASSPATH = "classpath:slogan.txt";
    // private static String FILE_PATH = "file:slogan.txt";

    // public String getSlogan() {
    // String content = "";
    // try {
    // // approach # 1
    // // logger.info("approach # 1");
    // // Resource resource = _resourceLoader.getResource(CLASSPATH);

    // // _resource = new ClassPathResource("slogan.txt");

    // // logger.info("approach # 2");
    // // Resource res = _resourceLoader.getResource(CLASSPATH);
    // // File file = res.getFile();
    // // content = new String(Files.readAllBytes(file.toPath()));

    // // logger.info("approach # 3");
    // // Resource resource = ctx.getResource(CLASSPATH);

    // File file = _resource.getFile();

    // System.out.println("File Found : " + file.exists());

    // content = new String(Files.readAllBytes(file.toPath()));

    // } catch (IOException e) {
    // logger.error(e.getMessage());
    // }
    // return content;
    // }

    private String getFileContent(Resource resource) {
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(resource.getURI()));

            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return e.getMessage();
        }
    }

    public String slogan() {
        String content = getFileContent(_slogan);

        content = content.replaceAll("<ServiceName>", releng.getServiceName());
        content = content.replaceAll("<Version>", releng.getVersion());
        content = content.replaceAll("<BuildType>", releng.getBuildType());
        content = content.replaceAll("<BuildId>", releng.getBuildId());
        content = content.replaceAll("<HashCode>", releng.getHashcode());

        return content;
    }

    public String userClaim() {
        String content = getFileContent(_userClaim);

        content = content.replaceAll("<TenantName>", "lds");
        content = content.replaceAll("<UserName>", "rickhwang");
        content = content.replaceAll("<RoleName>", "admin");
        // slogan = slogan.replaceAll("<Request-Id>", reqId);

        return content;
    }

    public String webruntime(String reqId) {
        String content = getFileContent(_webRuntime);

        content = content.replaceAll("<Request-Id>", reqId);
        content = content.replaceAll("<Datetime>", ZonedDateTime.now().toString());

        return content;
    }

    public String apiSlogan(String reqId) {
        StringBuffer buf = new StringBuffer();
        buf.append(slogan());
        buf.append(userClaim());
        buf.append(webruntime(reqId));
        return buf.toString();
    }

    public String releaseNotes() {
        String content = getFileContent(_releaseNotes);

        return content;
    }

}