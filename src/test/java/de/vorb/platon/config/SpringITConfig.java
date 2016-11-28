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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.net.URL;

@Configuration
@PropertySource(value = "classpath:config/application.properties")
public class SpringITConfig {

    @Value("${TRAVIS_JOB_NUMBER}")
    private String jobNumber;

    @Value("${selenium.version}")
    private String seleniumVersion;

    @Value("http://${SAUCE_USERNAME}:${SAUCE_ACCESS_KEY}@localhost:4445/wd/hub")
    private URL remoteUrl;

    @Bean
    public WebDriver webDriver() {

        final DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        capabilities.setCapability("tunnel-identifier", jobNumber);
        capabilities.setCapability("seleniumVersion", seleniumVersion);

        return new RemoteWebDriver(remoteUrl, capabilities);
    }

}
