package com.example.messageboard;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features", // .feature 檔案的路徑
        glue = "com.example.messageboard.steps", // 步驟定義的 package
        plugin = {"pretty", "html:target/cucumber-reports.html", "json:target/cucumber.json"},
        snippets = CucumberOptions.SnippetType.CAMELCASE,
        dryRun = false // true 時只檢查步驟定義是否匹配，不實際執行
)
public class CucumberRunner {
}