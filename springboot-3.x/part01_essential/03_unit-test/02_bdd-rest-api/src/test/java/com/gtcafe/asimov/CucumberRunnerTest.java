package com.gtcafe.asimov;

import org.junit.platform.suite.api.*;

import io.cucumber.spring.CucumberSpringSupport;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
    key = "cucumber.plugin", 
    value = "pretty,html:build/cucumber-report.html,json:build/cucumber-report.json"
)
public class CucumberRunnerTest extends CucumberSpringSupport {
}