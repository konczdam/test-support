package hu.konczdam.testsupport.profile;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.IOException;

/**
 * ApplicationContextInitializer that automatically adds test profiles.
 *
 * <p>Always adds the 'test' profile with application-test.properties.
 * When the system property 'environment=github_workflow' is set,
 * also adds the 'github_workflow' profile with application-github_workflow.properties.</p>
 *
 * <p>Usage in tests:</p>
 * <pre>
 * &#64;ContextConfiguration(initializers = [ProfileAddingContextInitializer::class])
 * </pre>
 */
public class ProfileAddingContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext>, Ordered {
    @Override
    public void initialize(@NotNull ConfigurableApplicationContext applicationContext) {
        // Always add the 'test' profile
        addProfileWithProperties(applicationContext, "test", "application-test.properties");

        // Check if the system property for 'environment' is set and then add 'github_workflow'
        String environment = System.getProperty("environment");
        if ("github_workflow".equals(environment)) {
            addProfileWithProperties(applicationContext, "github_workflow", "application-github_workflow.properties");
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    private void addProfileWithProperties(@NotNull ConfigurableApplicationContext applicationContext, String profileName, String propertiesFileName) {
        ConfigurableEnvironment env = applicationContext.getEnvironment();
        env.addActiveProfile(profileName);
        var resource = new EncodedResource(applicationContext.getResource("classpath:" + propertiesFileName));
        try {
            env.getPropertySources().addFirst(new ResourcePropertySource(resource));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
