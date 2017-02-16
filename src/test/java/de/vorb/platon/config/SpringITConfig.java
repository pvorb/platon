/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.vorb.platon.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;

import java.net.MalformedURLException;
import java.net.URL;

@Configuration
@EnableAutoConfiguration
@PropertySource(value = "classpath:config/application.properties")
public class SpringITConfig {

    @Autowired
    private Environment env;

    @Bean
    public WebDriver webDriver() throws MalformedURLException {
        return new RemoteWebDriver(getRemoteUrl(), getDesiredCapabilities());
    }

    private DesiredCapabilities getDesiredCapabilities() {

        final DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        if (useSauceLabs()) {
            capabilities.setCapability("name", "Firefox");
            capabilities.setCapability("tunnel-identifier", env.getProperty("TRAVIS_JOB_NUMBER"));
            capabilities.setCapability("seleniumVersion", env.getProperty("selenium.version"));
        }

        return capabilities;
    }

    private boolean useSauceLabs() {
        return env.getProperty("SAUCE_USERNAME") != null;
    }

    private URL getRemoteUrl() throws MalformedURLException {
        if (useSauceLabs()) {
            return new URL(String.format("http://%s:%s@localhost:4445/wd/hub",
                    env.getProperty("SAUCE_USERNAME"), env.getProperty("SAUCE_ACCESS_KEY")));
        } else {
            return new URL("http://localhost:4445/wd/hub");
        }
    }

}
